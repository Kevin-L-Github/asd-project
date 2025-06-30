package unibs.asd.roaring;

import unibs.asd.benchmarks.BenchmarkReader;

public class MainRoaring {
    public static final String BENCHMARK = "74L85.000.matrix";

    public static void main(String[] args) {
        boolean[][] benchmark = BenchmarkReader.readBenchmark("src\\benchmarks1\\"+ BENCHMARK);
        RoaringMHS mhs;

        mhs = new RoaringMHS(benchmark);
        mhs.run(300_000);
    }

    public static boolean[][] identityMatrix(int size) {
        boolean[][] matrix = new boolean[size][size];
        for (int i = 0; i < size; i++) {
            matrix[i][i] = true;
        }
        return matrix;
    }
}
