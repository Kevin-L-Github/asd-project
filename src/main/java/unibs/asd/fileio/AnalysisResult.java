package unibs.asd.fileio;

public class AnalysisResult {
    private final String filename;
    private final int numSolutions;
    private final String executionTime;
    private final String status;
    private final String stopReason;

    public AnalysisResult(String filename, int numSolutions, String executionTime, String status, String stopReason) {
        this.filename = filename;
        this.numSolutions = numSolutions;
        this.executionTime = executionTime;
        this.status = status;
        this.stopReason = stopReason;
    }

    public String getFilename() {
        return filename;
    }

    public int getNumSolutions() {
        return numSolutions;
    }

    public String getExecutionTime() {
        return executionTime;
    }

    public String getStatus() {
        return status;
    }

    public String getStopReason() {
        return stopReason;
    }

    @Override
    public String toString() {
        return "File: " + filename +
                "\nSoluzioni trovate: " + numSolutions +
                "\nTempo di esecuzione: " + executionTime +
                "\nStato: " + status +
                (stopReason != null ? "\nMotivo stop: " + stopReason : "");
    }
}
