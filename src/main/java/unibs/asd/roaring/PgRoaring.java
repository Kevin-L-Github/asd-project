package unibs.asd.roaring;

public class PgRoaring {
    public static void main(String[] args) {

        RoaringBitmapAdapter bitmap = new RoaringBitmapAdapter(10);
        
        bitmap.set(1, true);
        bitmap.set(3, true);
        bitmap.set(5, false); // non cambia nulla
        System.out.println(bitmap);
        System.out.println(bitmap.toNaturalValue()); // Output: 10 in decimale (2^1 + 2^3 = 2 + 8 = 10)

    }
}
