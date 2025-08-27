package unibs.asd.benchmarks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

import unibs.asd.enums.BitSetType;
import unibs.asd.fileio.BenchmarkReader;
import unibs.asd.interfaces.MHS;
import unibs.asd.mhs.BoostMHS;

public class BenchmarkComparison {
    
    private static final String RESULTS_DIR = "results/time_limited";
    private static final String BENCHMARKS_DIR = "src/mybenchmarks";
    
    static class BenchmarkResult {
        String filename;
        double timeSeconds;
        int rows, cols, solutions;
        boolean completed;
        
        BenchmarkResult(String filename, double timeSeconds, int rows, int cols, int solutions, boolean completed) {
            this.filename = filename;
            this.timeSeconds = timeSeconds;
            this.rows = rows;
            this.cols = cols;
            this.solutions = solutions;
            this.completed = completed;
        }
    }
    
    public static void main(String[] args) throws IOException {
        // Crea il nome del file di output con timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String outputFileName = "results/basemhs_vs_boostmhs_" + timestamp + ".txt";
        
        // Crea la directory results se non esiste
        Files.createDirectories(Paths.get("results"));
        
        try (PrintWriter writer = new PrintWriter(outputFileName)) {
            // Fase 1: Seleziona 10 benchmark ottimali
            List<BenchmarkResult> candidates = selectBenchmarks();
            
            writer.println("=== SELECTED BENCHMARKS FOR COMPARISON ===");
            candidates.forEach(b -> writer.printf("%-25s %dx%-4d %6.3fs %d solutions%n", 
                b.filename, b.rows, b.cols, b.timeSeconds, b.solutions));
            
            writer.println("\n=== RUNNING COMPARISON ===");
            writer.printf("%-25s %-12s %-12s %-8s%n", "Benchmark", "BaseMHS(s)", "BoostMHS(s)", "Speedup");
            writer.println("-".repeat(65));
            
            // Anche su console per monitoraggio
            System.out.println("Writing comparison results to: " + outputFileName);
            System.out.println("Running comparison...");
            
            List<Double> speedups = new ArrayList<>();
            
            for (BenchmarkResult result : candidates) {
                System.out.printf("Testing: %s... ", result.filename.replace(".mhs", ""));
                
                try {
                    boolean[][] matrix = BenchmarkReader.readBenchmark(BENCHMARKS_DIR + "/" + result.filename.replace(".mhs", ".matrix"));
                    
                    // Test BaseMHS
                    MHS baseMHS = new BoostMHS(matrix);
                    long startTime = System.nanoTime();
                    baseMHS.run(BitSetType.FAST_BITSET, 86400000); // 24 ore
                    long baseDuration = System.nanoTime() - startTime;
                    double baseSeconds = baseDuration / 1_000_000_000.0;
                    
                    String baseTime = String.format("%.3f", baseSeconds);
                    double speedup = baseSeconds / result.timeSeconds;
                    
                    writer.printf("%-25s %-12s %-12.3f ", 
                        result.filename.replace(".mhs", ""), baseTime, result.timeSeconds);
                    
                    writer.printf("%.2fx%n", speedup);
                    System.out.printf("%.2fx%n", speedup);
                    speedups.add(speedup);
                    
                    // Cleanup
                    System.gc();
                    Thread.sleep(1000);
                    
                } catch (Exception e) {
                    writer.printf("%-25s %-12s %-12.3f ERROR%n", 
                        result.filename.replace(".mhs", ""), "ERROR", result.timeSeconds);
                    System.out.println("ERROR");
                }
            }
            
            // Statistiche finali
            if (!speedups.isEmpty()) {
                double avgSpeedup = speedups.stream().mapToDouble(d -> d).average().orElse(0);
                double maxSpeedup = speedups.stream().mapToDouble(d -> d).max().orElse(0);
                
                writer.println("\n=== PERFORMANCE SUMMARY ===");
                writer.printf("Average speedup: %.2fx%n", avgSpeedup);
                writer.printf("Maximum speedup: %.2fx%n", maxSpeedup);
                writer.printf("Benchmarks completed by both: %d/%d%n", speedups.size(), candidates.size());
                
                System.out.println("\n=== PERFORMANCE SUMMARY ===");
                System.out.printf("Average speedup: %.2fx%n", avgSpeedup);
                System.out.printf("Maximum speedup: %.2fx%n", maxSpeedup);
            }
            
            System.out.println("Results saved to: " + outputFileName);
        }
    }
    
