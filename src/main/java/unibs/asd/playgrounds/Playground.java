package unibs.asd.playgrounds;

import unibs.asd.benchmarks.BenchmarkReader;
import unibs.asd.bools.BoolsMHS;

public class Playground {

    public static final String BENCHMARK = "c7552.325.matrix";

    public static void main(String[] args) {
        boolean[][] benchmark = null;
        benchmark = BenchmarkReader.readBenchmark("src\\benchmarks1\\" + BENCHMARK);

        benchmark = identityMatrix(17);
        BoolsMHS mhs;
        mhs = new BoolsMHS(benchmark);
        mhs.run(180_000);
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
