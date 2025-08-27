package unibs.asd.playgrounds;

import unibs.asd.structures.bitset.BitSetHypothesis;
import unibs.asd.structures.bools.BoolsHypothesis;
import unibs.asd.structures.fastbitset.FastHypothesis;
import unibs.asd.structures.roaringbitmap.RoaringHypothesis;
import unibs.asd.structures.sparsebitset.SparseHypothesis;

import org.openjdk.jol.info.GraphLayout;
import java.util.Random;

public class MemoryPlaygroung {

    static long getObjectSize(Object h) {
        return GraphLayout.parseInstance(h).totalSize();
    }

    public static void main(String[] args) {
        int[] sizes = { 256,1500 ,10000 };
        int[] densities = { 0, 25, 50, 100 };

        Random rnd = new Random(42);

        System.out.printf("%-8s %-8s %-18s %-18s %-18s %-18s %-18s%n",
                "Size", "Density", "RoaringHypothesis", "FastHypothesis", "BoolsHypothesis", "BitSetHypothesis",
                "SparseHypothesis");

        for (int size : sizes) {
            for (int densityPercent : densities) {

                int bitsToSet = size * densityPercent / 1000;

                RoaringHypothesis r = new RoaringHypothesis(size, size);
                FastHypothesis f = new FastHypothesis(size, size);
                BoolsHypothesis b = new BoolsHypothesis(size, size);
                BitSetHypothesis bit = new BitSetHypothesis(size, size);
                SparseHypothesis s = new SparseHypothesis(size, size);

                for (int i = 0; i < bitsToSet; i++) {
                    int idx = rnd.nextInt(size);
                    r.set(idx);
                    f.set(idx);
                    b.set(idx);
                    bit.set(idx);
                    s.set(idx);
                }

                long sizeR = getObjectSize(r);
                long sizeF = getObjectSize(f);
                long sizeB = getObjectSize(b);
                long sizeBit = getObjectSize(bit);
                long sizeS = getObjectSize(s);

                System.out.printf("%-8d %-8d %-18d %-18d %-18d %-18d %-18d%n",
                        size, densityPercent, sizeR, sizeF, sizeB, sizeBit, sizeS);

            }
        }
    }
}
