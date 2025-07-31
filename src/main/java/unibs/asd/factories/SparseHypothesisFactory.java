package unibs.asd.factories;

import unibs.asd.interfaces.BitVector;
import unibs.asd.interfaces.Hypothesis;
import unibs.asd.interfaces.HypothesisFactory;
import unibs.asd.structures.sparse.SparseHypothesis;
import unibs.asd.structures.sparse.SparseVector;

public class SparseHypothesisFactory implements HypothesisFactory {

    @Override
    public Hypothesis create(Object bin) {
        return new SparseHypothesis((SparseVector) bin);
    }

    @Override
    public Hypothesis create(int size) {
        return new SparseHypothesis(size, 0);
    }

    @Override
    public BitVector createVector(int size) {
        return new SparseVector(size);
    }
}
