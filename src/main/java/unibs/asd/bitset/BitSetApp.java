package unibs.asd.bitset;

import unibs.asd.project.BenchmarkReader;
import unibs.asd.project.MHS;

public class BitSetApp {

    public static final String BENCHMARK = "74L85.000.matrix";

    public static void main(String[] args) {
        boolean[][] benchmark = null;
        //benchmark = BenchmarkReader.readBenchmark("src\\benchmarks1\\" + BENCHMARK);
        benchmark = identityMatrix(18);
        MHS mhs;
        mhs = new MHS(benchmark);
        mhs.run(60_000);
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
