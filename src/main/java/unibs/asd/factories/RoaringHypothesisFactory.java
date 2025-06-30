package unibs.asd.factories;

import unibs.asd.interfaces.BitVector;
import unibs.asd.interfaces.Hypothesis;
import unibs.asd.interfaces.HypothesisFactory;
import unibs.asd.roaringbitmap.RoaringBitmapAdapter;
import unibs.asd.roaringbitmap.RoaringHypothesis;

public class RoaringHypothesisFactory implements HypothesisFactory {

    @Override
    public Hypothesis create(Object bin) {
        return new RoaringHypothesis((RoaringBitmapAdapter) bin);
    }

    @Override
    public Hypothesis create(int size) {
        return new RoaringHypothesis(size, 0);
    }

        @Override
    public BitVector createVector(int size) {
        return new RoaringBitmapAdapter(size);
    }

}
