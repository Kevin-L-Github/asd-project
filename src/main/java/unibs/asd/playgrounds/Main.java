package unibs.asd.playgrounds;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import unibs.asd.enums.BitSetType;
import unibs.asd.fileio.BenchmarkReader;
import unibs.asd.fileio.BenchmarkWriter;
import unibs.asd.interfaces.MHS;
import unibs.asd.mhs.BoostMHS;

public class Main {
    private static final String RUNNED_BENCHMARKS_PATH = "results\\fully_completed\\runnedbenchmarks.txt";
    private static final String BENCHMARKS_DIR = "src\\mybenchmarks\\";
    private static final String RESULTS_DIR = "results\\fully_completed";
    private static final int TIMEOUT = 300; // timeout in secondi

    public static void main(String[] args) {
        int i = 0;
        System.out.println("Starting benchmark processing...");
        try (BufferedReader reader = new BufferedReader(new FileReader(RUNNED_BENCHMARKS_PATH))) {
            String benchmarkName;
            
            while ((benchmarkName = reader.readLine()) != null) {
                if (benchmarkName.trim().isEmpty()) {
                    continue; // Salta righe vuote
                }
                
                try {
                    // Leggi il benchmark
                    boolean[][] benchmark = BenchmarkReader.readBenchmark(BENCHMARKS_DIR + benchmarkName);
                    
                    // Crea e esegui MHS
                    MHS mhs = new BoostMHS(benchmark);
                    mhs.run(BitSetType.FAST_BITSET, TIMEOUT * 1000);
                    
                    // Scrivi i risultati
                    BenchmarkWriter.writeBenchmark(mhs, benchmarkName, RESULTS_DIR);
                    
                    // Pulisci la memoria
                    mhs = null;
                    benchmark = null;
                } catch (Exception e) {
                    System.err.println("Error processing benchmark " + benchmarkName + ": " + e.getMessage());
                    e.printStackTrace();
                }
                
                // Forza il garbage collection
                System.gc();
                
                try {
                    // Piccola pausa per permettere al GC di lavorare
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.print("\rProgress: " + (++i) + "/172 - benchmarks processed.");
            }
        } catch (IOException e) {
            System.err.println("Error reading benchmark list: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
