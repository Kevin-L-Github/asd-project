package unibs.asd.bitset;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class PlaygroundBitSet {

    public static void main(String[] args) {

        FastBitSet prova = new FastBitSet(8);

        prova.set(4);
        prova.set(5);

        System.out.println("prova : " + prova.toString());

        FastBitSet prova1 = new FastBitSet(8);

        prova1.set(4);
        prova1.set(5);

        System.out.println(prova.equals(prova1));

        System.out.println("prova1: " + prova1.toString());

        prova1.and(prova);

        System.out.println("result: " + prova1);

        List<FastBitSet> children = new ArrayList<>();

        for (int i = 0; i < prova.size(); i++) {
            FastBitSet c = prova.clone();
            c.set(i);
            children.add(c);
        }
        children.stream().forEach(c -> System.out.println(toBigInteger(c)));

        System.out.println(children.getLast().previousSetBit(8));

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
