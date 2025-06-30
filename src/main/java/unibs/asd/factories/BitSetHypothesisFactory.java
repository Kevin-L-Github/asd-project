package unibs.asd.factories;

import unibs.asd.bitset.BitSetAdapter;
import unibs.asd.bitset.BitSetHypothesis;
import unibs.asd.interfaces.Hypothesis;
import unibs.asd.interfaces.HypothesisFactory;

public class BitSetHypothesisFactory implements HypothesisFactory {
    @Override
    public Hypothesis create(Object bin) {
        return new BitSetHypothesis((BitSetAdapter) bin);
    }

}
