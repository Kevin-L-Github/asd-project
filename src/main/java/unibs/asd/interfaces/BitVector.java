package unibs.asd.interfaces;

import java.math.BigInteger;

public interface BitVector extends Cloneable {
    int size();                           // Lunghezza fissa (numero di bit)
    boolean get(int index);               // Legge il bit in posizione index
    void set(int index);                  // Imposta a 1 il bit in posizione index
    void set(int index, boolean value);   // Imposta a 1 o 0 il bit in posizione index
    void flip(int index);                 // Inverte il bit in posizione index
    int cardinality();                    // Numero di bit settati a 1
    boolean isEmpty();                    // True se tutti i bit sono a 0
    boolean isFull();                     // True se tutti i bit sono a 1
    int leastSignificantBit();           // Massimo bit settato (LSB)
    int mostSignificantBit();            // Minimo bit settato (MSB)
    BigInteger toNaturalValue();         // Rappresentazione numerica
    String toBinaryString();             // Stringa MSB → LSB
    BitVector clone();
}
