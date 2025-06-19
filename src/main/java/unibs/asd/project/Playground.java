package unibs.asd.project;

/**
 * Playground class to test the MHS algorithm with a benchmark instance.
 * It reads a benchmark matrix from a file, runs the MHS algorithm,
 * and prints the found solutions.
 */
public class Playground {

    public static final String BENCHMARK_FILE = "src\\benchmarks1\\74283.007.matrix";

    public static void main(String[] args) {

        boolean[][] instance;
        instance = identityMatrix(15);
        MHS mhs = new MHS(instance);
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
