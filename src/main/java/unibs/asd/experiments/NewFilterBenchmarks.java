package unibs.asd.experiments;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import unibs.asd.enums.BitSetType;
import unibs.asd.fileio.BenchmarkReader;
import unibs.asd.fileio.BenchmarkWriter;
import unibs.asd.interfaces.MHS;
import unibs.asd.mhs.BoostMHS;

/**
 * Enhanced class for running and filtering benchmarks with multi-level approach.
 * Automatically classifies benchmarks by complexity and applies appropriate timeouts.
 */
public class NewFilterBenchmarks {
    
    // Configuration and state
    private final BenchmarkConfig config;
    private final BenchmarkResults results;
    private final BenchmarkReporter reporter;
    
    /**
     * Main constructor
     */
    public NewFilterBenchmarks(String benchmarkDir, String destDir, BitSetType bitSetType) {
        this(benchmarkDir, destDir, bitSetType, true, EnumSet.allOf(BenchmarkLevel.class));
    }
    
    /**
     * Constructor with level filtering options
     */
    public NewFilterBenchmarks(String benchmarkDir, String destDir, BitSetType bitSetType,
                         boolean enableLevelFiltering, Set<BenchmarkLevel> enabledLevels) {
        this.config = new BenchmarkConfig(benchmarkDir, destDir, bitSetType, 
                                       enableLevelFiltering, enabledLevels);
        this.results = new BenchmarkResults();
        this.reporter = new BenchmarkReporter(config, results);
    }
    
    /**
     * Main entry point - runs all enabled benchmarks
     */
    public void runBenchmarks() throws IOException {
        reporter.printStartHeader();
        
        List<BenchmarkInfo> benchmarks = new BenchmarkAnalyzer(config).discoverAndClassifyBenchmarks();
        
        if (config.isLevelFilteringEnabled()) {
            runBenchmarksByLevel(benchmarks);
        } else {
            runBenchmarksSequentially(benchmarks);
        }
        
        reporter.generateFinalReport();
    }
    
    private void runBenchmarksByLevel(List<BenchmarkInfo> benchmarks) throws IOException {
        for (BenchmarkLevel level : BenchmarkLevel.values()) {
            if (!config.isLevelEnabled(level)) {
                continue;
            }
            
            List<BenchmarkInfo> levelBenchmarks = filterBenchmarksByLevel(benchmarks, level);
            if (!levelBenchmarks.isEmpty()) {
                runSingleLevel(level, levelBenchmarks);
            }
        }
    }
    
    private List<BenchmarkInfo> filterBenchmarksByLevel(List<BenchmarkInfo> benchmarks, BenchmarkLevel level) {
        return benchmarks.stream()
            .filter(b -> b.level == level)
            .sorted(Comparator.comparing(b -> b.filename))
            .toList();
    }
    
    private void runSingleLevel(BenchmarkLevel level, List<BenchmarkInfo> benchmarks) throws IOException {
        reporter.printLevelHeader(level, benchmarks.size());
        
        Path levelResultsFile = reporter.prepareLevelResultsFile(level);
        
        for (int i = 0; i < benchmarks.size(); i++) {
            BenchmarkInfo benchmark = benchmarks.get(i);
            System.out.printf("\n[%d/%d] ", i + 1, benchmarks.size());
            
            BenchmarkExecutor executor = new BenchmarkExecutor(config);
            BenchmarkResult result = executor.executeBenchmark(benchmark, level.getTimeoutMs());
            
            results.addResult(level, result);
            reporter.writeAndPrintResult(result, levelResultsFile);
        }
        
        reporter.printLevelSummary(level);
    }
    
    private void runBenchmarksSequentially(List<BenchmarkInfo> benchmarks) throws IOException {
        System.out.println("Running benchmarks sequentially (legacy mode)...");
        Path resultsFile = reporter.prepareSequentialResultsFile();
        
        for (int i = 0; i < benchmarks.size(); i++) {
            BenchmarkInfo benchmark = benchmarks.get(i);
            System.out.printf("[%d/%d] ", i + 1, benchmarks.size());
            
            BenchmarkExecutor executor = new BenchmarkExecutor(config);
            BenchmarkResult result = executor.executeBenchmark(benchmark, benchmark.level.getTimeoutMs());
            
            results.addResult(benchmark.level, result);
            reporter.writeAndPrintResult(result, resultsFile);
        }
    }
    
