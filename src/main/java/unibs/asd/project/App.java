package unibs.asd.project;

import java.util.List;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        boolean[][] instance = BenchmarkReader.readBenchmark("src\\benchmarks1\\74L85.000.matrix");

        System.out.println(instance.length + " rows, " + instance[0].length + " columns");
        MHS mhs = new MHS();
        List<Hypothesis> solutions = mhs.run(instance);
        System.out.println("Found " + solutions.size() + " solutions.");
        for (Hypothesis h : solutions) {
            System.out.println(h);
        }
        System.out.println("Done.");
    }
}
