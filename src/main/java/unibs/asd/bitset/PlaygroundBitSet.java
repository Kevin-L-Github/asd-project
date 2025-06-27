package unibs.asd.bitset;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.roaringbitmap.RoaringBitmap;

public class PlaygroundBitSet {

    public static void main(String[] args) {

        FastBitSet a = new FastBitSet(64);
        a.set(63); // Imposta il bit più significativo
        FastBitSet b = new FastBitSet(64);
        b.set(63); // Imposta il secondo bit più significativo

        boolean result = FastMHS.isGreater(a, b);
        System.out.println(result);
        // result sarà true perché a > b
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
