package unibs.asd.experiments;

import java.nio.file.Path;
import java.nio.file.Paths;

public class BenchRuns {


    public final static Path BENCHMARKS_DIRECTORY = Paths.get("src", "mybenchmarks");
    public final static Path RESULTS_DIRECTORY = Paths.get("results", "time_limited");
    public final static String FILE_WITH_NAMES = "all_benchmarks.txt";

    public static void main(String[] args) {
        try {
            Experiment.runBenchmarks(FILE_WITH_NAMES, RESULTS_DIRECTORY.toString());
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
