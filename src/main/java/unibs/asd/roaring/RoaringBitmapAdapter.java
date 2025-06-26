package unibs.asd.roaring;

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

}
