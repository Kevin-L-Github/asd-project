package unibs.asd.interfaces;

public interface HypothesisFactory {
    Hypothesis create(Object bin);
    Hypothesis create(int size);
    BitVector createVector(int size);
}
