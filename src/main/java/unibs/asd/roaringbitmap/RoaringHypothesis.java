package unibs.asd.roaringbitmap;

import java.util.ArrayList;
import java.util.List;

import unibs.asd.interfaces.BitVector;
import unibs.asd.interfaces.Hypothesis;

public class RoaringHypothesis implements Hypothesis {

    private RoaringBitmapAdapter bin;
    private RoaringBitmapAdapter vector;

    public RoaringHypothesis(int size, int n) {
        if (size < 0) {
            throw new IllegalArgumentException("La dimensione non può essere negativa");
        }
        this.bin = new RoaringBitmapAdapter(size);
        this.vector = new RoaringBitmapAdapter(n);
    }

    public RoaringHypothesis(RoaringBitmapAdapter h) {
        if (h == null) {
            throw new IllegalArgumentException("Il BitSet non può essere null");
        }
        this.bin = (RoaringBitmapAdapter) h.clone();
        this.vector = new RoaringBitmapAdapter(h.size());
    }

    public RoaringBitmapAdapter getBin() {
        return (RoaringBitmapAdapter) bin.clone();
    }

    public int length() {
        return bin.size();
    }

    public int mostSignificantBit() {
        return bin.mostSignificantBit();
    }

    @Override
    public String toString() {
        return bin.toString();
    }

    public RoaringHypothesis globalInitial() {

        RoaringBitmapAdapter globalInitialBin = (RoaringBitmapAdapter) bin.clone();
        globalInitialBin.flip(0);
        int lsb = bin.leastSignificantBit();
        globalInitialBin.flip(lsb);

        return new RoaringHypothesis(globalInitialBin);
    }

    public int cardinality() {
        return bin.cardinality();
    }

    public RoaringBitmapAdapter getVector() {
        return (RoaringBitmapAdapter) vector.clone();
    }

    public void setVector(RoaringBitmapAdapter vector) {
        this.vector = (RoaringBitmapAdapter) vector.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RoaringHypothesis that = (RoaringHypothesis) o;
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
                RoaringBitmapAdapter h_s = (RoaringBitmapAdapter) bin.clone();
                h_s.flip(i);
                predecessors.add(new RoaringHypothesis(h_s));
            }
        }
        return predecessors;
    }

    @Override
    public void setVector(BitVector vector) {
        this.vector = (RoaringBitmapAdapter) vector.clone();
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
        this.bin.flip(i);
    }

    @Override
    public void or(Hypothesis other) {
        this.vector.or((RoaringBitmapAdapter) other.getVector());
    }

    @Override
    public RoaringHypothesis clone() {
        return new RoaringHypothesis(this.bin);
    }

    @Override
    public void update(BitVector information) {
        
    }

    @Override
    public boolean isSolution() {
        return this.vector.isFull();
    }

    

}
