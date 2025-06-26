package unibs.asd.roaring;

public class PgRoaring {
    public static void main(String[] args) {

        int logicalSize = 1000;
        RoaringBitmapAdapter bitmap = new RoaringBitmapAdapter(logicalSize);

        System.out.println("Inizio test con size = " + logicalSize);

        // Settiamo alcuni bit
        bitmap.set(10);
        bitmap.set(200);
        bitmap.set(999);

        assert bitmap.get(10) == true;
        assert bitmap.get(200) == true;
        assert bitmap.get(999) == true;
        assert bitmap.get(500) == false;

        System.out.println("Cardinalità (bit a 1): " + bitmap.cardinality()); // Dovrebbe essere 3

        // Flip
        bitmap.flip(200); // Toglie il bit
        bitmap.flip(300); // Aggiunge il bit

        assert bitmap.get(200) == false;
        assert bitmap.get(300) == true;

        System.out.println("Cardinalità dopo flip: " + bitmap.cardinality()); // Dovrebbe essere 3

        // Clear
        bitmap.clear(10);
        bitmap.clear(999);

        assert bitmap.get(10) == false;
        assert bitmap.get(999) == false;

        System.out.println("Cardinalità dopo clear: " + bitmap.cardinality()); // Dovrebbe essere 1

        // Clone e confronto
        RoaringBitmapAdapter clone = bitmap.clone();
        assert clone.get(300) == true;
        assert clone.cardinality() == 1;
        assert clone.size() == bitmap.size();

        // IsEmpty
        bitmap.clear(300);
        assert bitmap.isEmpty() == true;

        System.out.println("Tutti i test superati.");

    }
}