    private static List<BenchmarkResult> selectBenchmarks() throws IOException {
        List<BenchmarkResult> allResults = new ArrayList<>();
        
        // Scansiona tutti i file .mhs
        try (Stream<Path> files = Files.list(Paths.get(RESULTS_DIR))) {
            files.filter(path -> path.toString().endsWith(".mhs"))
                 .forEach(path -> {
                     try {
                         BenchmarkResult result = parseResultFile(path);
                         if (result != null) allResults.add(result);
                     } catch (IOException e) {
                         System.err.println("Error parsing: " + path.getFileName());
                     }
                 });
        }
        
        // Filtra per criteri di selezione
        List<BenchmarkResult> candidates = allResults.stream()
            .filter(r -> r.completed)
            .filter(r -> r.timeSeconds >= 0.0005 && r.timeSeconds <= 100.0) // 0.5ms - 100s
            .sorted(Comparator.comparing(r -> r.timeSeconds))
            .toList();
        
        // Seleziona 10 con buona distribuzione
        return selectDiverseBenchmarks(candidates, 10);
    }
    
    private static BenchmarkResult parseResultFile(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        
        String filename = filePath.getFileName().toString();
        boolean completed = false;
        double timeNs = 0;
        int rows = 0, cols = 0, solutions = 0;
        
        for (String line : lines) {
            if (line.contains("COMPLETED SUCCESSFULLY")) completed = true;
            else if (line.startsWith(";;; Total computation time (nanoseconds): ")) {
                timeNs = Double.parseDouble(line.split(": ")[1]);
            }
            else if (line.startsWith(";;; Number of rows (elements): ")) {
                rows = Integer.parseInt(line.split(": ")[1]);
            }
            else if (line.startsWith(";;; Total number of columns: ")) {
                cols = Integer.parseInt(line.split(": ")[1]);
            }
            else if (line.startsWith(";;; Number of solutions found: ")) {
                solutions = Integer.parseInt(line.split(": ")[1]);
            }
        }
        
        return completed ? new BenchmarkResult(filename, timeNs / 1_000_000_000.0, rows, cols, solutions, true) : null;
    }
    
    private static List<BenchmarkResult> selectDiverseBenchmarks(List<BenchmarkResult> candidates, int count) {
        if (candidates.size() <= count) return candidates;
        
        List<BenchmarkResult> selected = new ArrayList<>();
        
        // Distribuzione: 30% veloci, 40% medi, 30% lenti
        int fastCount = Math.max(1, count * 3 / 10);
        int mediumCount = Math.max(1, count * 4 / 10);
        int slowCount = count - fastCount - mediumCount;
        
        int size = candidates.size();
        
        // Veloci (primi 30%)
        for (int i = 0; i < fastCount && i < size * 0.3; i++) {
            selected.add(candidates.get((int)(i * size * 0.3 / fastCount)));
        }
        
        // Medi (30%-70%)
        for (int i = 0; i < mediumCount; i++) {
            int index = (int)(size * 0.3 + i * size * 0.4 / mediumCount);
            if (index < size) selected.add(candidates.get(index));
        }
        
        // Lenti (ultimi 30%)
        for (int i = 0; i < slowCount; i++) {
            int index = (int)(size * 0.7 + i * size * 0.3 / slowCount);
            if (index < size) selected.add(candidates.get(index));
        }
        
        return selected;
    }
}