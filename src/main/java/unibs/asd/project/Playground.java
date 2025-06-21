package unibs.asd.project;

/**
 * Playground class to test the MHS algorithm with a benchmark instance.
 * It reads a benchmark matrix from a file, runs the MHS algorithm,
 * and prints the found solutions. 74182.042
 */
public class Playground {

    public static final String BENCHMARK = "src\\benchmarks1\\74L85.026.matrix";

    public static void main(String[] args) {

        boolean[][] benchmark = null;
            benchmark = BenchmarkReader.readBenchmark(BENCHMARK);

        MHS mhs;
        mhs = new MHS(benchmark);
        mhs.run();

        System.out.println("Found " + mhs.getSolutions().size() + " solutions.");
        for (Hypothesis hs : mhs.getSolutions()) {
            System.out.println(hs);
        }
        System.out.println("Done.");
    }

    public static boolean[][] identityMatrix(int size) {
        boolean[][] matrix = new boolean[size][size];
        for (int i = 0; i < size; i++) {
            matrix[i][i] = true;
        }
        return matrix;
    }

}
