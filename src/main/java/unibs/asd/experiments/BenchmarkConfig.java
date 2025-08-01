package unibs.asd.experiments;

import java.io.*;
import java.util.*;

/**
 * Configuration class for benchmark execution with customizable parameters
 */
public class BenchmarkConfig {
    
    // Default timeout values (in milliseconds)
    public static final int DEFAULT_QUICK_TIMEOUT = 30_000;      // 30 seconds
    public static final int DEFAULT_MEDIUM_TIMEOUT = 300_000;    // 5 minutes
    public static final int DEFAULT_INTENSIVE_TIMEOUT = 1_800_000; // 30 minutes
    
    // Classification thresholds
    private int smallMatrixThreshold = 100;      // rows * cols < 100 = small
    private int mediumMatrixThreshold = 1000;    // rows * cols < 1000 = medium
    private int largeMatrixThreshold = 5000;     // rows * cols < 5000 = large
    
    private double sparseDensityThreshold = 0.05; // density < 0.05 = sparse
    private double mediumDensityThreshold = 0.2;  // density < 0.2 = medium density
    private double denseDensityThreshold = 0.3;   // density >= 0.3 = dense
    
    // Timeout customization
    private int quickTimeoutMs = DEFAULT_QUICK_TIMEOUT;
    private int mediumTimeoutMs = DEFAULT_MEDIUM_TIMEOUT;
    private int intensiveTimeoutMs = DEFAULT_INTENSIVE_TIMEOUT;
    
    // Output configuration
    private boolean enableDetailedLogging = true;
    private boolean enableProgressReports = true;
    private boolean enableMemoryMonitoring = false;
    private boolean saveIntermediateResults = true;
    
    // Execution configuration
    private boolean enableLevelFiltering = true;
    private Set<NewFilterBenchmarks.BenchmarkLevel> enabledLevels = EnumSet.allOf(NewFilterBenchmarks.BenchmarkLevel.class);
    private boolean stopOnFirstFailure = false;
    private int maxConcurrentExecutions = 1; // Future: for parallel execution
    
    /**
     * Default constructor with standard settings
     */
    public BenchmarkConfig() {
        // Use default values
    }
    
