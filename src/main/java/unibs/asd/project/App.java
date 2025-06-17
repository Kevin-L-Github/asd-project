package unibs.asd.project;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        
        boolean[][] instance = BenchmarkReader.readBenchmark("src\\benchmarks1\\74182.043.matrix");
        //instance = BenchmarkReader.readBenchmark("src\\benchmarks1\\mybenchmark.matrix");
        MHS mhs = new MHS(instance);
        mhs.run();

        System.out.println("Found " + mhs.getSolutions().size() + " solutions.");
        for (Hypothesis h : mhs.getSolutions()) {
            System.out.println(h);
        }
        System.out.println("Done.");
    }
}
