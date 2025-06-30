package unibs.asd.interfaces;

import java.util.List;

public interface Hypothesis<T extends BitVector> extends Cloneable {
    T getBin();
    T getVector();
    Hypothesis<T> clone();
    Hypothesis<T> globalInitial();
    int mostSignificantBit();
    int cardinality();
    List<Hypothesis<T>> predecessors();
    boolean equals(Object o);
}
