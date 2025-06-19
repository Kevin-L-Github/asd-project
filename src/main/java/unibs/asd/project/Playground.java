package unibs.asd.project;

/**
 * Playground class to test the MHS algorithm with a benchmark instance.
 * It reads a benchmark matrix from a file, runs the MHS algorithm,
 * and prints the found solutions.
 */
public class Playground {

    public static final String BENCHMARK_FILE = "src\\benchmarks1\\example.matrix";
    public static void main(String[] args) {

        boolean[][] instance;
        instance = BenchmarkReader.readBenchmark(BENCHMARK_FILE);
        MHS mhs = new MHS(instance);
        mhs.run();

        System.out.println("Found " + mhs.getSolutions().size() + " solutions.");
        for (Hypothesis hs : mhs.getSolutions()) {
            System.out.println(hs);
        }
        System.out.println("Done.");
    }
}