    /**
     * Load configuration from properties file
     */
    public static BenchmarkConfig fromFile(String configPath) throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configPath)) {
            props.load(fis);
        }
        
        BenchmarkConfig config = new BenchmarkConfig();
        
        // Load thresholds
        config.smallMatrixThreshold = Integer.parseInt(
            props.getProperty("threshold.small_matrix", "100"));
        config.mediumMatrixThreshold = Integer.parseInt(
            props.getProperty("threshold.medium_matrix", "1000"));
        config.largeMatrixThreshold = Integer.parseInt(
            props.getProperty("threshold.large_matrix", "5000"));
        
        config.sparseDensityThreshold = Double.parseDouble(
            props.getProperty("threshold.sparse_density", "0.05"));
        config.mediumDensityThreshold = Double.parseDouble(
            props.getProperty("threshold.medium_density", "0.2"));
        config.denseDensityThreshold = Double.parseDouble(
            props.getProperty("threshold.dense_density", "0.3"));
        
        // Load timeouts
        config.quickTimeoutMs = Integer.parseInt(
            props.getProperty("timeout.quick_ms", String.valueOf(DEFAULT_QUICK_TIMEOUT)));
        config.mediumTimeoutMs = Integer.parseInt(
            props.getProperty("timeout.medium_ms", String.valueOf(DEFAULT_MEDIUM_TIMEOUT)));
        config.intensiveTimeoutMs = Integer.parseInt(
            props.getProperty("timeout.intensive_ms", String.valueOf(DEFAULT_INTENSIVE_TIMEOUT)));
        
        // Load execution options
        config.enableDetailedLogging = Boolean.parseBoolean(
            props.getProperty("output.detailed_logging", "true"));
        config.enableProgressReports = Boolean.parseBoolean(
            props.getProperty("output.progress_reports", "true"));
        config.enableMemoryMonitoring = Boolean.parseBoolean(
            props.getProperty("output.memory_monitoring", "false"));
        config.saveIntermediateResults = Boolean.parseBoolean(
            props.getProperty("output.save_intermediate", "true"));
        
        config.enableLevelFiltering = Boolean.parseBoolean(
            props.getProperty("execution.level_filtering", "true"));
        config.stopOnFirstFailure = Boolean.parseBoolean(
            props.getProperty("execution.stop_on_failure", "false"));
        
        // Load enabled levels
        String enabledLevelsStr = props.getProperty("execution.enabled_levels", "QUICK,MEDIUM,INTENSIVE");
        config.enabledLevels = parseEnabledLevels(enabledLevelsStr);
        
        return config;
    }
    
    /**
     * Save current configuration to properties file
     */
    public void saveToFile(String configPath) throws IOException {
        Properties props = new Properties();
        
        // Thresholds
        props.setProperty("threshold.small_matrix", String.valueOf(smallMatrixThreshold));
        props.setProperty("threshold.medium_matrix", String.valueOf(mediumMatrixThreshold));
        props.setProperty("threshold.large_matrix", String.valueOf(largeMatrixThreshold));
        props.setProperty("threshold.sparse_density", String.valueOf(sparseDensityThreshold));
        props.setProperty("threshold.medium_density", String.valueOf(mediumDensityThreshold));
        props.setProperty("threshold.dense_density", String.valueOf(denseDensityThreshold));
        
        // Timeouts
        props.setProperty("timeout.quick_ms", String.valueOf(quickTimeoutMs));
        props.setProperty("timeout.medium_ms", String.valueOf(mediumTimeoutMs));
        props.setProperty("timeout.intensive_ms", String.valueOf(intensiveTimeoutMs));
        
        // Output options
        props.setProperty("output.detailed_logging", String.valueOf(enableDetailedLogging));
        props.setProperty("output.progress_reports", String.valueOf(enableProgressReports));
        props.setProperty("output.memory_monitoring", String.valueOf(enableMemoryMonitoring));
        props.setProperty("output.save_intermediate", String.valueOf(saveIntermediateResults));
        
        // Execution options
        props.setProperty("execution.level_filtering", String.valueOf(enableLevelFiltering));
        props.setProperty("execution.stop_on_failure", String.valueOf(stopOnFirstFailure));
        props.setProperty("execution.enabled_levels", formatEnabledLevels(enabledLevels));
        
        try (FileOutputStream fos = new FileOutputStream(configPath)) {
            props.store(fos, "Benchmark Configuration");
        }
    }
    
    /**
     * Create a configuration optimized for debugging (quick tests only, detailed logging)
     */
    public static BenchmarkConfig forDebugging() {
        BenchmarkConfig config = new BenchmarkConfig();
        config.enabledLevels = EnumSet.of(NewFilterBenchmarks.BenchmarkLevel.QUICK);
        config.quickTimeoutMs = 10_000; // Reduced to 10 seconds for debugging
        config.enableDetailedLogging = true;
        config.enableProgressReports = true;
        config.stopOnFirstFailure = true;
        return config;
    }
    
    /**
     * Create a configuration optimized for performance evaluation
     */
    public static BenchmarkConfig forPerformanceEvaluation() {
        BenchmarkConfig config = new BenchmarkConfig();
        config.enabledLevels = EnumSet.allOf(NewFilterBenchmarks.BenchmarkLevel.class);
        config.intensiveTimeoutMs = 3_600_000; // Extended to 1 hour for performance tests
        config.enableMemoryMonitoring = true;
        config.enableDetailedLogging = true;
        config.saveIntermediateResults = true;
        return config;
    }
    
    /**
     * Create a configuration for quick validation (all levels with reduced timeouts)
     */
    public static BenchmarkConfig forQuickValidation() {
        BenchmarkConfig config = new BenchmarkConfig();
        config.quickTimeoutMs = 5_000;    // 5 seconds
        config.mediumTimeoutMs = 30_000;  // 30 seconds
        config.intensiveTimeoutMs = 180_000; // 3 minutes
        config.enableProgressReports = false;
        config.enableDetailedLogging = false;
        return config;
    }
    
    /**
     * Determine benchmark level based on matrix characteristics and current thresholds
     */
    public NewFilterBenchmarks.BenchmarkLevel classifyBenchmark(int rows, int cols, double density) {
        int sizeFactor = rows * cols;
        
        // Quick level: small matrices or very sparse matrices
        if (sizeFactor < smallMatrixThreshold || 
           (density < sparseDensityThreshold && sizeFactor < mediumMatrixThreshold)) {
            return NewFilterBenchmarks.BenchmarkLevel.QUICK;
        }
        
        // Intensive level: large dense matrices or very large matrices
        if ((sizeFactor > largeMatrixThreshold && density > mediumDensityThreshold) || 
            sizeFactor > largeMatrixThreshold * 2) {
            return NewFilterBenchmarks.BenchmarkLevel.INTENSIVE;
        }
        
        // Medium level: everything else
        return NewFilterBenchmarks.BenchmarkLevel.MEDIUM;
    }
    
    /**
     * Get timeout for a specific level
     */
    public int getTimeoutForLevel(NewFilterBenchmarks.BenchmarkLevel level) {
        return switch (level) {
            case QUICK -> quickTimeoutMs;
            case MEDIUM -> mediumTimeoutMs;
            case INTENSIVE -> intensiveTimeoutMs;
        };
    }
    
    // Helper methods
    private static Set<NewFilterBenchmarks.BenchmarkLevel> parseEnabledLevels(String levelsStr) {
        Set<NewFilterBenchmarks.BenchmarkLevel> levels = EnumSet.noneOf(FilterBenchmarks.BenchmarkLevel.class);
        
        for (String levelName : levelsStr.split(",")) {
            try {
                levels.add(NewFilterBenchmarks.BenchmarkLevel.valueOf(levelName.trim()));
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid level name: " + levelName);
            }
        }
        
        return levels.isEmpty() ? EnumSet.allOf(NewFilterBenchmarks.BenchmarkLevel.class) : levels;
    }
    
    private static String formatEnabledLevels(Set<NewFilterBenchmarks.BenchmarkLevel> levels) {
        return String.join(",", levels.stream()
            .map(Enum::name)
            .toArray(String[]::new));
    }
    
    // Getters and setters
    public int getSmallMatrixThreshold() { return smallMatrixThreshold; }
    public void setSmallMatrixThreshold(int threshold) { this.smallMatrixThreshold = threshold; }
    
    public int getMediumMatrixThreshold() { return mediumMatrixThreshold; }
    public void setMediumMatrixThreshold(int threshold) { this.mediumMatrixThreshold = threshold; }
    
    public int getLargeMatrixThreshold() { return largeMatrixThreshold; }
    public void setLargeMatrixThreshold(int threshold) { this.largeMatrixThreshold = threshold; }
    
    public double getSparseDensityThreshold() { return sparseDensityThreshold; }
    public void setSparseDensityThreshold(double threshold) { this.sparseDensityThreshold = threshold; }
    
    public double getMediumDensityThreshold() { return mediumDensityThreshold; }
    public void setMediumDensityThreshold(double threshold) { this.mediumDensityThreshold = threshold; }
    
    public double getDenseDensityThreshold() { return denseDensityThreshold; }
    public void setDenseDensityThreshold(double threshold) { this.denseDensityThreshold = threshold; }
    
    public int getQuickTimeoutMs() { return quickTimeoutMs; }
    public void setQuickTimeoutMs(int timeout) { this.quickTimeoutMs = timeout; }
    
    public int getMediumTimeoutMs() { return mediumTimeoutMs; }
    public void setMediumTimeoutMs(int timeout) { this.mediumTimeoutMs = timeout; }
    
    public int getIntensiveTimeoutMs() { return intensiveTimeoutMs; }
    public void setIntensiveTimeoutMs(int timeout) { this.intensiveTimeoutMs = timeout; }
    
    public boolean isDetailedLoggingEnabled() { return enableDetailedLogging; }
    public void setDetailedLoggingEnabled(boolean enabled) { this.enableDetailedLogging = enabled; }
    
    public boolean isProgressReportsEnabled() { return enableProgressReports; }
    public void setProgressReportsEnabled(boolean enabled) { this.enableProgressReports = enabled; }
    
    public boolean isMemoryMonitoringEnabled() { return enableMemoryMonitoring; }
    public void setMemoryMonitoringEnabled(boolean enabled) { this.enableMemoryMonitoring = enabled; }
    
    public boolean isSaveIntermediateResultsEnabled() { return saveIntermediateResults; }
    public void setSaveIntermediateResultsEnabled(boolean enabled) { this.saveIntermediateResults = enabled; }
    
    public boolean isLevelFilteringEnabled() { return enableLevelFiltering; }
    public void setLevelFilteringEnabled(boolean enabled) { this.enableLevelFiltering = enabled; }
    
    public Set<NewFilterBenchmarks.BenchmarkLevel> getEnabledLevels() { return enabledLevels; }
    public void setEnabledLevels(Set<NewFilterBenchmarks.BenchmarkLevel> levels) { this.enabledLevels = levels; }
    
    public boolean isStopOnFirstFailureEnabled() { return stopOnFirstFailure; }
    public void setStopOnFirstFailureEnabled(boolean enabled) { this.stopOnFirstFailure = enabled; }
    
    public int getMaxConcurrentExecutions() { return maxConcurrentExecutions; }
    public void setMaxConcurrentExecutions(int max) { this.maxConcurrentExecutions = max; }
    
    @Override
    public String toString() {
        return String.format("BenchmarkConfig{" +
            "thresholds=[%d,%d,%d], " +
            "densities=[%.2f,%.2f,%.2f], " +
            "timeouts=[%d,%d,%d]ms, " +
            "levels=%s}",
            smallMatrixThreshold, mediumMatrixThreshold, largeMatrixThreshold,
            sparseDensityThreshold, mediumDensityThreshold, denseDensityThreshold,
            quickTimeoutMs, mediumTimeoutMs, intensiveTimeoutMs,
            enabledLevels);
    }
}