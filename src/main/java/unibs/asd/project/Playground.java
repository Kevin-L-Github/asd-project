package unibs.asd.project;

public class Playground {

    public static final String BENCHMARK = "c7552.228.matrix";

    public static void main(String[] args) {
        boolean[][] benchmark = null;
        //benchmark = BenchmarkReader.readBenchmark("src\\benchmarks1\\" + BENCHMARK);

        benchmark = identityMatrix(16);
        MHS_LL mhs;
        mhs = new MHS_LL(benchmark);
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
