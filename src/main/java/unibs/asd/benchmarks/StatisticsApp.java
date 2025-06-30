package unibs.asd.benchmarks;

public class StatisticsApp {
    public static void main(String[] args) {
        String benchmarkDirectory = "src/mybenchmarks";         // ← metti qui il path corretto della cartella con i file .txt
        String outputDirectory = "results";      // ← cartella dove scrivere selected_benchmark.txt

        BenchmarksSelection selector = new BenchmarksSelection();

        try {
            selector.runBenchmarks(benchmarkDirectory, outputDirectory);
        } catch (Exception e) {
            System.err.println("[Fatal Error] " + e.getMessage());
            e.printStackTrace();
        }
    }
}
