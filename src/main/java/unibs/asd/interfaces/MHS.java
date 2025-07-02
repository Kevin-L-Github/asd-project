package unibs.asd.interfaces;

import java.util.List;

import unibs.asd.enums.BitSetType;

public interface MHS {

    List<Hypothesis> run(BitSetType type, long timeoutMillis);
    List<Hypothesis> getSolutions();
    boolean[][] getInstance();
    boolean[][] getMatrix();
    List<Integer> getNonEmptyColumns();
    int getDEPTH();
    double getComputationTime();
    boolean isExecuted();
    boolean isStopped();
    boolean isStoppedInsideLoop();
    
}
