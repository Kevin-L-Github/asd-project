package unibs.asd.fastbitset;

import unibs.asd.benchmarks.BenchmarkReader;

public class FastMain {

    public static final String BENCHMARK = "74181.020.matrix";

    public static void main(String[] args) {
        @SuppressWarnings("unused")
        boolean[][] benchmark = BenchmarkReader.readBenchmark("src\\mybenchmarks\\" + BENCHMARK);
        eMHS mhs;
        mhs = new eMHS(identityMatrix(20));
        mhs.run(6000_000);
        System.out.println("\nAlgorithm completed. Solutions found: " + mhs.getSolutions().size());
        System.out.println("Computation Time: " + mhs.getComputationTime());
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
