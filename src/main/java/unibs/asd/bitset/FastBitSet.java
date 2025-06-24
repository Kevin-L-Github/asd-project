package unibs.asd.bitset;

import java.util.Arrays;

public class FastBitSet {
    private final int logicalSize;
    long[] words; // Ogni long contiene 64 bit

    public FastBitSet(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Size must be non-negative");
        }
        this.logicalSize = size;
        this.words = new long[(size + 63) / 64];
    }

    public void set(int bitIndex) {
        int wordIndex = bitIndex / 64;
        int bitPosition = bitIndex % 64;
        words[wordIndex] |= (1L << bitPosition);
    }

    public void clear(int bitIndex) {
        if (bitIndex >= logicalSize)
            return;
        int wordIndex = bitIndex / 64;
        if (wordIndex >= words.length)
            return;
        int bitPosition = bitIndex % 64;
        words[wordIndex] &= ~(1L << bitPosition);
    }

    public boolean get(int bitIndex) {
        int wordIndex = bitIndex / 64;
        if (wordIndex >= words.length)
            return false;
        int bitPosition = bitIndex % 64;
        return (words[wordIndex] & (1L << bitPosition)) != 0;
    }

    public void flip(int bitIndex) {
        int wordIndex = bitIndex / 64;
        int bitPosition = bitIndex % 64;
        words[wordIndex] ^= (1L << bitPosition);
    }

    public int size() {
        return logicalSize;
    }

    public int cardinality() {
        int count = 0;
        for (int i = 0; i < words.length; i++) {
            count += Long.bitCount(words[i]);
        }
        return count;
    }

    public void or(FastBitSet other) {
        if (this.logicalSize != other.logicalSize) {
            throw new IllegalArgumentException("Bitsets must have the same logical size");
        }
        for (int i = 0; i < words.length && i < other.words.length; i++) {
            words[i] |= other.words[i];
        }
    }

    public void and(FastBitSet other) {
        if (this.logicalSize != other.logicalSize) {
            throw new IllegalArgumentException("Bitsets must have the same logical size");
        }
        for (int i = 0; i < words.length; i++) {
            if (i < other.words.length) {
                words[i] &= other.words[i];
            } else {
                words[i] = 0;
            }
        }
    }

    public void xor(FastBitSet other) {
        if (this.logicalSize != other.logicalSize) {
            throw new IllegalArgumentException("Bitsets must have the same logical size");
        }
        for (int i = 0; i < words.length && i < other.words.length; i++) {
            words[i] ^= other.words[i];
        }
    }

    public int nextSetBit(int fromIndex) {
        if (fromIndex >= logicalSize)
            return -1;

        int wordIndex = fromIndex / 64;
        if (wordIndex >= words.length)
            return -1;

        long word = words[wordIndex] >>> (fromIndex % 64);
        if (word != 0) {
            int result = fromIndex + Long.numberOfTrailingZeros(word);
            return result < logicalSize ? result : -1;
        }

        for (int i = wordIndex + 1; i < words.length; i++) {
            if (words[i] != 0) {
                int result = i * 64 + Long.numberOfTrailingZeros(words[i]);
                return result < logicalSize ? result : -1;
            }
        }
        return -1;
    }

    public int previousSetBit(int fromIndex) {
        if (fromIndex < 0)
            return -1;
        if (fromIndex >= logicalSize)
            fromIndex = logicalSize - 1;

        int wordIndex = fromIndex / 64;
        long word = words[wordIndex] & ((1L << ((fromIndex % 64) + 1)) - 1);
        if (word != 0) {
            int bitPos = 63 - Long.numberOfLeadingZeros(word);
            return (wordIndex * 64) + bitPos;
        }

        for (int i = wordIndex - 1; i >= 0; i--) {
            if (words[i] != 0) {
                int bitPos = 63 - Long.numberOfLeadingZeros(words[i]);
                return (i * 64) + bitPos;
            }
        }

        return -1;
    }

    @Override
    public FastBitSet clone() {
        FastBitSet copy = new FastBitSet(this.logicalSize);
        copy.words = Arrays.copyOf(this.words, this.words.length);
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
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < logicalSize; i++) {
            sb.append(get(i) ? '1' : '0');
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof FastBitSet))
            return false;

        FastBitSet other = (FastBitSet) obj;

        int thisLast = this.words.length - 1;
        while (thisLast >= 0 && this.words[thisLast] == 0) {
            thisLast--;
        }

        int otherLast = other.words.length - 1;
        while (otherLast >= 0 && other.words[otherLast] == 0) {
            otherLast--;
        }

        if (thisLast != otherLast)
            return false;

        for (int i = 0; i <= thisLast; i++) {
            if (this.words[i] != other.words[i])
                return false;
        }

        return true;
    }

    private void checkIndex(int bitIndex) {
        if (bitIndex < 0 || bitIndex >= logicalSize) {
            throw new IndexOutOfBoundsException("Bit index " + bitIndex +
                    " out of bounds for size " + logicalSize);
        }
    }
}