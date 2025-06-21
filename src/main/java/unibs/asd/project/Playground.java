package unibs.asd.project;


public class Playground {

    public static final String BENCHMARK = "74L85.000.matrix";
    
    public static void main(String[] args) {
        boolean[][] benchmark = null;
            benchmark = BenchmarkReader.readBenchmark("src\\benchmarks1\\"+BENCHMARK);
        

        MHS mhs;
        mhs = new MHS(benchmark);
        mhs.run(30_000);
    }

    public static boolean[][] identityMatrix(int size) {
        boolean[][] matrix = new boolean[size][size];
        for (int i = 0; i < size; i++) {
            matrix[i][i] = true;
        }
        return matrix;
    }

}
