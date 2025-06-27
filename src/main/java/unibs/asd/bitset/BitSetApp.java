package unibs.asd.bitset;

import unibs.asd.project.BenchmarkReader;
import unibs.asd.project.MHS;
import unibs.asd.roaring.RoaringMHS;

@SuppressWarnings("unused")
public class BitSetApp {

    public static final String BENCHMARK = "74182.042.matrix";

    public static void main(String[] args) {
        boolean[][] benchmark = BenchmarkReader.readBenchmark("src\\benchmarks1\\"+BENCHMARK);
        FastMHS mhs;
        mhs = new FastMHS(identityMatrix(17));
        mhs.run(480_000);
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
