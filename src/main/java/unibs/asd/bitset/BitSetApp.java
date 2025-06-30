package unibs.asd.bitset;

import unibs.asd.benchmarks.BenchmarkReader;
import unibs.asd.bools.BoolsMHS;
import unibs.asd.enums.BitSetType;
import unibs.asd.fastbitset.eMHS;
import unibs.asd.playgrounds.IdentityMatrix;
import unibs.asd.roaringbitmap.RoaringMHS;

@SuppressWarnings("unused")
public class BitSetApp {

    public static final String BENCHMARK = "74181.020.matrix";
    public static final long TIMEOUT = 60; // secondi

    public static void main(String[] args) {
        boolean[][] benchmark = BenchmarkReader.readBenchmark("src\\mybenchmarks\\" + BENCHMARK);
        BitSetMHS mhs;
        mhs = new BitSetMHS(IdentityMatrix.create(6));
        mhs.run(BitSetType.BITSET, TIMEOUT * 1000);
        System.out.println("\nAlgorithm completed. Solutions found: " + mhs.getSolutions().size());
        System.out.println("Computation Time: " + mhs.getComputationTime());
        System.out.println(mhs.getSolutions());
    }

}
