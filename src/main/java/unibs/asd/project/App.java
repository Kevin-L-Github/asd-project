package unibs.asd.project;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        // BenchmarkReader.readBenchmark("src\\benchmarks1\\74L85.000.matrix");
        boolean[] bin = { false, false, true, true };
        Hypothesis hypothesis = new Hypothesis(bin);
        System.out.println("Ipotesi: " + hypothesis.toString());
        System.out.println("Dimensione dell'ipotesi: " + hypothesis.size());
        System.out.println("Numero di successori: " + hypothesis.numberOfSuccessors());
        System.out.println("Numero di predecessori: " + hypothesis.numberOfPredecessors());

        if (!bin[0]) {
            System.out.println("h seconds: " + hypothesis.getHseconds().toString());
        } else {
            System.out.println("h seconds non computabili");
        }

        System.out.println("Global initial " + hypothesis.globalInitial().toString());
        Hypothesis h_prime = new Hypothesis(hypothesis.leftSuccessors().get(0));
        System.out.println("h' " + h_prime.toString());
        System.out.println("Initial " + hypothesis.initial(h_prime).toString());
        System.out.println("Final " + hypothesis.finalPred(h_prime).toString());
    }
}
