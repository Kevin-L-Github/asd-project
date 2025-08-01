package unibs.asd.experiments;

import java.util.ArrayList;
import java.util.List;

public class TestBenchsNew {
    public static void main(String[] args) {
        String benchmarkDir = "src\\mybenchmarks";
        
        // Definiamo i livelli di timeout e le relative cartelle di output
        List<BenchmarkLevel> levels = new ArrayList<>();
        levels.add(new BenchmarkLevel(1_000, "results\\1000ms", "solved_benchmarks_1000ms.txt"));
        levels.add(new BenchmarkLevel(30_000, "results\\30_000ms", "solved_benchmarks_30_000ms.txt"));
        levels.add(new BenchmarkLevel(300_000, "results\\300_000ms", "solved_benchmarks_300_000ms.txt"));
        levels.add(new BenchmarkLevel(600_000, "results\\600_000ms", "solved_benchmarks_600_000ms.txt"));

        // Eseguiamo i benchmark per ogni livello
        for (int i = 0; i < levels.size(); i++) {
            BenchmarkLevel currentLevel = levels.get(i);
            System.out.println("Processing benchmarks with timeout " + currentLevel.timeoutMs + "ms");
            
            // Prepara la lista dei file già risolti nei livelli precedenti
            List<String> alreadySolvedFiles = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                alreadySolvedFiles.add(levels.get(j).getSolvedFilePath());
            }
            
            try {
                FilterBenchmarks filterBenchmarks = new FilterBenchmarks(
                    benchmarkDir, 
                    currentLevel.destDir, 
                    currentLevel.solvedFilename, 
                    currentLevel.timeoutMs, 
                    alreadySolvedFiles);
                
                filterBenchmarks.runBenchmarks();
                System.out.println("Benchmarks processed successfully for timeout " + currentLevel.timeoutMs + "ms");
            } catch (Exception e) {
                System.err.println("Error processing benchmarks for timeout " + currentLevel.timeoutMs + "ms: " + e.getMessage());
            }
        }
    }
    
    // Helper class to manage timeout levels
    private static class BenchmarkLevel {
        final int timeoutMs;
        final String destDir;
        final String solvedFilename;
        
        public BenchmarkLevel(int timeoutMs, String destDir, String solvedFilename) {
            this.timeoutMs = timeoutMs;
            this.destDir = destDir;
            this.solvedFilename = solvedFilename;
        }
        
        public String getSolvedFilePath() {
            return destDir + "\\" + solvedFilename;
        }
    }
}