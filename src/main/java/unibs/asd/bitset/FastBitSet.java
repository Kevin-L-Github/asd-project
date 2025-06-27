package unibs.asd.bitset;

import java.util.Arrays;

public class FastBitSet {

    private static final int ADDRESS_BITS_PER_WORD = 6; // 2^6 = 64
    private static final int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
    private static final int BIT_INDEX_MASK = BITS_PER_WORD - 1;

    final int logicalSize;
    long[] words;

    public FastBitSet(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Size must be non-negative");
        }
        this.logicalSize = size;
        this.words = new long[(size + 63) / 64];
    }

    public void set(int bitIndex) {
        words[bitIndex >> ADDRESS_BITS_PER_WORD] |= (1L << (bitIndex & BIT_INDEX_MASK));
    }

    public void clear(int bitIndex) {
        if (bitIndex >= logicalSize)
            return;
        int wordIndex = bitIndex >> ADDRESS_BITS_PER_WORD;
        if (wordIndex >= words.length)
            return;
        words[wordIndex] &= ~(1L << (bitIndex & BIT_INDEX_MASK));
    }

    public boolean get(int bitIndex) {
        int wordIndex = bitIndex >> ADDRESS_BITS_PER_WORD;
        if (wordIndex >= words.length)
            return false;
        return (words[wordIndex] & (1L << (bitIndex & BIT_INDEX_MASK))) != 0;
    }

    public void flip(int bitIndex) {
        words[bitIndex >> ADDRESS_BITS_PER_WORD] ^= (1L << (bitIndex & BIT_INDEX_MASK));
    }

    public int size() {
        return logicalSize;
    }

    public int cardinality() {
        int count = 0;
        int i = 0;
        int len = words.length;
        for (; i + 3 < len; i += 4) {
            count += Long.bitCount(words[i]);
            count += Long.bitCount(words[i + 1]);
            count += Long.bitCount(words[i + 2]);
            count += Long.bitCount(words[i + 3]);
        }
        for (; i < len; i++) {
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

        int wordIndex = fromIndex >> ADDRESS_BITS_PER_WORD;
        if (wordIndex >= words.length)
            return -1;

        long word = words[wordIndex] & (~0L << fromIndex);
        while (true) {
            if (word != 0) {
                int bitPos = (wordIndex << ADDRESS_BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
                return bitPos < logicalSize ? bitPos : -1;
            }
            if (++wordIndex >= words.length)
                return -1;
            word = words[wordIndex];
        }
    }

    public int previousSetBit(int fromIndex) {

        int wordIndex = fromIndex >>> 6; // Divisione per 64
        int bitInWord = fromIndex & 0x3F; // Modulo 64

        // Maschera per isolare i bit <= fromIndex nella parola corrente
        long mask = (bitInWord == 63) ? ~0L : (1L << (bitInWord + 1)) - 1;
        long word = words[wordIndex] & mask;

        if (word != 0) {
            return (wordIndex << 6) + (63 - Long.numberOfLeadingZeros(word));
        }

        // Cerca nelle parole precedenti
        for (int i = wordIndex - 1; i >= 0; i--) {
            if (words[i] != 0) {
                return (i << 6) + (63 - Long.numberOfLeadingZeros(words[i]));
            }
        }

        // Se nessun bit è settato in tutto il bitset, restituisci -1
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
        char[] bits = new char[logicalSize];
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

        int max = Math.min(this.words.length, other.words.length);
        for (int i = 0; i < max; i++) {
            if (this.words[i] != other.words[i]) {
                return false;
            }
        }

        for (int i = max; i < this.words.length; i++) {
            if (this.words[i] != 0)
                return false;
        }
        for (int i = max; i < other.words.length; i++) {
            if (other.words[i] != 0)
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        int len = words.length;
        while (len > 0 && words[len - 1] == 0) {
            len--;
        }

        for (int i = 0; i < len; i++) {
            long word = words[i];
            result = prime * result + (int) (word ^ (word >>> 32));
        }

        result = prime * result + logicalSize;

        return result;
    }
}