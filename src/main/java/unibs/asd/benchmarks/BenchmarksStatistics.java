package unibs.asd.benchmarks;

public class BenchmarksStatistics {
    private String benchmarkName;
    private float sparsityIndex;
    private int rows;
    private int colomuns;
    private int emptyColumns;
    private float avarageOnePerRow;
    private float maxOnePerRow;
    private float minOnePerRow;
    private float distribution;
    
    public BenchmarksStatistics(String benchmarkName, float sparsityIndex, int rows, int colomuns, int emptyColumns,
            float avarageOnePerRow, float maxOnePerRow, float minOnePerRow, float distribution) {
        this.benchmarkName = benchmarkName;
        this.sparsityIndex = sparsityIndex;
        this.rows = rows;
        this.colomuns = colomuns;
        this.emptyColumns = emptyColumns;
        this.avarageOnePerRow = avarageOnePerRow;
        this.maxOnePerRow = maxOnePerRow;
        this.minOnePerRow = minOnePerRow;
        this.distribution = distribution;
    }

    public String getBenchmarkName() {
        return benchmarkName;
    }

    public float getSparsityIndex() {
        return sparsityIndex;
    }

    public int getRows() {
        return rows;
    }

    public int getColomuns() {
        return colomuns;
    }

    public int getEmptyColumns() {
        return emptyColumns;
    }

    public float getAvarageOnePerRow() {
        return avarageOnePerRow;
    }

    public float getMaxOnePerRow() {
        return maxOnePerRow;
    }

    public float getMinOnePerRow() {
        return minOnePerRow;
    }

    public float getDistribution() {
        return distribution;
    }

}
