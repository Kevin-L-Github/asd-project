package unibs.asd.interfaces;

public interface HypothesisFactory {
    Hypothesis<BitVector> create(BitVector bin);
}
