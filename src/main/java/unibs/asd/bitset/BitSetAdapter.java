package unibs.asd.bitset;

import java.math.BigInteger;
import java.util.BitSet;

import unibs.asd.interfaces.BitVector;

public class BitSetAdapter implements BitVector {

    private final BitSet bitSet;
    private final int size;

    public BitSetAdapter(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size deve essere maggiore di zero");
        }
        this.size = size;
        this.bitSet = new BitSet(size);
    }

    private BitSetAdapter(BitSetAdapter original) {
        this.size = original.size;
        this.bitSet = (BitSet) original.bitSet.clone();
    }

    public int size() {
        return size;
    }

    public int mostSignificantBit() {
        return this.bitSet.nextSetBit(0);
    }

    public boolean get(int index) {
        return bitSet.get(index);
    }

    public BigInteger toNaturalValue() {
        BigInteger value = BigInteger.ZERO;
        for (int i = 0; i < size; i++) {
            if (bitSet.get(i)) {
                int power = size - 1 - i;
                value = value.add(BigInteger.ONE.shiftLeft(power));
            }
        }
        return value;
    }

    public void set(int index) {
        bitSet.set(index);
    }

    public void set(int index, boolean value) {
        bitSet.set(index, value);
    }

    public void clear(int index) {
        bitSet.clear(index);
    }

    public void flip(int index) {
        bitSet.flip(index);
    }

    public int cardinality() {
        return bitSet.get(0, size).cardinality();
    }

    public void and(BitSetAdapter other) {
        bitSet.and(other.bitSet);
    }

    public void or(BitSetAdapter other) {
        bitSet.or(other.bitSet);
    }

    public void xor(BitSetAdapter other) {
        bitSet.xor(other.bitSet);
    }

    public int leastSignificantBit() {
        return bitSet.previousSetBit(size);
    }

    public boolean isEmpty() {
        return bitSet.get(0, size).isEmpty();
    }

    public BitSet toBitSet() {
        return (BitSet) bitSet.clone();
    }

    public String toBinaryString() {
        StringBuilder sb = new StringBuilder(size);
        for (int i = size - 1; i >= 0; i--) {
            sb.append(bitSet.get(i) ? '1' : '0');
        }
        return sb.toString();
    }

    public String toBinaryStringLSBFirst() {
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            sb.append(bitSet.get(i) ? '1' : '0');
        }
        return sb.toString();
    }

    @Override
    public BitSetAdapter clone() {
        return new BitSetAdapter(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BitSetAdapter that = (BitSetAdapter) o;
        return size == that.size && bitSet.get(0, size).equals(that.toBitSet().get(0, that.size));
    }

    @Override
    public int hashCode() {
        int result = bitSet.get(0, size).hashCode();
        result = 31 * result + size;
        return result;
    }

    @Override
    public String toString() {
        return toBinaryStringLSBFirst();
    }

    public boolean isFull() {
        for (int i = 0; i < size; i++) {
            if (!this.bitSet.get(i)) {
                return false;
            }
        }
        return true;
    }

}