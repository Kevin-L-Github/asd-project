package unibs.asd.playgrounds;

import unibs.asd.benchmarks.BenchmarkReader;
import unibs.asd.benchmarks.BenchmarkWriter;
import unibs.asd.enums.BitSetType;
import unibs.asd.fastbitset.eMHS;
import unibs.asd.mhs.BaseMHS;
import unibs.asd.mhs.MegaMHS;

public class Playground {

    /*
     * Il tempo limite deve essere espresso in secondi
     */
    public static final String BENCHMARK = "74L85.026.matrix";
    public static final long TIMEOUT = 120;

    public static void main(String[] args) {
        boolean[][] benchmark = BenchmarkReader.readBenchmark("src\\mybenchmarks\\" + BENCHMARK);
        MegaMHS mhs;
        mhs = new MegaMHS(benchmark);
        mhs.run(TIMEOUT * 1000);
        System.out.println("\nAlgorithm completed. Solutions found: " + mhs.getSolutions().size());
        System.out.println("Computation Time: " + mhs.getComputationTime());
        System.out.println(mhs.getSolutions());

        //BenchmarkWriter.writeBenchmark(mhs, BENCHMARK, "results");
    }

}
