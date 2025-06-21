package unibs.asd.bitset;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class BitSetHypothesis {
    private BitSet bin; 
    private BitSet vector;

    private int size;
    private int n;

    public BitSetHypothesis(int size, int n) {
        if (size < 0) {
            throw new IllegalArgumentException("La dimensione non può essere negativa");
        }
        this.bin = new BitSet(size);
        this.vector = new BitSet(n);
        this.size = size;
        this.n = n;
    }

    public BitSetHypothesis(BitSet bin) {
        if (bin == null) {
            throw new IllegalArgumentException("Il BitSet non può essere null");
        }
        this.bin = (BitSet) bin.clone();
        this.vector = new BitSet();
    }

    public BitSet getBin() {
        return (BitSet) bin.clone();
    }

    public int length() {
        return bin.size();
    }

    public int mostSignificantBit() {
        return bin.nextSetBit(0);
    }

    public int leastSignificantBit() {
        return bin.previousSetBit(bin.length() - 1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bin.length(); i++) {
            sb.append(bin.get(i) ? "1" : "0");
        }
        return sb.toString();
    }

    public BitSetHypothesis globalInitial() {
        if (bin.length() == 0) {
            throw new IllegalArgumentException("L'ipotesi non può essere vuota");
        }
        if (bin.length() <= 1 || bin.get(0)) {
            throw new IllegalArgumentException("bin(h)[1] deve essere 0");
        }

        BitSet globalInitialBin = (BitSet) bin.clone();
        globalInitialBin.flip(0);

        int lsb = leastSignificantBit();
        if (lsb != -1) {
            globalInitialBin.flip(lsb);
        }

        return new BitSetHypothesis(globalInitialBin);
    }

    public int cardinality() {
        return bin.cardinality();
    }

    public BitSet getVector() {
        return (BitSet) vector.clone();
    }

    public void setVector(BitSet vector) {
        this.vector = (BitSet) vector.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BitSetHypothesis that = (BitSetHypothesis) o;
        return this.bin.equals(that.bin);
    }

    @Override
    public int hashCode() {
        return bin.hashCode();
    }

    public List<BitSetHypothesis> predecessors(){

        List<BitSetHypothesis> predecessors = new ArrayList<>();

        for(int i = 0; i < this.size; i++){

            if(bin.get(i)){
                BitSet h_s = (BitSet) bin.clone();
                h_s.flip(i);
                predecessors.add(new BitSetHypothesis(h_s));

            }
            
        }

        return predecessors;
        
    }
}