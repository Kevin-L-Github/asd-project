package unibs.asd.project;

import java.util.List;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        boolean[][] instance = BenchmarkReader.readBenchmark("src\\benchmarks1\\74L85.000.matrix");

        MHS mhs = new MHS(instance);
        mhs.run();

        System.out.println("Found " + mhs.getSolutions().size() + " solutions.");
        for (Hypothesis h : mhs.getSolutions()) {
            System.out.println(h);
        }
        System.out.println("Done.");
    }
}
