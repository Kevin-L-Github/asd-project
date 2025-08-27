package unibs.asd.playgrounds;

import unibs.asd.structures.bitset.BitSetHypothesis;
import unibs.asd.structures.bools.BoolsHypothesis;
import unibs.asd.structures.fastbitset.FastHypothesis;
import unibs.asd.structures.roaringbitmap.RoaringHypothesis;
import unibs.asd.structures.sparsebitset.SparseHypothesis;

import org.openjdk.jol.info.GraphLayout;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

public class MemoryPlayground {

    static long getObjectSize(Object h) {
        return GraphLayout.parseInstance(h).totalSize();
    }

    public static void main(String[] args) {
        // Dimensioni più rappresentative per l'analisi
        int[] sizes = { 64, 128, 256, 512, 1024, 5000, 10000 };
        // Densità per evidenziare il comportamento delle implementazioni compresse
        int[] densities = { 25, 50, 75, 100 };

        Random rnd = new Random(42);

        System.out.println("=== MEMORY USAGE ANALYSIS ===");
        System.out.println("Measuring actual memory footprint of different BitSet implementations");
        System.out.println();
        
        System.out.printf("%-8s %-8s %-18s %-18s %-18s %-18s %-18s%n",
                "Size", "Density%", "RoaringHypothesis", "FastHypothesis", "BoolsHypothesis", 
                "BitSetHypothesis", "SparseHypothesis");
        System.out.println("=".repeat(120));

        for (int size : sizes) {
            for (int densityPercent : densities) {

                // FIX 1: Calcolo corretto della densità
                int bitsToSet = (size * densityPercent) / 100;

                // Crea istanze
                RoaringHypothesis r = new RoaringHypothesis(size, size);
                FastHypothesis f = new FastHypothesis(size, size);
                BoolsHypothesis b = new BoolsHypothesis(size, size);
                BitSetHypothesis bit = new BitSetHypothesis(size, size);
                SparseHypothesis s = new SparseHypothesis(size, size);

                // FIX 2: Evita bit duplicati usando un Set
                Set<Integer> bitsToSetSet = new HashSet<>();
                while (bitsToSetSet.size() < bitsToSet && bitsToSetSet.size() < size) {
                    bitsToSetSet.add(rnd.nextInt(size));
                }

                // Setta i bit per tutte le implementazioni
                for (int idx : bitsToSetSet) {
                    r.set(idx);
                    f.set(idx);
                    b.set(idx);
                    bit.set(idx);
                    s.set(idx);
                }

                // Misura memoria
                long sizeR = getObjectSize(r);
                long sizeF = getObjectSize(f);
                long sizeB = getObjectSize(b);
                long sizeBit = getObjectSize(bit);
                long sizeS = getObjectSize(s);

                System.out.printf("%-8d %-8d %-18d %-18d %-18d %-18d %-18d%n",
                        size, densityPercent, sizeR, sizeF, sizeB, sizeBit, sizeS);

            }
            System.out.println(); // Riga vuota tra diverse dimensioni per leggibilità
        }
        
        // Aggiungi analisi semplice
        System.out.println("\n=== ANALYSIS NOTES ===");
        System.out.println("* All measurements in bytes");
        System.out.println("* Size = number of possible bit positions");
        System.out.println("* Density% = percentage of bits actually set to 1");
        System.out.println("* Each implementation stores same logical data but with different memory footprint");
    }
}