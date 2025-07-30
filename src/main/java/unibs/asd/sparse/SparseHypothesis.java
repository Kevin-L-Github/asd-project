package unibs.asd.sparse;

import java.util.ArrayList;
import java.util.List;

import unibs.asd.interfaces.BitVector;
import unibs.asd.interfaces.Hypothesis;

public class SparseHypothesis implements Hypothesis {

    private SparseVector bin;
    private SparseVector vector;

    public SparseHypothesis(int size, int n) {
        if (size < 0) {
            throw new IllegalArgumentException("La dimensione non può essere negativa");
        }
        this.bin = new SparseVector(size);
        this.vector = new SparseVector(n);
    }

    public SparseHypothesis(SparseVector h) {
        if (h == null) {
            throw new IllegalArgumentException("Il BitSet non può essere null");
        }
        this.bin = h.clone();
        this.vector = new SparseVector(h.size());
    }

    public SparseVector getBin() {
        return this.bin.clone();
    }

    public int length() {
        return bin.size();
    }

    public int mostSignificantBit() {
        return bin.mostSignificantBit();
    }

    @Override
    public String toString() {
        return bin.toBinaryString();
    }

    public SparseHypothesis globalInitial() {
        SparseVector globalInitialBin = bin.clone();
        globalInitialBin.flip(0);
        int lsb = bin.leastSignificantBit();
        if (lsb >= 0) {
            globalInitialBin.flip(lsb);
        }
        return new SparseHypothesis(globalInitialBin);
    }

    public int cardinality() {
        return bin.cardinality();
    }

    public SparseVector getVector() {
        return vector.clone();
    }

    public void setVector(SparseVector vector) {
        this.vector = vector.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SparseHypothesis that = (SparseHypothesis) o;
        return this.bin.equals(that.getBin());
    }

    @Override
    public int hashCode() {
        return bin.hashCode();
    }

    public List<Hypothesis> predecessors() {
        List<Hypothesis> predecessors = new ArrayList<>();
        int limit = bin.size();
        for (int i = 0; i < limit; i++) {
            if (bin.get(i)) {
                SparseVector h_s = bin.clone();
                h_s.flip(i);
                predecessors.add(new SparseHypothesis(h_s));
            }
        }
        return predecessors;
    }

    @Override
    public void setVector(BitVector vector) {
        this.vector = (SparseVector) vector.clone();
    }

    @Override
    public void set(int i) {
        bin.set(i);
    }

    @Override
    public void set(int i, boolean value) {
        bin.set(i, value);
    }

    @Override
    public void flip(int i) {
        bin.flip(i);
    }

    @Override
    public void or(Hypothesis other) {
        this.vector.or((SparseVector)other.getVector());
    }

    @Override
    public SparseHypothesis clone() {
        return new SparseHypothesis(this.bin);
    }

    @Override
    public void update(BitVector information) {
        this.vector.or((SparseVector)information);
    }

    @Override
    public boolean isSolution() {
        return this.vector.isFull();
    }
}