    // Utility methods for selective execution
    public void runOnlyQuickTests() throws IOException {
        NewFilterBenchmarks runner = new NewFilterBenchmarks(config.benchmarkDir, config.destDir, 
                                                   config.bitSetType, true, 
                                                   EnumSet.of(BenchmarkLevel.QUICK));
        runner.runBenchmarks();
    }
    
    public void runOnlyIntensiveTests() throws IOException {
        NewFilterBenchmarks runner = new NewFilterBenchmarks(config.benchmarkDir, config.destDir, 
                                                   config.bitSetType, true, 
                                                   EnumSet.of(BenchmarkLevel.INTENSIVE));
        runner.runBenchmarks();
    }
    
    // Data access methods
    public List<BenchmarkResult> getAllResults() {
        return results.getAllResults();
    }
    
    public Map<BenchmarkLevel, List<BenchmarkResult>> getResultsByLevel() {
        return results.getResultsByLevel();
    }
    
    // Inner classes for better organization
    
    /**
     * Configuration holder class
     */
    private static class BenchmarkConfig {
        final String benchmarkDir;
        final String destDir;
        final BitSetType bitSetType;
        final boolean enableLevelFiltering;
        final Set<BenchmarkLevel> enabledLevels;
        
        BenchmarkConfig(String benchmarkDir, String destDir, BitSetType bitSetType,
                       boolean enableLevelFiltering, Set<BenchmarkLevel> enabledLevels) {
            this.benchmarkDir = benchmarkDir;
            this.destDir = destDir;
            this.bitSetType = bitSetType;
            this.enableLevelFiltering = enableLevelFiltering;
            this.enabledLevels = enabledLevels != null ? enabledLevels : EnumSet.allOf(BenchmarkLevel.class);
        }
        
        boolean isLevelFilteringEnabled() {
            return enableLevelFiltering;
        }
        
        boolean isLevelEnabled(BenchmarkLevel level) {
            return enabledLevels.contains(level);
        }
    }
    
    /**
     * Benchmark level classification
     */
    public enum BenchmarkLevel {
        QUICK(30_000, "Quick tests for small/simple matrices"),
        MEDIUM(300_000, "Medium tests for intermediate complexity"), 
        INTENSIVE(1_800_000, "Intensive tests for large/complex matrices");
        
        private final int timeoutMs;
        private final String description;
        
        BenchmarkLevel(int timeoutMs, String description) {
            this.timeoutMs = timeoutMs;
            this.description = description;
        }
        
        public int getTimeoutMs() { return timeoutMs; }
        public String getDescription() { return description; }
    }
    
    /**
     * Benchmark information holder
     */
    public static class BenchmarkInfo {
        public final String filename;
        public final int rows;
        public final int cols; 
        public final double density;
        public final BenchmarkLevel level;
        public final String estimatedComplexity;
        
        public BenchmarkInfo(String filename, int rows, int cols, double density, 
                           BenchmarkLevel level, String estimatedComplexity) {
            this.filename = filename;
            this.rows = rows;
            this.cols = cols;
            this.density = density;
            this.level = level;
            this.estimatedComplexity = estimatedComplexity;
        }
        
        @Override
        public String toString() {
            return String.format("%s: %dx%d, density=%.3f, complexity=%s, level=%s",
                filename, rows, cols, density, estimatedComplexity, level);
        }
    }
    
    /**
     * Benchmark result holder
     */
    public static class BenchmarkResult {
        public final String filename;
        public final BenchmarkLevel level;
        public final long executionTimeMs;
        public final boolean completed;
        public final boolean interrupted;
        public final int mhsCount;
        public final String error;
        
        public BenchmarkResult(String filename, BenchmarkLevel level, long executionTimeMs,
                             boolean completed, boolean interrupted, int mhsCount, String error) {
            this.filename = filename;
            this.level = level;
            this.executionTimeMs = executionTimeMs;
            this.completed = completed;
            this.interrupted = interrupted;
            this.mhsCount = mhsCount;
            this.error = error;
        }
    }
    
    /**
     * Results tracker
     */
    private static class BenchmarkResults {
        private final List<BenchmarkResult> allResults = new ArrayList<>();
        private final Map<BenchmarkLevel, List<BenchmarkResult>> resultsByLevel = new EnumMap<>(BenchmarkLevel.class);
        
        BenchmarkResults() {
            for (BenchmarkLevel level : BenchmarkLevel.values()) {
                resultsByLevel.put(level, new ArrayList<>());
            }
        }
        
        void addResult(BenchmarkLevel level, BenchmarkResult result) {
            allResults.add(result);
            resultsByLevel.get(level).add(result);
        }
        
