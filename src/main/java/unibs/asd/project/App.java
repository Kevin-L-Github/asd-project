package unibs.asd.project;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        // BenchmarkReader.readBenchmark("src\\benchmarks1\\74L85.000.matrix");
        boolean[] bin = {false, false, true, true};
        Hypothesis hypothesis = new Hypothesis(bin);
        System.out.println("Ipotesi: " + hypothesis.toString());
        System.out.println("Dimensione dell'ipotesi: " + hypothesis.size());
        System.out.println("Numero di successori: " + hypothesis.numberOfSuccessors());
        System.out.println("Numero di predecessori: " + hypothesis.numberOfPredecessors());
        System.out.println("h seconds: " + hypothesis.getHseconds().toString());
    }
}
