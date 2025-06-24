package unibs.asd.bitset;

import java.util.ArrayList;
import java.util.List;

public class BitSetHypothesis {

    private BitSetAdapter bin;
    private BitSetAdapter vector;

    public BitSetHypothesis(int size, int n) {
        if (size < 0) {
            throw new IllegalArgumentException("La dimensione non può essere negativa");
        }
        this.bin = new BitSetAdapter(size);
        this.vector = new BitSetAdapter(n);
    }

    public BitSetHypothesis(BitSetAdapter h) {
        if (h == null) {
            throw new IllegalArgumentException("Il BitSet non può essere null");
        }
        this.bin = (BitSetAdapter) h.clone();
        this.vector = new BitSetAdapter(h.size());
    }

    public BitSetAdapter getBin() {
        return (BitSetAdapter) bin.clone();
    }

    public int length() {
        return bin.size();
    }

    public int mostSignificantBit() {
        return bin.getLength();
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

    public void setVector(BitSetAdapter vector) {
        this.vector = (BitSetAdapter) vector.clone();
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
                BitSetAdapter h_s = (BitSetAdapter) bin.clone();
                h_s.flip(i);
                predecessors.add(new BitSetHypothesis(h_s));
            }
        }
        return predecessors;
    }
}