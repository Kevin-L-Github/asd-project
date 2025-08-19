package unibs.asd.playgrounds;

import unibs.asd.structures.bitset.BitSetHypothesis;
import unibs.asd.structures.bools.BoolsHypothesis;
import unibs.asd.structures.fastbitset.FastHypothesis;
import unibs.asd.structures.roaringbitmap.RoaringHypothesis;
import unibs.asd.structures.sparse.SparseHypothesis;

import java.util.*;

public class MemoryPlayground {
    
    private static final Random rnd = new Random(42);
    
    public static void main(String[] args) {
        System.out.println("=== PERFORMANCE COMPARISON ===");
        System.out.println("Focusing on OR operations (critical for MHS algorithm)");
        System.out.println();
        
        performanceTest();
        
        System.out.println("\n=== THEORETICAL MEMORY ANALYSIS ===");
        theoreticalMemoryAnalysis();
        
        System.out.println("\n=== CARDINALITY PERFORMANCE ===");
        cardinalityTest();
    }
    
    /**
     * Test delle operazioni OR (critiche per l'algoritmo MHS)
     */
    private static void performanceTest() {
        int[] sizes = {64, 256, 512, 1024};
        int[] densities = {10, 25, 50};
        int iterations = 10000;
        
        System.out.printf("%-6s %-8s %-12s %-12s %-12s %-12s %-12s%n",
                "Size", "Density%", "Boolean(ms)", "BitSet(ms)", "Fast(ms)", "Roaring(ms)", "Sparse(ms)");
        System.out.println("=".repeat(77));
        
        for (int size : sizes) {
            for (int density : densities) {
                Map<String, Long> results = measureORPerformance(size, density, iterations);
                
                System.out.printf("%-6d %-8d %-12.2f %-12.2f %-12.2f %-12.2f %-12.2f%n",
                        size, density,
                        results.get("Boolean") / 1_000_000.0,
                        results.get("BitSet") / 1_000_000.0,
                        results.get("Fast") / 1_000_000.0,
                        results.get("Roaring") / 1_000_000.0,
                        results.get("Sparse") / 1_000_000.0);
            }
        }
    }
    
    /**
     * Analisi teorica dell'occupazione di memoria
     */
    private static void theoreticalMemoryAnalysis() {
        System.out.printf("%-8s %-10s %-15s %-15s %-15s %-15s%n",
                "Size", "Density%", "Boolean(bytes)", "BitSet(bytes)", "Roaring(est)", "Sparse(est)");
        System.out.println("=".repeat(75));
        
        int[] sizes = {64, 256, 512, 1024};
        int[] densities = {10, 25, 50, 75};
        
        for (int size : sizes) {
            for (int density : densities) {
                int setBits = (size * density) / 100;
                
                // Calcoli teorici
                int booleanMem = size; // 1 byte per boolean in Java
                int bitsetMem = (size + 7) / 8; // Arrotondato a byte
                int roaringEst = estimateRoaringMemory(setBits);
                int sparseEst = estimateSparseMemory(setBits);
                
                System.out.printf("%-8d %-10d %-15d %-15d %-15d %-15d%n",
                        size, density, booleanMem, bitsetMem, roaringEst, sparseEst);
            }
        }
        
        System.out.println("\n* Stime teoriche basate su documentazione delle librerie");
    }
    
    /**
     * Test performance operazione cardinality multidimensionale
     */
    private static void cardinalityTest() {
        int[] sizes = {64, 256, 512, 1024};
        int[] densities = {10, 25, 50};
        int iterations = 50000; // Ridotto per gestire multiple configurazioni
        
        System.out.printf("%-6s %-8s %-12s %-12s %-12s %-12s %-12s%n",
                "Size", "Density%", "Boolean(ms)", "BitSet(ms)", "Fast(ms)", "Roaring(ms)", "Sparse(ms)");
        System.out.println("=".repeat(77));
        
        for (int size : sizes) {
            for (int density : densities) {
                Map<String, Long> results = measureCardinalityPerformance(size, density, iterations);
                
                System.out.printf("%-6d %-8d %-12.2f %-12.2f %-12.2f %-12.2f %-12.2f%n",
                        size, density,
                        results.get("Boolean") / 1_000_000.0,
                        results.get("BitSet") / 1_000_000.0,
                        results.get("Fast") / 1_000_000.0,
                        results.get("Roaring") / 1_000_000.0,
                        results.get("Sparse") / 1_000_000.0);
            }
        }
    }
    
    private static Map<String, Long> measureCardinalityPerformance(int size, int density, int iterations) {
        Set<Integer> positions = generateRandomPositions(size, density);
        Map<String, Long> results = new HashMap<>();
        
        // Create test instances
        BoolsHypothesis bool = new BoolsHypothesis(size, size);
        BitSetHypothesis bitSet = new BitSetHypothesis(size, size);
        FastHypothesis fast = new FastHypothesis(size, size);
        RoaringHypothesis roaring = new RoaringHypothesis(size, size);
        SparseHypothesis sparse = new SparseHypothesis(size, size);
        
        positions.forEach(pos -> {
            bool.set(pos);
            bitSet.set(pos);
            fast.set(pos);
            roaring.set(pos);
            sparse.set(pos);
        });
        
        // Warm up
        for (int i = 0; i < 100; i++) {
            bool.cardinality();
            bitSet.cardinality();
            fast.cardinality();
            roaring.cardinality();
            sparse.cardinality();
        }
        
        // Measure Boolean
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            bool.cardinality();
        }
        results.put("Boolean", System.nanoTime() - start);
        
