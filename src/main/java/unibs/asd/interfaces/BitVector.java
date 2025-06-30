package unibs.asd.interfaces;

import java.math.BigInteger;
import java.util.BitSet;

public interface BitVector extends Cloneable {
    BitSet getBitVector();                // Ritorna il vettore di bit
    int size();                           // Lunghezza fissa (numero di bit)
    boolean get(int index);               // Legge il bit in posizione index
    void set(int index);                  // Imposta a 1 il bit in posizione index
    void set(int index, boolean value);   // Imposta a 1 o 0 il bit in posizione index
    void clear(int index);                // Resetta il bit in posizione index
    void flip(int index);                 // Inverte il bit in posizione index
    void clear();                         // Resetta tutti i bit
    int cardinality();                    // Numero di bit settati a 1
    boolean isEmpty();                    // True se tutti i bit sono a 0
    boolean isFull();                     // True se tutti i bit sono a 1
    void and(BitVector other);            // Operazione AND
    void or(BitVector other);             // OR
    void xor(BitVector other);            // XOR
    void andNot(BitVector other);         // AND NOT
    int leastSignificantBit();           // Massimo bit settato (LSB)
    int mostSignificantBit();            // Minimo bit settato (MSB)
    BigInteger toNaturalValue();         // Rappresentazione numerica
    String toBinaryString();             // Stringa MSB → LSB
    String toBinaryStringLSBFirst();     // Stringa LSB → MSB
    BitVector clone();
}
