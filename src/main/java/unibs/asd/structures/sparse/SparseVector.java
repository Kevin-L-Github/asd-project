package unibs.asd.structures.sparse;

import com.zaxxer.sparsebits.SparseBitSet;
import unibs.asd.interfaces.BitVector;

public class SparseVector implements BitVector {

    private final SparseBitSet bitmap;
    private int size;

    public SparseVector(int size) {
        this.bitmap = new SparseBitSet(size);
        this.size = size;

    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean get(int index) {
        return this.bitmap.get(index);
    }

    @Override
    public void set(int index) {
        this.bitmap.set(index);
    }

    @Override
    public void set(int index, boolean value) {
        if (value) {
            this.bitmap.set(index);
        } else {
            this.bitmap.clear(index);
        }
    }

    @Override
    public void flip(int index) {
        this.bitmap.flip(index);
    }

    @Override
    public int cardinality() {
        return this.bitmap.cardinality();
    }

    @Override
    public boolean isEmpty() {
        return this.bitmap.isEmpty();
    }

    @Override
    public boolean isFull() {
        return this.bitmap.cardinality() == this.size;
    }

    @Override
    public int leastSignificantBit() {
    if (this.bitmap.isEmpty())
        return -1;  // Convention: -1 if no bit is set
    
    // Find the last (rightmost) bit set
    int lastBit = -1;
    for (int i = this.bitmap.nextSetBit(0); i >= 0; i = this.bitmap.nextSetBit(i + 1)) {
        lastBit = i;
        // Avoid overflow in the extreme case
        if (i == Integer.MAX_VALUE) {
            break;
        }
    }
    return lastBit;
}

    @Override
    public int mostSignificantBit() {
        return this.bitmap.nextSetBit(0);
    }

    @Override
    public String toBinaryString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(this.bitmap.get(i) ? '1' : '0');
        }
        return sb.toString();
    }

    @Override
    public SparseVector clone() {
        SparseVector cloned = new SparseVector(size);
        for (int i = this.bitmap.nextSetBit(0); i >= 0; i = this.bitmap.nextSetBit(i + 1)) {
            cloned.set(i);
        }
        return cloned;
    }

    public void or(SparseVector other) {
        this.bitmap.or(other.bitmap);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        SparseVector other = (SparseVector) obj;
        return this.bitmap.equals(other.bitmap);
    }

    @Override
    public int hashCode() {
        return bitmap.hashCode();
    }

}
