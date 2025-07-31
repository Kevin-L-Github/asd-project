package unibs.asd.structures.fastbitset;

import java.util.Arrays;

import unibs.asd.interfaces.BitVector;

public class FastBitSet implements BitVector{

    public final int logicalSize;
    final long[] words;

    public long[] words() {
        return words;
    }

    public FastBitSet(int size) {
        this.logicalSize = size;
        this.words = new long[(size + 63) >>> 6];
    }

    public void set(int bitIndex) {
        words[bitIndex >>> 6] |= (1L << bitIndex);
    }

    public void clear(int bitIndex) {
        words[bitIndex >>> 6] &= ~(1L << bitIndex);
    }

    public void flip(int bitIndex) {
        words[bitIndex >>> 6] ^= (1L << bitIndex);
    }

    public boolean get(int bitIndex) {
        return (words[bitIndex >>> 6] & (1L << bitIndex)) != 0;
    }

    public int size() {
        return logicalSize;
    }

    public int cardinality() {
        int count = 0;
        final int len = words.length;

        int i = 0;
        for (; i + 7 < len; i += 8) {
            count += Long.bitCount(words[i])
                    + Long.bitCount(words[i + 1])
                    + Long.bitCount(words[i + 2])
                    + Long.bitCount(words[i + 3])
                    + Long.bitCount(words[i + 4])
                    + Long.bitCount(words[i + 5])
                    + Long.bitCount(words[i + 6])
                    + Long.bitCount(words[i + 7]);
        }

        for (; i < len; i++) {
            count += Long.bitCount(words[i]);
        }

        return count;
    }

    public void or(FastBitSet other) {
        final long[] otherWords = other.words;
        for (int i = 0; i < words.length; i++) {
            words[i] |= otherWords[i];
        }
    }

    public void and(FastBitSet other) {
        final long[] otherWords = other.words;
        for (int i = 0; i < words.length; i++) {
            words[i] &= otherWords[i];
        }
    }

    public void andNot(FastBitSet other) {
        final long[] otherWords = other.words;
        for (int i = 0; i < words.length; i++) {
            words[i] &= ~otherWords[i];
        }
    }

    public void xor(FastBitSet other) {
        final long[] otherWords = other.words;
        for (int i = 0; i < words.length; i++) {
            words[i] ^= otherWords[i];
        }
    }

    public int nextSetBit(int fromIndex) {
        int wordIndex = fromIndex >>> 6;
        if (wordIndex >= words.length)
            return -1;

        long word = words[wordIndex] & (~0L << fromIndex);

        while (true) {
            if (word != 0) {
                return (wordIndex << 6) + Long.numberOfTrailingZeros(word);
            }
            if (++wordIndex == words.length)
                return -1;
            word = words[wordIndex];
        }
    }

    public int previousSetBit(int fromIndex) {

        int wordIndex = fromIndex >>> 6;
        int bitInWord = fromIndex & 0x3F;
        long mask = (bitInWord == 63) ? ~0L : (1L << (bitInWord + 1)) - 1;
        long word = words[wordIndex] & mask;

        if (word != 0) {
            return (wordIndex << 6) + (63 - Long.numberOfLeadingZeros(word));
        }

        for (int i = wordIndex - 1; i >= 0; i--) {
            if (words[i] != 0) {
                return (i << 6) + (63 - Long.numberOfLeadingZeros(words[i]));
            }
        }
        return -1;
    }

    @Override
    public FastBitSet clone() {
        FastBitSet copy = new FastBitSet(this.logicalSize);
        System.arraycopy(this.words, 0, copy.words, 0, this.words.length);
        return copy;
    }

    public void set(int bitIndex, boolean value) {
        if (value) {
            set(bitIndex);
        } else {
            clear(bitIndex);
        }
    }

    @Override
    public String toString() {
        final char[] bits = new char[logicalSize];
        Arrays.fill(bits, '0');

        for (int i = 0; i < logicalSize; i++) {
            if (get(i)) {
                bits[i] = '1';
            }
        }
        return new String(bits);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof FastBitSet))
            return false;

        FastBitSet other = (FastBitSet) obj;
        if (this.logicalSize != other.logicalSize)
            return false;

        int i = words.length;
        while (i-- > 0) {
            if (words[i] != other.words[i])
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        int i = words.length;

        while (i > 0 && words[i - 1] == 0)
            i--;

        for (int j = 0; j < i; j++) {
            long word = words[j];
            result = 31 * result + (int) (word ^ (word >>> 32));
        }

        return 31 * result + logicalSize;
    }

    @Override
    public boolean isEmpty() {
        return this.cardinality() == 0;
    }

    @Override
    public boolean isFull() {
        return this.cardinality() == this.logicalSize;
    }

    @Override
    public int leastSignificantBit() {
        return this.previousSetBit(this.logicalSize-1);
    }

    @Override
    public int mostSignificantBit() {
        return this.nextSetBit(0);
    }

    @Override
    public String toBinaryString() {
        return toString();
    }
}