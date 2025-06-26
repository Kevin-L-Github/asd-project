package unibs.asd.bitset;

import unibs.asd.project.BenchmarkReader;

public class BitSetApp {

    public static final String BENCHMARK = "c7552.325.matrix";

    public static void main(String[] args) {
        boolean[][] benchmark = BenchmarkReader.readBenchmark("src\\benchmarks1\\"+BENCHMARK);
        FastMHS mhs;

        mhs = new FastMHS(benchmark);
        mhs.run(300_000);
        System.out.println(mhs.getSolutions());
    }

    public static boolean[][] identityMatrix(int size) {
        boolean[][] matrix = new boolean[size][size];
        for (int i = 0; i < size; i++) {
            matrix[i][i] = true;
        }
        return matrix;
    }

}
