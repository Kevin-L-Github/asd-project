package unibs.asd.playgrounds;

import unibs.asd.benchmarks.BenchmarkReader;
import unibs.asd.roaringbitmap.RoaringMHS;

public class RoaringPlayground {
    public static final String BENCHMARK = "74L85.000.matrix";
    public static void main(String[] args) {
        boolean[][] benchmark = BenchmarkReader.readBenchmark("src\\benchmarks1\\"+ BENCHMARK);
        RoaringMHS mhs;

        mhs = new RoaringMHS(benchmark);
        mhs.run(300_000);
    }

}
