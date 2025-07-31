package unibs.asd.factories;

import unibs.asd.interfaces.BitVector;
import unibs.asd.interfaces.Hypothesis;
import unibs.asd.interfaces.HypothesisFactory;
import unibs.asd.structures.bitset.BitSetAdapter;
import unibs.asd.structures.bitset.BitSetHypothesis;

public class BitSetHypothesisFactory implements HypothesisFactory {
    @Override
    public Hypothesis create(Object bin) {
        return new BitSetHypothesis((BitSetAdapter) bin);
    }

    @Override
    public Hypothesis create(int size) {
        return new BitSetHypothesis(size, 0);
    }

    @Override
    public BitVector createVector(int size) {
        return new BitSetAdapter(size);
    }

}
