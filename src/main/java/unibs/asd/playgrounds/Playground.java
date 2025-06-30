package unibs.asd.playgrounds;

import unibs.asd.benchmarks.BenchmarkReader;
import unibs.asd.enums.BitSetType;
import unibs.asd.mhs.BaseMHS;

public class Playground {

    /*
     * Il tempo limite deve essere espresso in secondi
     */
    public static final String BENCHMARK = "74181.020.matrix";
    public static final long TIMEOUT = 600;

    public static void main(String[] args) {
        @SuppressWarnings("unused")
        boolean[][] benchmark = BenchmarkReader.readBenchmark("src\\mybenchmarks\\" + BENCHMARK);
        BaseMHS mhs;
        mhs = new BaseMHS(IdentityMatrix.create(16));
        mhs.run(BitSetType.BOOLS_ARRAY, TIMEOUT * 1000);
        System.out.println("\nAlgorithm completed. Solutions found: " + mhs.getSolutions().size());
        System.out.println("Computation Time: " + mhs.getComputationTime());
        System.out.println(mhs.getSolutions());
    }

}