        // Measure BitSet
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            bitSet.cardinality();
        }
        results.put("BitSet", System.nanoTime() - start);
        
        // Measure Fast
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            fast.cardinality();
        }
        results.put("Fast", System.nanoTime() - start);
        
        // Measure Roaring
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            roaring.cardinality();
        }
        results.put("Roaring", System.nanoTime() - start);
        
        // Measure Sparse
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            sparse.cardinality();
        }
        results.put("Sparse", System.nanoTime() - start);
        
        return results;
    }
    
    private static Map<String, Long> measureORPerformance(int size, int density, int iterations) {
        Set<Integer> positions = generateRandomPositions(size, density);
        Map<String, Long> results = new HashMap<>();
        
        // Create test pairs
        BoolsHypothesis[] boolPair = createBoolPair(size, positions);
        BitSetHypothesis[] bitSetPair = createBitSetPair(size, positions);
        FastHypothesis[] fastPair = createFastPair(size, positions);
        RoaringHypothesis[] roaringPair = createRoaringPair(size, positions);
        SparseHypothesis[] sparsePair = createSparsePair(size, positions);
        
        // Warm up JVM
        for (int i = 0; i < 100; i++) {
            boolPair[0].or(boolPair[1]);
            bitSetPair[0].or(bitSetPair[1]);
            fastPair[0].or(fastPair[1]);
            roaringPair[0].or(roaringPair[1]);
            sparsePair[0].or(sparsePair[1]);
        }
        
        // Measure Boolean
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            boolPair[0].or(boolPair[1]);
        }
        results.put("Boolean", System.nanoTime() - start);
        
        // Measure BitSet
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            bitSetPair[0].or(bitSetPair[1]);
        }
        results.put("BitSet", System.nanoTime() - start);
        
        // Measure Fast
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            fastPair[0].or(fastPair[1]);
        }
        results.put("Fast", System.nanoTime() - start);
        
        // Measure Roaring
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            roaringPair[0].or(roaringPair[1]);
        }
        results.put("Roaring", System.nanoTime() - start);
        
        // Measure Sparse
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            sparsePair[0].or(sparsePair[1]);
        }
        results.put("Sparse", System.nanoTime() - start);
        
        return results;
    }
    
    // Helper methods
    private static Set<Integer> generateRandomPositions(int size, int densityPercent) {
        int bitsToSet = (size * densityPercent) / 100;
        Set<Integer> positions = new HashSet<>();
        
        while (positions.size() < bitsToSet && positions.size() < size) {
            positions.add(rnd.nextInt(size));
        }
        return positions;
    }
    
    private static BoolsHypothesis[] createBoolPair(int size, Set<Integer> positions) {
        BoolsHypothesis h1 = new BoolsHypothesis(size, size);
        BoolsHypothesis h2 = new BoolsHypothesis(size, size);
        positions.forEach(pos -> { h1.set(pos); h2.set(pos); });
        return new BoolsHypothesis[]{h1, h2};
    }
    
    private static BitSetHypothesis[] createBitSetPair(int size, Set<Integer> positions) {
        BitSetHypothesis h1 = new BitSetHypothesis(size, size);
        BitSetHypothesis h2 = new BitSetHypothesis(size, size);
        positions.forEach(pos -> { h1.set(pos); h2.set(pos); });
        return new BitSetHypothesis[]{h1, h2};
    }
    
    private static FastHypothesis[] createFastPair(int size, Set<Integer> positions) {
        FastHypothesis h1 = new FastHypothesis(size, size);
        FastHypothesis h2 = new FastHypothesis(size, size);
        positions.forEach(pos -> { h1.set(pos); h2.set(pos); });
        return new FastHypothesis[]{h1, h2};
    }
    
    private static RoaringHypothesis[] createRoaringPair(int size, Set<Integer> positions) {
        RoaringHypothesis h1 = new RoaringHypothesis(size, size);
        RoaringHypothesis h2 = new RoaringHypothesis(size, size);
        positions.forEach(pos -> { h1.set(pos); h2.set(pos); });
        return new RoaringHypothesis[]{h1, h2};
    }
    
    private static SparseHypothesis[] createSparsePair(int size, Set<Integer> positions) {
        SparseHypothesis h1 = new SparseHypothesis(size, size);
        SparseHypothesis h2 = new SparseHypothesis(size, size);
        positions.forEach(pos -> { h1.set(pos); h2.set(pos); });
        return new SparseHypothesis[]{h1, h2};
    }
    
    private static int estimateRoaringMemory(int setBits) {
        // RoaringBitmap overhead: ~16 bytes base + ~2 bytes per set bit (semplificato)
        return 16 + (setBits * 2);
    }
    
    private static int estimateSparseMemory(int setBits) {
        // SparseBitSet overhead: ~32 bytes base + ~4 bytes per set bit (semplificato)
        return 32 + (setBits * 4);
    }
}