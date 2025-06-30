package unibs.asd.factories;

import unibs.asd.bools.BooleanSet;
import unibs.asd.bools.BoolsHypothesis;
import unibs.asd.interfaces.Hypothesis;
import unibs.asd.interfaces.HypothesisFactory;

public class BoolsHypothesisFactory implements HypothesisFactory {

    @Override
    public Hypothesis create(Object bin) {
        return new BoolsHypothesis((BooleanSet) bin);
    }

}
