package unibs.asd.factories;

import unibs.asd.bitset.BitSetHypothesis;
import unibs.asd.interfaces.Hypothesis;
import unibs.asd.interfaces.HypothesisFactory;
import unibs.asd.interfaces.BitVector;

public class BitSetHypothesisFactory implements HypothesisFactory {
    @Override
    public Hypothesis<BitVector> create(BitVector bin) {
        return new BitSetHypothesis(bin);
    }
}
