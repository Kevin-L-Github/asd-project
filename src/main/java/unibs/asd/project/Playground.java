package unibs.asd.project;

public class Playground {

    public static final String BENCHMARK = "74L85.000.matrix";

    public static void main(String[] args) {
        boolean[][] benchmark = BenchmarkReader.readBenchmark("src\\benchmarks1\\" + BENCHMARK);

        MHS mhs;
        mhs = new MHS(benchmark);
        mhs.run(18_000_000);
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
