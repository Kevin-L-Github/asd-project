package unibs.asd.playgrounds;

import unibs.asd.benchmarks.BenchmarkReader;
import unibs.asd.enums.BitSetType;
import unibs.asd.fastbitset.eMHS;
import unibs.asd.mhs.BaseMHS;

public class Playground {

    /*
     * Il tempo limite deve essere espresso in secondi
     */
    public static final String BENCHMARK = "74L85.000.matrix";
    public static final long TIMEOUT = 600;

    public static void main(String[] args) {
        boolean[][] benchmark = BenchmarkReader.readBenchmark("src\\mybenchmarks\\" + BENCHMARK);
        eMHS mhs;
        mhs = new eMHS(IdentityMatrix.create(22));
        mhs.run(TIMEOUT * 1000);
        System.out.println("\nAlgorithm completed. Solutions found: " + mhs.getSolutions().size());
        System.out.println("Computation Time: " + mhs.getComputationTime());
        System.out.println(mhs.getSolutions());
    }

}
