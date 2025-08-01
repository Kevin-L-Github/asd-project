package unibs.asd.experiments;

import unibs.asd.enums.BitSetType;
import java.util.EnumSet;

/**
 * Main class per eseguire i benchmark con il sistema multi-livello
 */
public class NewExperiment {
    
    public static void main(String[] args) {
        try {
            // Configurazione percorsi
            String benchmarkDir = "benchmarks";  // cartella con i tuoi .matrix
            String outputDir = "results";        // cartella risultati
            
            // Configura il BitSetType (usa quello che usi normalmente)
            BitSetType bitSetType = BitSetType.FAST_BITSET;
            
            System.out.println("🚀 Avvio benchmark multi-livello...");
            
            // SCEGLI UNA DELLE OPZIONI SOTTO:
            
            // OPZIONE 1: Esecuzione completa (raccomandato per il progetto finale)
            runAllLevels(benchmarkDir, outputDir, bitSetType);
            
            // OPZIONE 2: Solo test rapidi (per debugging - decommentare se serve)
            // runQuickTestsOnly(benchmarkDir, outputDir, bitSetType);
            
            // OPZIONE 3: Test progressivo (prima quick, poi se OK medium, ecc.)
            // runProgressiveTest(benchmarkDir, outputDir, bitSetType);
            
        } catch (Exception e) {
            System.err.println("❌ Errore durante l'esecuzione: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Esecuzione completa di tutti i livelli
     */
    public static void runAllLevels(String benchmarkDir, String outputDir, BitSetType bitSetType) 
            throws Exception {
        System.out.println("=== ESECUZIONE COMPLETA ===");
        
        NewFilterBenchmarks runner = new NewFilterBenchmarks(benchmarkDir, outputDir, bitSetType);
        runner.runBenchmarks();
        
        System.out.println("✅ Esecuzione completa terminata!");
    }
    
    /**
     * Solo test rapidi (utile per debugging)
     */
    public static void runQuickTestsOnly(String benchmarkDir, String outputDir, BitSetType bitSetType) 
            throws Exception {
        System.out.println("=== SOLO TEST RAPIDI ===");
        
        NewFilterBenchmarks runner = new NewFilterBenchmarks(
            benchmarkDir, 
            outputDir + "/quick_only", 
            bitSetType, 
            true, 
            EnumSet.of(NewFilterBenchmarks.BenchmarkLevel.QUICK)
        );
        
        runner.runBenchmarks();
        
        System.out.println("✅ Test rapidi completati!");
    }
    
    /**
     * Test progressivo: prima quick, se va bene medium, ecc.
     */
    public static void runProgressiveTest(String benchmarkDir, String outputDir, BitSetType bitSetType) 
            throws Exception {
        System.out.println("=== TEST PROGRESSIVO ===");
        
        // Step 1: Test rapidi
        System.out.println("🔸 Step 1: Esecuzione test rapidi...");
        NewFilterBenchmarks quickRunner = new NewFilterBenchmarks(
            benchmarkDir, outputDir + "/progressive/quick", bitSetType, 
            true, EnumSet.of(NewFilterBenchmarks.BenchmarkLevel.QUICK)
        );
        quickRunner.runBenchmarks();
        
        // Verifica successo
        var quickResults = quickRunner.getResultsByLevel().get(NewFilterBenchmarks.BenchmarkLevel.QUICK);
        long quickCompleted = quickResults.stream().mapToLong(r -> r.completed ? 1 : 0).sum();
        double quickSuccessRate = (double) quickCompleted / quickResults.size();
        
        System.out.printf("📊 Test rapidi: %.1f%% successo (%d/%d)%n", 
            quickSuccessRate * 100, quickCompleted, quickResults.size());
        
        // Step 2: Se quick OK, fai medium
        if (quickSuccessRate > 0.8) {
            System.out.println("🔸 Step 2: Test rapidi OK, esecuzione test medi...");
            NewFilterBenchmarks mediumRunner = new NewFilterBenchmarks(
                benchmarkDir, outputDir + "/progressive/medium", bitSetType,
                true, EnumSet.of(NewFilterBenchmarks.BenchmarkLevel.MEDIUM)
            );
            mediumRunner.runBenchmarks();
            
            var mediumResults = mediumRunner.getResultsByLevel().get(NewFilterBenchmarks.BenchmarkLevel.MEDIUM);
            long mediumCompleted = mediumResults.stream().mapToLong(r -> r.completed ? 1 : 0).sum();
            double mediumSuccessRate = (double) mediumCompleted / mediumResults.size();
            
            System.out.printf("📊 Test medi: %.1f%% successo (%d/%d)%n", 
                mediumSuccessRate * 100, mediumCompleted, mediumResults.size());
            
            // Step 3: Se medium OK, fai intensive
            if (mediumSuccessRate > 0.6) {
                System.out.println("🔸 Step 3: Test medi OK, esecuzione test intensivi...");
                NewFilterBenchmarks intensiveRunner = new NewFilterBenchmarks(
                    benchmarkDir, outputDir + "/progressive/intensive", bitSetType,
                    true, EnumSet.of(NewFilterBenchmarks.BenchmarkLevel.INTENSIVE)
                );
                intensiveRunner.runBenchmarks();
                System.out.println("✅ Test progressivo completato!");
            } else {
                System.out.println("⚠️ Test medi con problemi, salto test intensivi");
            }
        } else {
            System.out.println("❌ Test rapidi con troppi fallimenti, stop");
        }
    }
}