package unibs.asd.bitset;

import java.util.ArrayList;
import java.util.List;

public class FastHypothesis {

    private FastBitSet bin;
    private FastBitSet vector;

    public FastHypothesis(int size, int n) {
        if (size < 0) {
            throw new IllegalArgumentException("La dimensione non può essere negativa");
        }
        this.bin = new FastBitSet(size);
        this.vector = new FastBitSet(n);
    }

    public FastHypothesis(FastBitSet h) {
        if (h == null) {
            throw new IllegalArgumentException("Il BitSet non può essere null");
        }
        this.bin = (FastBitSet) h.clone();
        this.vector = new FastBitSet(h.size());
    }

    public FastBitSet getBin() {
        return (FastBitSet) bin.clone();
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
        int lsb = bin.previousSetBit(bin.size());
        globalInitialBin.flip(lsb);

        return new FastHypothesis(globalInitialBin);
    }

    public int cardinality() {
        return bin.cardinality();
    }

    public FastBitSet getVector() {
        return (FastBitSet) vector.clone();
    }

    public boolean isSolution(){
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
        List<FastHypothesis> predecessors = new ArrayList<>();
        int limit = this.bin.size();
        for (int i = 0; i < limit; i++) {
            if (bin.get(i)) {
                FastBitSet h_s = (FastBitSet) bin.clone();
                h_s.flip(i);
                predecessors.add(new FastHypothesis(h_s));
            }
        }
        return predecessors;
    }
}