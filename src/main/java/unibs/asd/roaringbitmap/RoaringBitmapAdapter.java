package unibs.asd.roaringbitmap;

import java.math.BigInteger;

import org.roaringbitmap.RoaringBitmap;

public class RoaringBitmapAdapter {
    private final RoaringBitmap bitmap;
    private final int size;

    public RoaringBitmapAdapter(int size) {
        if (size <= 0)
            throw new IllegalArgumentException("Size must be > 0");
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

    public BigInteger toNaturalValue() {
        BigInteger value = BigInteger.ZERO;
        for (int i = 0; i < size; i++) {
            if (bitmap.contains(i)) {
                int power = size - 1 - i;
                value = value.setBit(power);
            }
        }
        return value;
    }

    public void set(int index, boolean value) {
        if (value) {
            bitmap.add(index);
        } else {
            bitmap.remove(index);
        }
    }

    public boolean isFull(){
        return this.bitmap.getCardinality() == size;
    }

}
