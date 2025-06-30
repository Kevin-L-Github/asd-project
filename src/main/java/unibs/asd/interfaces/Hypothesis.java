package unibs.asd.interfaces;

import java.util.List;

public interface Hypothesis extends Cloneable {

    BitVector getBin();

    BitVector getVector();

    Hypothesis globalInitial();

    int mostSignificantBit();

    int cardinality();

    List<Hypothesis> predecessors();

    boolean equals(Object o);

    void setVector(BitVector vector);

    int length();

    Hypothesis clone();

    void set(int i);

    void set(int i, boolean value);

    void flip(int i);

    void or(Hypothesis other);

    

}
