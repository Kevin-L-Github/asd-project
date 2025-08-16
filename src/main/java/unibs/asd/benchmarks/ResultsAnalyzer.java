package unibs.asd.benchmarks;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * Analyzes benchmark results from .mhs files and provides comprehensive statistics
 * about execution outcomes, performance metrics, and problem characteristics.
 */
public class ResultsAnalyzer {
    
    private static final DecimalFormat PERCENTAGE_FORMAT = new DecimalFormat("0.00%");
    private static final DecimalFormat TIME_FORMAT = new DecimalFormat("0.###");
    private PrintWriter out; // Aggiunto per gestire l'output
    
    public static void main(String[] args) {
        String resultsDirectory = "results/time_limited";
        
        if (args.length > 0) {
            resultsDirectory = args[0];
        }
        
        try {
            ResultsAnalyzer analyzer = new ResultsAnalyzer();
            analyzer.analyzeResults(resultsDirectory);
        } catch (IOException e) {
            System.err.println("Error analyzing results: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Main analysis method that processes all .mhs files in the directory
     */
    public void analyzeResults(String resultsDirectory) throws IOException {
        // Crea il nome del file di output con timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());
        String outputFileName = "results/analysis_report_" + timestamp + ".txt";
        
        // Crea la directory results se non esiste
        Files.createDirectories(Paths.get("results"));
        
        // Inizializza il PrintWriter per scrivere su file
        try (PrintWriter writer = new PrintWriter(outputFileName)) {
            this.out = writer;
            
            // Notifica all'utente dove viene salvato il file
            System.out.println("Writing analysis report to: " + outputFileName);
            
            out.println("=".repeat(60));
            out.println("           BENCHMARK RESULTS ANALYSIS");
            out.println("=".repeat(60));
            out.println("Directory: " + resultsDirectory);
            out.println("Report generated: " + new Date());
            out.println();
            
            List<BenchmarkResult> results = loadAllResults(resultsDirectory);
            
            if (results.isEmpty()) {
                out.println("No .mhs files found in the specified directory.");
                System.out.println("No .mhs files found in the specified directory.");
                return;
            }
            
            printExecutionSummary(results);
            printPerformanceStatistics(results);
            printProblemCharacteristics(results);
            printDetailedBreakdown(results);
            
            System.out.println("Analysis complete! Report saved to: " + outputFileName);
        }
    }
    
    /**
     * Loads and parses all .mhs files in the directory
     */
    private List<BenchmarkResult> loadAllResults(String directory) throws IOException {
        List<BenchmarkResult> results = new ArrayList<>();
        
        try (Stream<Path> files = Files.list(Paths.get(directory))) {
            files.filter(path -> path.toString().endsWith(".mhs"))
                 .forEach(path -> {
                     try {
                         BenchmarkResult result = parseBenchmarkFile(path);
                         results.add(result);
                     } catch (IOException e) {
                         System.err.println("Error parsing file " + path.getFileName() + ": " + e.getMessage());
                     }
                 });
        }
        
        results.sort(Comparator.comparing(r -> r.filename));
        return results;
    }
    
    /**
     * Parses a single .mhs file and extracts relevant information
     */
    private BenchmarkResult parseBenchmarkFile(Path filePath) throws IOException {
        BenchmarkResult result = new BenchmarkResult();
        result.filename = filePath.getFileName().toString();
        
        List<String> lines = Files.readAllLines(filePath);
        
        for (String line : lines) {
            if (line.startsWith(";;; Algorithm completion status: ")) {
                result.status = line.substring(";;; Algorithm completion status: ".length());
            } else if (line.startsWith(";;; Number of solutions found: ")) {
                result.solutionsFound = Integer.parseInt(line.substring(";;; Number of solutions found: ".length()));
            } else if (line.startsWith(";;; Min cardinality: ")) {
                result.minCardinality = Integer.parseInt(line.substring(";;; Min cardinality: ".length()));
            } else if (line.startsWith(";;; Max cardinality: ")) {
                result.maxCardinality = Integer.parseInt(line.substring(";;; Max cardinality: ".length()));
            } else if (line.startsWith(";;; Number of rows (elements): ")) {
                result.rows = Integer.parseInt(line.substring(";;; Number of rows (elements): ".length()));
            } else if (line.startsWith(";;; Total number of columns: ")) {
                result.totalColumns = Integer.parseInt(line.substring(";;; Total number of columns: ".length()));
            } else if (line.startsWith(";;; Number of non-empty columns: ")) {
                result.nonEmptyColumns = Integer.parseInt(line.substring(";;; Number of non-empty columns: ".length()));
            } else if (line.startsWith(";;; Total computation time (nanoseconds): ")) {
                result.computationTimeNs = Double.parseDouble(line.substring(";;; Total computation time (nanoseconds): ".length()));
            } else if (line.startsWith(";;; Execution was interrupted, reason: ")) {
                result.stopReason = line.substring(";;; Execution was interrupted, reason: ".length());
            } else if (line.startsWith(";;; Depth reached: ")) {
                String depthInfo = line.substring(";;; Depth reached: ".length());
                String[] parts = depthInfo.split("/");
                result.depthReached = Integer.parseInt(parts[0]);
                result.maxDepth = Integer.parseInt(parts[1]);
            }
        }
        
        return result;
    }
    
    /**
     * Prints overall execution summary with completion rates
     */
    private void printExecutionSummary(List<BenchmarkResult> results) {
        out.println("EXECUTION SUMMARY");
        out.println("-".repeat(40));
        
        int total = results.size();
        int completed = (int) results.stream().filter(r -> "COMPLETED SUCCESSFULLY".equals(r.status)).count();
        int timeouts = (int) results.stream().filter(r -> "STOPPED BEFORE COMPLETION".equals(r.status)).count();
        int timeoutsDuringProcessing = (int) results.stream().filter(r -> "TIMEOUT DURING PROCESSING".equals(r.stopReason)).count();
        int outOfMemory = (int) results.stream().filter(r -> "OUT OF MEMORY".equals(r.stopReason)).count();
        int timeoutsBeforeProcessing = timeouts - timeoutsDuringProcessing - outOfMemory;
        
        out.printf("Total benchmarks processed: %d%n", total);
        out.printf("Successfully completed: %d (%s)%n", completed, PERCENTAGE_FORMAT.format((double) completed / total));
        out.printf("Stopped before completion: %d (%s)%n", timeouts, PERCENTAGE_FORMAT.format((double) timeouts / total));
        out.printf("  - Timeout during processing: %d (%s)%n", timeoutsDuringProcessing, PERCENTAGE_FORMAT.format((double) timeoutsDuringProcessing / total));
        out.printf("  - Out of memory: %d (%s)%n", outOfMemory, PERCENTAGE_FORMAT.format((double) outOfMemory / total));
        out.printf("  - Timeout before processing: %d (%s)%n", timeoutsBeforeProcessing, PERCENTAGE_FORMAT.format((double) timeoutsBeforeProcessing / total));
        out.println();
    }
    
    /**
     * Prints performance statistics for completed benchmarks
     */
    private void printPerformanceStatistics(List<BenchmarkResult> results) {
        List<BenchmarkResult> completed = results.stream()
                .filter(r -> "COMPLETED SUCCESSFULLY".equals(r.status))
                .toList();
        
        if (completed.isEmpty()) {
            out.println("PERFORMANCE STATISTICS");
            out.println("-".repeat(40));
            out.println("No completed benchmarks to analyze.");
            out.println();
            return;
        }
        
        out.println("PERFORMANCE STATISTICS (Completed benchmarks only)");
        out.println("-".repeat(50));
        
        DoubleSummaryStatistics timeStats = completed.stream()
                .mapToDouble(r -> r.computationTimeNs / 1_000_000) // Convert to milliseconds
                .summaryStatistics();
        
        IntSummaryStatistics solutionStats = completed.stream()
                .mapToInt(r -> r.solutionsFound)
                .summaryStatistics();
        
        out.printf("Execution times (milliseconds):%n");
        out.printf("  Min: %s ms%n", TIME_FORMAT.format(timeStats.getMin()));
        out.printf("  Max: %s ms%n", TIME_FORMAT.format(timeStats.getMax()));
        out.printf("  Average: %s ms%n", TIME_FORMAT.format(timeStats.getAverage()));
        out.println();
        
        out.printf("Solutions found:%n");
        out.printf("  Min: %d%n", solutionStats.getMin());
        out.printf("  Max: %d%n", solutionStats.getMax());
        out.printf("  Average: %.2f%n", solutionStats.getAverage());
        out.printf("  Total: %d%n", solutionStats.getSum());
        out.println();
    }
    
    /**
     * Prints characteristics of the problem instances
     */
    private void printProblemCharacteristics(List<BenchmarkResult> results) {
        out.println("PROBLEM CHARACTERISTICS");
        out.println("-".repeat(40));
        
        IntSummaryStatistics rowStats = results.stream().mapToInt(r -> r.rows).summaryStatistics();
        IntSummaryStatistics colStats = results.stream().mapToInt(r -> r.totalColumns).summaryStatistics();
        IntSummaryStatistics nonEmptyColStats = results.stream().mapToInt(r -> r.nonEmptyColumns).summaryStatistics();
        
        out.printf("Matrix dimensions:%n");
        out.printf("  Rows - Min: %d, Max: %d, Avg: %.1f%n", 
                rowStats.getMin(), rowStats.getMax(), rowStats.getAverage());
        out.printf("  Total columns - Min: %d, Max: %d, Avg: %.1f%n", 
                colStats.getMin(), colStats.getMax(), colStats.getAverage());
        out.printf("  Non-empty columns - Min: %d, Max: %d, Avg: %.1f%n", 
                nonEmptyColStats.getMin(), nonEmptyColStats.getMax(), nonEmptyColStats.getAverage());
        
        double avgSparsity = results.stream()
                .mapToDouble(r -> 1.0 - (double) r.nonEmptyColumns / r.totalColumns)
                .average().orElse(0.0);
        out.printf("  Average column sparsity: %s%n", PERCENTAGE_FORMAT.format(avgSparsity));
        out.println();
    }
    
    /**
     * Prints detailed breakdown of results by category
     */
    private void printDetailedBreakdown(List<BenchmarkResult> results) {
        out.println("DETAILED BREAKDOWN");
        out.println("-".repeat(40));
        
        // Group by problem size categories
        Map<String, List<BenchmarkResult>> sizeCategories = new TreeMap<>();
        
        for (BenchmarkResult result : results) {
            String category = categorizeBySize(result);
            sizeCategories.computeIfAbsent(category, k -> new ArrayList<>()).add(result);
        }
        
        for (Map.Entry<String, List<BenchmarkResult>> entry : sizeCategories.entrySet()) {
            List<BenchmarkResult> categoryResults = entry.getValue();
            int completed = (int) categoryResults.stream().filter(r -> "COMPLETED SUCCESSFULLY".equals(r.status)).count();
            
            out.printf("%s: %d total, %d completed (%s)%n", 
                    entry.getKey(), 
                    categoryResults.size(), 
                    completed, 
                    PERCENTAGE_FORMAT.format((double) completed / categoryResults.size()));
        }
        out.println();
        
        // Aggiungi una sezione finale con i file analizzati
        out.println("FILES ANALYZED");
        out.println("-".repeat(40));
        out.printf("Total files processed: %d%n", results.size());
        out.println();
    }
    
    /**
     * Categorizes a benchmark result by problem size
     */
    private String categorizeBySize(BenchmarkResult result) {
        int effectiveSize = result.rows * result.nonEmptyColumns;
        
        if (effectiveSize < 100) return "Very Small (< 100 cells)";
        if (effectiveSize < 1000) return "Small (< 1K cells)";
        if (effectiveSize < 10000) return "Medium (< 10K cells)";
        if (effectiveSize < 100000) return "Large (< 100K cells)";
        return "Very Large (>= 100K cells)";
    }
    
    /**
     * Data class to hold benchmark result information
     */
    private static class BenchmarkResult {
        String filename;
        String status;
        String stopReason;
        int solutionsFound;
        int minCardinality;
        int maxCardinality;
        int rows;
        int totalColumns;
        int nonEmptyColumns;
        double computationTimeNs;
        int depthReached;
        int maxDepth;
    }
}