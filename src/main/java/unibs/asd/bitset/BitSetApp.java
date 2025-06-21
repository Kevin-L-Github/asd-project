package unibs.asd.bitset;

import java.util.BitSet;

import unibs.asd.project.BenchmarkReader;

public class BitSetApp {

    public static final String BENCHMARK = "c7552.228.matrix";

    public static void main(String[] args) {

        boolean[][] benchmark = null;
        benchmark = identityMatrix(18);
        BitSetMHS mhs;
        mhs = new BitSetMHS(benchmark);
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
