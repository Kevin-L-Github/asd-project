package unibs.asd.playgrounds;

import java.io.IOException;

import unibs.asd.enums.BitSetType;
import unibs.asd.fileio.BenchmarkReader;
import unibs.asd.fileio.BenchmarkWriter;
import unibs.asd.interfaces.MHS;
import unibs.asd.mhs.BaseMHS;
import unibs.asd.mhs.BoostMHS;

public class Playground {

    /*
     * Il tempo limite deve essere espresso in secondi
     */
    public static final String BENCHMARK = "74L85.000.matrix";
    public static final long TIMEOUT = 60;

    public static void main(String[] args) throws IOException {
        boolean[][] benchmark = BenchmarkReader.readBenchmark("src\\mybenchmarks\\" + BENCHMARK);
        MHS mhs;
        mhs = new BoostMHS(benchmark);
        mhs.run(BitSetType.FAST_BITSET, TIMEOUT * 1000);
        System.out.println("Solutions found: " + mhs.getSolutions().size());
        //System.out.println("Solutions: " + mhs.getSolutions());
        System.out.println("Tempo di esecuzione: " + mhs.getComputationTime()/1_000_000_000F + " secondi");
        BenchmarkWriter.writeBenchmark(mhs, BENCHMARK, "results");
    }

}
