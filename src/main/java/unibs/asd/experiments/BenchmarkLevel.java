package unibs.asd.experiments;

/**
 * Represents a benchmarking level with specific timeout settings.
 * Used to organize benchmarks into different timeout categories.
 */
public class BenchmarkLevel {
    private final int timeoutMs;
    private final String destDir;
    private final String solvedFilename;
    
    /**
     * Constructs a new BenchmarkLevel.
     * 
     * @param timeoutMs the timeout in milliseconds for this level
     * @param destDir the destination directory for results
     * @param solvedFilename the filename to store solved benchmarks
     */
    public BenchmarkLevel(int timeoutMs, String destDir, String solvedFilename) {
        this.timeoutMs = timeoutMs;
        this.destDir = destDir;
        this.solvedFilename = solvedFilename;
    }
    
    /**
     * @return the timeout in milliseconds for this level
     */
    public int getTimeoutMs() {
        return timeoutMs;
    }
    
    /**
     * @return the destination directory for results
     */
    public String getDestDir() {
        return destDir;
    }
    
    /**
     * @return the filename for storing solved benchmarks
     */
    public String getSolvedFilename() {
        return solvedFilename;
    }
    
    /**
     * @return the full path to the solved benchmarks file
     */
    public String getSolvedFilePath() {
        return destDir + "\\" + solvedFilename;
    }
}