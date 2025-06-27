package unibs.asd.bitset;

import java.util.ArrayList;
import java.util.List;

public class FastHypothesis {

    private FastBitSet bin;
    private FastBitSet vector;

    public FastHypothesis(int size, int n) {
        this.bin = new FastBitSet(size);
        this.vector = new FastBitSet(n);
    }

    public FastHypothesis(FastBitSet h) {
        this.bin = new FastBitSet(h.size());
        this.vector = new FastBitSet(h.size());
        System.arraycopy(h.words(), 0, this.bin.words(), 0, h.words().length);
    }

    public FastBitSet getBin() {
        return (FastBitSet) bin.clone();
    }

    public FastBitSet getVector() {
        return (FastBitSet) vector.clone();
    }

    public int length() {
        return bin.size();
    }

    public int mostSignificantBit() {
        return bin.nextSetBit(0);
    }

    @Override
    public String toString() {
        return bin.toString();
    }

    public FastHypothesis globalInitial() {
        FastBitSet globalInitialBin = (FastBitSet) bin.clone();
        globalInitialBin.flip(0);
        int lsb = bin.previousSetBit(bin.size()-1);
        globalInitialBin.flip(lsb);
        return new FastHypothesis(globalInitialBin);
    }

    public int cardinality() {
        return bin.cardinality();
    }

    public boolean isSolution() {
        return this.vector.cardinality() == this.vector.size();
    }

    public void setVector(FastBitSet vector) {
        this.vector = (FastBitSet) vector.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FastHypothesis that = (FastHypothesis) o;
        return this.bin.equals(that.getBin());
    }

    @Override
    public int hashCode() {
        return this.bin.hashCode();
    }

    public List<FastHypothesis> predecessors() {
        List<FastHypothesis> list = new ArrayList<>();
        int size = bin.size();
        FastBitSet tmp = new FastBitSet(size);
        long[] tmpWords = tmp.words();
        for (int i = 0; i < bin.words().length; i++) {
            tmpWords[i] = bin.words()[i];
        }

        for (int i = 0; i < size; i++) {
            if ((bin.words()[i >> 6] & (1L << i)) != 0) {
                tmp.words()[i >> 6] ^= (1L << i);
                list.add(new FastHypothesis(tmp));
                tmp.words()[i >> 6] ^= (1L << i); // ripristino
            }
        }
        return list;
    }
}