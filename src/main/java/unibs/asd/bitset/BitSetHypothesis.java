package unibs.asd.bitset;

import java.util.ArrayList;
import java.util.List;

import unibs.asd.interfaces.BitVector;

public class BitSetHypothesis {

    private BitVector bin;
    private BitVector vector;

    public BitSetHypothesis(int size, int n) {
        if (size < 0) {
            throw new IllegalArgumentException("La dimensione non può essere negativa");
        }
        this.bin = new BitSetAdapter(size);
        this.vector = new BitSetAdapter(n);
    }

    public BitSetHypothesis(BitVector vector) {
        if (vector == null) {
            throw new IllegalArgumentException("Il BitSet non può essere null");
        }
        this.bin = (BitVector) vector.clone();
        this.vector = new BitSetAdapter(vector.size());
    }

    public BitVector getBin() {
        return (BitVector) bin.clone();
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
        BitVector globalInitialBin = (BitVector) bin.clone();
        globalInitialBin.flip(0);
        int lsb = bin.leastSignificantBit();
        globalInitialBin.flip(lsb);
        return new BitSetHypothesis(globalInitialBin);
    }

    public int cardinality() {
        return bin.cardinality();
    }

    public BitVector getVector() {
        return (BitVector) vector.clone();
    }

    public void setVector(BitVector vector) {
        this.vector = (BitVector) vector.clone();
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

    public List<BitSetHypothesis> predecessors() {
        List<BitSetHypothesis> predecessors = new ArrayList<>();
        int limit = this.bin.size();
        for (int i = 0; i < limit; i++) {
            if (bin.get(i)) {
                BitVector h_s = (BitVector) bin.clone();
                h_s.flip(i);
                predecessors.add(new BitSetHypothesis(h_s));
            }
        }
        return predecessors;
    }
}