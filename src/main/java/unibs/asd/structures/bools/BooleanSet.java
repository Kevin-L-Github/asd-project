package unibs.asd.structures.bools;

import java.util.Arrays;

import unibs.asd.interfaces.BitVector;

public class BooleanSet implements BitVector {

    private boolean[] bits;

    public BooleanSet(boolean[] bin) {
        this.bits = bin;
    }

    public BooleanSet(int size) {
        this.bits = new boolean[size];
        Arrays.fill(this.bits, false);
    }

    public boolean[] getBools() {
        return this.bits;
    }

    @Override
    public int size() {
        return this.bits.length;
    }

    @Override
    public boolean get(int index) {
        return this.bits[index];
    }

    @Override
    public void set(int index) {
        this.bits[index] = true;
    }

    @Override
    public void set(int index, boolean value) {
        this.bits[index] = value;
    }

    @Override
    public void flip(int index) {
        this.bits[index] = !this.bits[index];
    }

    @Override
    public int cardinality() {
        int cardinality = 0;
        for (boolean b : bits) {
            if (b) {
                cardinality++;
            }
        }
        return cardinality;
    }

    @Override
    public boolean isEmpty() {
        for (boolean b : this.bits) {
            if (b) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isFull() {
        return this.cardinality() == this.bits.length;
    }

    @Override
    public int leastSignificantBit() {
        for (int i = bits.length - 1; i >= 0; i--) {
            if (bits[i]) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int mostSignificantBit() {
        for (int i = 0; i < bits.length; i++) {
            if (bits[i]) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String toBinaryString() {
        StringBuilder sb = new StringBuilder();
        for (boolean b : bits) {
            sb.append(b ? "1" : "0");
        }
        return sb.toString();
    }

    @Override
    public BooleanSet clone() {
        boolean[] bitsCopy = new boolean[this.bits.length];
        System.arraycopy(this.bits, 0, bitsCopy, 0, this.bits.length);
        return new BooleanSet(bitsCopy);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bits); // Potenzialmente OK, ma vedi sotto
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BooleanSet))
            return false;
        BooleanSet that = (BooleanSet) o;
        return Arrays.equals(this.bits, that.bits); // OK
    }

}
