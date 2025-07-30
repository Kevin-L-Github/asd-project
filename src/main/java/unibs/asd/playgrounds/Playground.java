package unibs.asd.playgrounds;

import unibs.asd.benchmarks.BenchmarkReader;
import unibs.asd.enums.BitSetType;
import unibs.asd.interfaces.MHS;
import unibs.asd.mhs.BoostMHS;

public class Playground {

    /*
     * Il tempo limite deve essere espresso in secondi
     */
    public static final String BENCHMARK = "c7552.311.matrix";
    public static final long TIMEOUT = 300;

    public static void main(String[] args) {
        boolean[][] benchmark = BenchmarkReader.readBenchmark("src\\mybenchmarks\\" + BENCHMARK);
        MHS mhs;
        mhs = new BoostMHS(benchmark);
        mhs.run(BitSetType.FAST_BITSET, TIMEOUT * 1000);
        System.out.println("\nAlgorithm completed. Solutions found: " +
                mhs.getSolutions().size());
        System.out.println("Computation Time: " + mhs.getComputationTime());
        System.out.println(mhs.getSolutions());
    }

}
