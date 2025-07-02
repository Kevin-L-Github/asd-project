package unibs.asd.fastbitset;

import java.util.ArrayList;
import java.util.List;

import unibs.asd.interfaces.BitVector;
import unibs.asd.interfaces.Hypothesis;

public class FastHypothesis implements Hypothesis {

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
        int lsb = bin.previousSetBit(bin.size() - 1);
        globalInitialBin.flip(lsb);
        return new FastHypothesis(globalInitialBin);
    }

    public int cardinality() {
        return bin.cardinality();
    }

    public boolean isSolution() {
        return this.vector.isFull();
    }

    public void setVector(FastBitSet vector) {
        this.vector = vector;
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

    public List<Hypothesis> predecessors() {
        List<Hypothesis> list = new ArrayList<>();
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

    @Override
    public void setVector(BitVector vector) {
        this.vector = (FastBitSet) vector;
    }

    @Override
    public void set(int i) {
        this.bin.set(i);
    }

    @Override
    public void set(int i, boolean value) {
        this.bin.set(i, value);
    }

    @Override
    public void flip(int i) {
        this.flip(i);
    }

    @Override
    public void or(Hypothesis other) {
        this.vector.or((FastBitSet) other.getVector());
    }

    @Override
    public FastHypothesis clone() {
        try {
            FastHypothesis copy = (FastHypothesis) super.clone();
            copy.bin = (FastBitSet) this.bin.clone();
            copy.vector = (FastBitSet) this.vector.clone();
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Should never happen since we implement Cloneable
        }
    }

    @Override
    public void update(BitVector information) {
        this.vector.or((FastBitSet) information);
    }
}