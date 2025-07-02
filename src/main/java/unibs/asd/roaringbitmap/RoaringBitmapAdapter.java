package unibs.asd.roaringbitmap;

import org.roaringbitmap.IntIterator;
import org.roaringbitmap.RoaringBitmap;

import unibs.asd.interfaces.BitVector;

public class RoaringBitmapAdapter implements BitVector {

    private final RoaringBitmap bitmap;
    private final int size;

    public RoaringBitmapAdapter(int size) {
        this.size = size;
        this.bitmap = new RoaringBitmap();
    }

    public boolean get(int index) {
        return bitmap.contains(index);
    }

    public void set(int index) {
        bitmap.add(index);
    }

    public void clear(int index) {
        bitmap.remove(index);
    }

    public void flip(int index) {
        if (bitmap.contains(index))
            bitmap.remove(index);
        else
            bitmap.add(index);
    }

    public int cardinality() {
        return bitmap.getCardinality();
    }

    public boolean isEmpty() {
        return bitmap.isEmpty();
    }

    public int size() {
        return this.size;
    }

    public RoaringBitmapAdapter clone() {
        RoaringBitmapAdapter copy = new RoaringBitmapAdapter(this.size);
        copy.bitmap.or(this.bitmap);
        return copy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            sb.append(bitmap.contains(i) ? '1' : '0');
        }
        return sb.toString();
    }

    public void or(RoaringBitmapAdapter other) {
        this.bitmap.or(other.bitmap);
    }

    public void xor(RoaringBitmapAdapter other) {
        this.bitmap.xor(other.bitmap);
    }

    public int mostSignificantBit() {
        if (bitmap.isEmpty())
            return -1;
        return bitmap.getIntIterator().next();
    }

    public int leastSignificantBit() {
        if (bitmap.isEmpty())
            return -1;
        return bitmap.getReverseIntIterator().next();
    }

    public void set(int index, boolean value) {
        if (value) {
            bitmap.add(index);
        } else {
            bitmap.remove(index);
        }
    }

    public boolean isFull() {
        return this.bitmap.getCardinality() == size;
    }

    @Override
    public String toBinaryString() {
        return this.toString();
    }

    @Override
    public int hashCode() {
        int result = bitmap.selectRange(0, size).hashCode();
        result = 31 * result + size;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RoaringBitmapAdapter))
            return false;

        RoaringBitmapAdapter that = (RoaringBitmapAdapter) o;
        if (this.size != that.size)
            return false;

        if (this.bitmap.getCardinality() != that.bitmap.getCardinality())
            return false;

        IntIterator thisIt = this.bitmap.getIntIterator();
        IntIterator thatIt = that.bitmap.getIntIterator();

        while (thisIt.hasNext() && thatIt.hasNext()) {
            if (thisIt.next() != thatIt.next()) {
                return false;
            }
        }
        return !(thisIt.hasNext() || thatIt.hasNext());
    }

}