        List<BenchmarkResult> getAllResults() {
            return new ArrayList<>(allResults);
        }
        
        Map<BenchmarkLevel, List<BenchmarkResult>> getResultsByLevel() {
            return new EnumMap<>(resultsByLevel);
        }
    }
    
    /**
     * Benchmark analyzer and classifier
     */
    private static class BenchmarkAnalyzer {
        private final BenchmarkConfig config;
        
        BenchmarkAnalyzer(BenchmarkConfig config) {
            this.config = config;
        }
        
        List<BenchmarkInfo> discoverAndClassifyBenchmarks() throws IOException {
            List<BenchmarkInfo> benchmarks = new ArrayList<>();
            
            System.out.println("🔍 Discovering and classifying benchmarks...");
            
            try (Stream<Path> filesStream = Files.list(Paths.get(config.benchmarkDir))) {
                filesStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".matrix"))
                    .sorted(Comparator.comparing(Path::getFileName))
                    .forEach(path -> {
                        try {
                            BenchmarkInfo info = analyzeBenchmarkFile(path);
                            benchmarks.add(info);
                            System.out.println("📁 " + info.toString());
                        } catch (Exception e) {
                            System.err.println("❌ Error analyzing " + path.getFileName() + ": " + e.getMessage());
                        }
                    });
            }
            
