package unibs.asd.factories;

import unibs.asd.interfaces.BitVector;
import unibs.asd.interfaces.Hypothesis;
import unibs.asd.interfaces.HypothesisFactory;
import unibs.asd.structures.bools.BooleanSet;
import unibs.asd.structures.bools.BoolsHypothesis;

public class BoolsHypothesisFactory implements HypothesisFactory {

    @Override
    public Hypothesis create(Object bin) {
        return new BoolsHypothesis((BooleanSet) bin);
    }

    @Override
    public Hypothesis create(int size) {
        return new BoolsHypothesis(size, 0);
    }

        @Override
    public BitVector createVector(int size) {
        return new BooleanSet(size);
    }

}
