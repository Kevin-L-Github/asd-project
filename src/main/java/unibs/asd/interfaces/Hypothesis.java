package unibs.asd.interfaces;

import java.util.List;

public interface Hypothesis<T extends BitVector> {
    T getBin();
    T getVector();
    Hypothesis<T> globalInitial();
    int mostSignificantBit();
    int cardinality();
    List<Hypothesis<T>> predecessors();
    boolean equals(Object o);
    void setVector(BitVector vector);
    int length();
    
}