            printClassificationSummary(benchmarks);
            return benchmarks;
        }
        
        private BenchmarkInfo analyzeBenchmarkFile(Path filePath) throws IOException {
            boolean[][] matrix = BenchmarkReader.readBenchmark(filePath.toString());
            
            int rows = matrix.length;
            int cols = rows > 0 ? matrix[0].length : 0;
            double density = calculateMatrixDensity(matrix, rows, cols);
            
            String complexity = estimateComplexity(rows, cols, density);
            BenchmarkLevel level = determineLevel(rows, cols, density, complexity);
            
            return new BenchmarkInfo(filePath.getFileName().toString(), rows, cols, 
                                   density, level, complexity);
        }
        
        private double calculateMatrixDensity(boolean[][] matrix, int rows, int cols) {
            int onesCount = 0;
            int totalElements = rows * cols;
            
            for (boolean[] row : matrix) {
                for (boolean cell : row) {
                    if (cell) onesCount++;
                }
            }
            
            return totalElements > 0 ? (double) onesCount / totalElements : 0.0;
        }
        
        private String estimateComplexity(int rows, int cols, double density) {
            int sizeFactor = rows * cols;
            
            if (sizeFactor < 100) {
                return "LOW";
            } else if (sizeFactor < 1000) {
                return density > 0.3 ? "MEDIUM" : "LOW";
            } else if (sizeFactor < 5000) {
                return density > 0.2 ? "HIGH" : "MEDIUM";
            } else {
                return "VERY_HIGH";
            }
        }
        
        private BenchmarkLevel determineLevel(int rows, int cols, double density, String complexity) {
            int sizeFactor = rows * cols;
            
            if (sizeFactor < 50 || (density < 0.05 && sizeFactor < 500)) {
                return BenchmarkLevel.QUICK;
            }
            
            if ((sizeFactor > 2000 && density > 0.3) || sizeFactor > 10000) {
                return BenchmarkLevel.INTENSIVE;
            }
            
            return BenchmarkLevel.MEDIUM;
        }
        
        private void printClassificationSummary(List<BenchmarkInfo> benchmarks) {
            System.out.println("\n📊 CLASSIFICATION SUMMARY:");
            
            Map<BenchmarkLevel, Long> levelCounts = benchmarks.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    b -> b.level, java.util.stream.Collectors.counting()));
            
            for (BenchmarkLevel level : BenchmarkLevel.values()) {
                long count = levelCounts.getOrDefault(level, 0L);
                String status = config.enabledLevels.contains(level) ? "✅" : "⏸️";
                System.out.printf("   %s %s: %d benchmarks (timeout: %ds)%n", 
                    status, level.name(), count, level.getTimeoutMs() / 1000);
            }
            
            System.out.printf("📁 Total benchmarks found: %d%n", benchmarks.size());
        }
    }
    
    /**
     * Benchmark executor
     */
    private static class BenchmarkExecutor {
        private final BenchmarkConfig config;
        
        BenchmarkExecutor(BenchmarkConfig config) {
            this.config = config;
        }
        
        BenchmarkResult executeBenchmark(BenchmarkInfo benchmarkInfo, int timeoutMs) {
            clearMemoryBeforeExecution();
            
            try {
                String fullPath = Paths.get(config.benchmarkDir, benchmarkInfo.filename).toString();
                boolean[][] instance = BenchmarkReader.readBenchmark(fullPath);
                MHS mhsSolver = new BoostMHS(instance);
                
                long startTime = System.currentTimeMillis();
                mhsSolver.run(config.bitSetType, timeoutMs);
                long executionTime = (long) mhsSolver.getComputationTime();
                
                boolean completed = mhsSolver.isExecuted() && !mhsSolver.isStopped();
                boolean interrupted = mhsSolver.isStopped();
                int mhsCount = 0; // Adapt based on your MHS interface
                
                if (completed) {
                    writeSolution(mhsSolver, benchmarkInfo.filename);
                }
                
                return new BenchmarkResult(benchmarkInfo.filename, benchmarkInfo.level, 
                                         executionTime, completed, interrupted, 
                                         mhsCount, null);
                
            } catch (Exception e) {
                return new BenchmarkResult(benchmarkInfo.filename, benchmarkInfo.level, 
                                         0, false, true, 0, e.getMessage());
            } finally {
                clearMemoryAfterExecution();
            }
        }
        
        private void writeSolution(MHS mhsSolver, String filename) {
            try {
                BenchmarkWriter.writeBenchmark(mhsSolver, filename, config.destDir);
            } catch (IOException e) {
                System.err.println("Failed to write solution: " + e.getMessage());
            }
        }
        
        private void clearMemoryBeforeExecution() {
            System.gc();
            sleepBriefly();
        }
        
        private void clearMemoryAfterExecution() {
            System.gc();
            sleepBriefly();
        }
        
        private void sleepBriefly() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Benchmark reporter and results writer
     */
    private static class BenchmarkReporter {
        private final BenchmarkConfig config;
        private final BenchmarkResults results;
        
        BenchmarkReporter(BenchmarkConfig config, BenchmarkResults results) {
            this.config = config;
            this.results = results;
        }
        
        void printStartHeader() {
            System.out.println("=".repeat(60));
            System.out.println("🚀 MULTI-LEVEL BENCHMARK EXECUTION");
            System.out.println("=".repeat(60));
        }
        
        void printLevelHeader(BenchmarkLevel level, int benchmarkCount) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println(String.format("🎯 LEVEL %s: %d benchmarks", level.name(), benchmarkCount));
            System.out.println("⏱️  Timeout: " + (level.getTimeoutMs() / 1000) + " seconds");
            System.out.println("📝 " + level.getDescription());
            System.out.println("=".repeat(50));
        }
        
        Path prepareLevelResultsFile(BenchmarkLevel level) throws IOException {
            Path dirPath = Paths.get(config.destDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            
            String filename = String.format("results_%s_%s.txt", 
                level.name().toLowerCase(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            Path resultFile = dirPath.resolve(filename);
            Files.deleteIfExists(resultFile);
            return Files.createFile(resultFile);
        }
        
        Path prepareSequentialResultsFile() throws IOException {
            Path resultsFile = Paths.get(config.destDir, "sequential_results.txt");
            Files.deleteIfExists(resultsFile);
            return Files.createFile(resultsFile);
        }
        
        void writeAndPrintResult(BenchmarkResult result, Path resultFile) {
            writeBenchmarkResult(result, resultFile);
            printBenchmarkResult(result);
        }
        
        void writeBenchmarkResult(BenchmarkResult result, Path resultFile) {
            try {
                String line = String.format("%s,%s,%d,%s,%s,%d,%s%n",
                    result.filename, result.level, result.executionTimeMs,
                    result.completed, result.interrupted, result.mhsCount,
                    result.error != null ? result.error.replace(",", ";") : "");
                
                Files.writeString(resultFile, line, StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.err.println("Failed to write result for " + result.filename);
            }
        }
        
        void printBenchmarkResult(BenchmarkResult result) {
            if (result.completed) {
                System.out.printf("✅ COMPLETED in %dms (MHS: %d)%n", 
                    result.executionTimeMs, result.mhsCount);
            } else if (result.interrupted) {
                System.out.printf("⏹️ TIMEOUT after %dms%n", result.executionTimeMs);
            } else {
                System.out.printf("❌ ERROR: %s%n", result.error);
            }
        }
        
        void printLevelSummary(BenchmarkLevel level) {
            List<BenchmarkResult> results = this.results.getResultsByLevel().get(level);
            
            long completed = results.stream().mapToLong(r -> r.completed ? 1 : 0).sum();
            long interrupted = results.stream().mapToLong(r -> r.interrupted ? 1 : 0).sum();
            double avgTime = results.stream().mapToDouble(r -> r.executionTimeMs).average().orElse(0);
            long totalTime = results.stream().mapToLong(r -> r.executionTimeMs).sum();
            
            System.out.println("\n📈 " + level.name() + " SUMMARY:");
            System.out.printf("   ✅ Completed: %d/%d%n", completed, results.size());
            System.out.printf("   ⏹️ Interrupted: %d/%d%n", interrupted, results.size());
            System.out.printf("   ⏱️ Average time: %.1fms%n", avgTime);
            System.out.printf("   🕒 Total time: %.1fs%n", totalTime / 1000.0);
        }
        
        void generateFinalReport() throws IOException {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("📊 FINAL COMPREHENSIVE REPORT");
            System.out.println("=".repeat(60));
            
            printOverallStatistics();
            printPerLevelBreakdown();
            writeDetailedReport();
        }
        
        private void printOverallStatistics() {
            long totalCompleted = results.getAllResults().stream().mapToLong(r -> r.completed ? 1 : 0).sum();
            long totalInterrupted = results.getAllResults().stream().mapToLong(r -> r.interrupted ? 1 : 0).sum();
            long totalTime = results.getAllResults().stream().mapToLong(r -> r.executionTimeMs).sum();
            
            System.out.printf("🎯 Total tests: %d%n", results.getAllResults().size());
            System.out.printf("✅ Completed: %d (%.1f%%)%n", 
                totalCompleted, 100.0 * totalCompleted / results.getAllResults().size());
            System.out.printf("⏹️ Interrupted: %d (%.1f%%)%n", 
                totalInterrupted, 100.0 * totalInterrupted / results.getAllResults().size());
            System.out.printf("⏱️ Total execution time: %.1f minutes%n", totalTime / 60000.0);
        }
        
        private void printPerLevelBreakdown() {
            for (BenchmarkLevel level : BenchmarkLevel.values()) {
                List<BenchmarkResult> results = this.results.getResultsByLevel().get(level);
                if (!results.isEmpty()) {
                    long levelCompleted = results.stream().mapToLong(r -> r.completed ? 1 : 0).sum();
                    long levelTime = results.stream().mapToLong(r -> r.executionTimeMs).sum();
                    
                    System.out.printf("\n📊 %s LEVEL:%n", level.name());
                    System.out.printf("   Tests: %d, Completed: %d, Time: %.1fs%n", 
                        results.size(), levelCompleted, levelTime / 1000.0);
                }
            }
        }
        
        private void writeDetailedReport() throws IOException {
            Path reportFile = Paths.get(config.destDir, "benchmark_report_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt");
            
            StringBuilder report = new StringBuilder();
            report.append("BENCHMARK EXECUTION REPORT\n");
            report.append("Generated: ").append(LocalDateTime.now()).append("\n");
            report.append("=".repeat(60)).append("\n\n");
            
            appendConfiguration(report);
            appendResultsByLevel(report);
            
            Files.writeString(reportFile, report.toString());
            System.out.printf("💾 Detailed report saved: %s%n", reportFile);
        }
        
        private void appendConfiguration(StringBuilder report) {
            report.append("CONFIGURATION:\n");
            report.append("Benchmark Directory: ").append(config.benchmarkDir).append("\n");
            report.append("Output Directory: ").append(config.destDir).append("\n");
            report.append("BitSet Type: ").append(config.bitSetType).append("\n");
            report.append("Level Filtering: ").append(config.enableLevelFiltering).append("\n");
            report.append("Enabled Levels: ").append(config.enabledLevels).append("\n\n");
        }
        
        private void appendResultsByLevel(StringBuilder report) {
            for (BenchmarkLevel level : BenchmarkLevel.values()) {
                List<BenchmarkResult> results = this.results.getResultsByLevel().get(level);
                if (!results.isEmpty()) {
                    report.append("LEVEL ").append(level.name()).append(":\n");
                    report.append("Timeout: ").append(level.getTimeoutMs()).append("ms\n");
                    
                    for (BenchmarkResult result : results) {
                        report.append(String.format("  %s: %s (%dms)%n", 
                            result.filename,
                            result.completed ? "COMPLETED" : (result.interrupted ? "TIMEOUT" : "ERROR"),
                            result.executionTimeMs));
                    }
                    report.append("\n");
                }
            }
        }
    }
}