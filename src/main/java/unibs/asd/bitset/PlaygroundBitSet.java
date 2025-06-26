package unibs.asd.bitset;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.roaringbitmap.RoaringBitmap;

public class PlaygroundBitSet {

    public static void main(String[] args) {

        // Crea due bitmap
        RoaringBitmap bitmap1 = new RoaringBitmap();
        RoaringBitmap bitmap2 = new RoaringBitmap();

        // Aggiunge alcuni valori
        bitmap1.add(1);
        bitmap1.add(2);
        bitmap1.add(100);

        bitmap2.add(2);
        bitmap2.add(1000);

        // Intersezione tra i due bitmap
        RoaringBitmap intersection = RoaringBitmap.and(bitmap1, bitmap2);

        // Stampa il contenuto
        System.out.println("Bitmap 1: " + bitmap1);
        System.out.println("Bitmap 2: " + bitmap2);
        System.out.println("Intersezione: " + intersection);
    }

    public static BigInteger toBigInteger(FastBitSet bitSet) {
        int logicalSize = bitSet.size();
        if (logicalSize == 0)
            return BigInteger.ZERO;

        int byteLength = (logicalSize + 7) / 8;
        byte[] bytes = new byte[byteLength];

        for (int i = 0; i < logicalSize; i++) {
            if (bitSet.get(i)) {
                int byteIndex = i / 8;
                int bitInByte = 7 - (i % 8); // Inverti la posizione del bit nel byte
                bytes[byteIndex] |= (1 << bitInByte);
            }
        }

        return new BigInteger(1, bytes);
    }

}
