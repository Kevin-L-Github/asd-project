package unibs.asd.playgrounds;

import java.io.IOException;

import unibs.asd.enums.BitSetType;
import unibs.asd.fileio.BenchmarkReader;
import unibs.asd.fileio.BenchmarkWriter;
import unibs.asd.interfaces.MHS;
import unibs.asd.mhs.BoostMHS;

public class Playground {

    /*
     * Il tempo limite deve essere espresso in secondi
     */
    public static final String BENCHMARK = "74L85.026.matrix";
    public static final long TIMEOUT = 300;

    public static void main(String[] args) throws IOException {
        boolean[][] benchmark = BenchmarkReader.readBenchmark("src\\mybenchmarks\\" + BENCHMARK);
        MHS mhs;
        //benchmark = IdentityMatrix.create(25000);
        mhs = new BoostMHS(benchmark);
        mhs.run(BitSetType.FAST_BITSET, TIMEOUT * 1000);
        System.out.println("Computation Time: " + mhs.getComputationTime());
        System.out.println("Out of Memory Error: " + mhs.isOutOfMemoryError());
        System.out.println("Executed: " + mhs.isExecuted());
        System.out.println("Stopped: " + mhs.isStopped());
        BenchmarkWriter.writeBenchmark(mhs, BENCHMARK, "results");
    }

}
