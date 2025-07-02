package unibs.asd.factories;

import unibs.asd.fastbitset.FastBitSet;
import unibs.asd.fastbitset.FastHypothesis;
import unibs.asd.interfaces.BitVector;
import unibs.asd.interfaces.Hypothesis;
import unibs.asd.interfaces.HypothesisFactory;

public class FastBitSetHypothesisFactory implements HypothesisFactory {

    @Override
    public Hypothesis create(Object bin) {
        return new FastHypothesis((FastBitSet) bin);
    }

    @Override
    public Hypothesis create(int size) {
        return new FastHypothesis(size, 0);
    }

    @Override
    public BitVector createVector(int size) {
        return new FastBitSet(size);
    }

}
