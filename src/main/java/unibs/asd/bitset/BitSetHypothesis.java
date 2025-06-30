package unibs.asd.bitset;

import java.util.ArrayList;
import java.util.List;

import unibs.asd.interfaces.BitVector;
import unibs.asd.interfaces.Hypothesis;

public class BitSetHypothesis implements Hypothesis {

    private BitSetAdapter bin;
    private BitSetAdapter vector;

    public BitSetHypothesis(int size, int n) {
        if (size < 0) {
            throw new IllegalArgumentException("La dimensione non può essere negativa");
        }
        this.bin = new BitSetAdapter(size);
        this.vector = new BitSetAdapter(n);
    }

    public BitSetHypothesis(BitSetAdapter vector) {
        if (vector == null) {
            throw new IllegalArgumentException("Il BitSet non può essere null");
        }
        this.bin = (BitSetAdapter) vector.clone();
        this.vector = new BitSetAdapter(vector.size());
    }

    public BitSetAdapter getBin() {
        return (BitSetAdapter) bin.clone();
    }

    public int length() {
        return bin.size();
    }

    public int mostSignificantBit() {
        return bin.mostSignificantBit();
    }

    @Override
    public String toString() {
        return bin.toBinaryStringLSBFirst();
    }

    public BitSetHypothesis globalInitial() {
        BitSetAdapter globalInitialBin = (BitSetAdapter) bin.clone();
        globalInitialBin.flip(0);
        int lsb = bin.leastSignificantBit();
        globalInitialBin.flip(lsb);
        return new BitSetHypothesis(globalInitialBin);
    }

    public int cardinality() {
        return bin.cardinality();
    }

    public BitSetAdapter getVector() {
        return (BitSetAdapter) vector.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BitSetHypothesis that = (BitSetHypothesis) o;
        return this.bin.equals(that.getBin());
    }

    @Override
    public int hashCode() {
        return bin.hashCode();
    }

    public List<Hypothesis> predecessors() {
        List<Hypothesis> predecessors = new ArrayList<>();
        int limit = this.bin.size();
        for (int i = 0; i < limit; i++) {
            if (bin.get(i)) {
                BitSetAdapter h_s = (BitSetAdapter) bin.clone();
                h_s.flip(i);
                predecessors.add(new BitSetHypothesis(h_s));
            }
        }
        return predecessors;
    }

    public void set(int i) {
        this.bin.set(i);
    }

    public void flip(int i) {
        this.bin.flip(i);
    }

    @Override
    public Hypothesis clone() {
        try {
            BitSetHypothesis cloned = (BitSetHypothesis) super.clone();
            cloned.bin = (BitSetAdapter) this.bin.clone();
            cloned.vector = (BitSetAdapter) this.vector.clone();
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // non dovrebbe succedere
        }
    }

    @Override
    public void or(Hypothesis other) {
        this.bin.or((BitSetAdapter) other.getBin());
    }

    @Override
    public void setVector(BitVector vector) {
        this.vector = (BitSetAdapter) vector;
    }

}