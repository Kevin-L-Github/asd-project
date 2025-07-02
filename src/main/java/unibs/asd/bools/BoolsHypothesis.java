package unibs.asd.bools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import unibs.asd.interfaces.BitVector;
import unibs.asd.interfaces.Hypothesis;

/**
 * Classe che rappresenta un'ipotesi come un array di valori booleani.
 * Ogni elemento dell'array rappresenta uno stato binario (true = attivo, false
 * = inattivo).
 * Fornisce metodi per manipolare e analizzare la configurazione binaria.
 */
public class BoolsHypothesis implements Hypothesis {

    private final BooleanSet bin; // Array immutabile che rappresenta lo stato binario
    private BooleanSet vector;
    private int cardinality = -1; // Cache per cardinalità

    /**
     * Costruttore che inizializza una nuova ipotesi della dimensione specificata.
     * Tutti i valori sono inizializzati a false.
     * 
     * @param size dimensione dell'ipotesi
     * @throws IllegalArgumentException se size è negativo
     */
    public BoolsHypothesis(int size, int n) {
        if (size < 0) {
            throw new IllegalArgumentException("La dimensione non può essere negativa");
        }
        this.bin = new BooleanSet(size);
        this.vector = new BooleanSet(n);
    }

    public BoolsHypothesis(BooleanSet bin) {
        if (bin == null) {
            throw new IllegalArgumentException("L'array binario non può essere null");
        }
        this.bin = bin.clone();
        this.vector = new BooleanSet(0);
    }

    public BooleanSet getBin() {
        return bin.clone(); // Restituisce una copia per garantire l'immutabilità
    }

    public int size() {
        return this.bin.size();
    }

    public int numberOfPredecessors() {
        if (cardinality == -1)
            cardinality = calculateCardinality();
        return cardinality;
    }

    private int calculateCardinality() {
        int count = 0;
        for (boolean b : bin.getBools())
            if (b)
                count++;
        return count;
    }

    public int mostSignificantBit() {
        for (int i = 0; i < bin.size(); i++) {
            if (bin.get(i)) {
                return i;
            }
        }
        return -1;
    }

    public int leastSignificantBit() {
        for (int i = bin.size() - 1; i >= 0; i--) {
            if (bin.get(i)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (boolean b : bin.getBools()) {
            sb.append(b ? "1" : "0");
        }
        return sb.toString();
    }

    public BoolsHypothesis globalInitial() {
        if (bin.size() == 0) {
            throw new IllegalArgumentException("L'ipotesi non può essere vuota");
        }
        if (bin.size() <= 1 || bin.get(0)) {
            throw new IllegalArgumentException("bin(h)[1] deve essere 0");
        }

        BooleanSet globalInitialBin = bin.clone();
        globalInitialBin.flip(0);

        int lsb = leastSignificantBit();
        if (lsb != -1) {
            globalInitialBin.flip(lsb);
        }

        return new BoolsHypothesis(globalInitialBin);
    }

    public BoolsHypothesis initial_(BoolsHypothesis hPrime) {

        if (numberOfPredecessors() == 0) {
            return hPrime;
        }

        for (int i = this.size() - 1; i >= 0; i--) {
            if (bin.get(i)) {
                BooleanSet predecessor = hPrime.getBin().clone();
                predecessor.set(i, false);
                BoolsHypothesis pred = new BoolsHypothesis(predecessor);
                if (!this.equals(pred)) {
                    return pred;
                }
            }
        }
        return null;
    }

    public BoolsHypothesis final_(BoolsHypothesis hPrime) {

        if (numberOfPredecessors() == 0) {
            return hPrime;
        }
        for (int i = 0; i < this.size(); i++) {
            if (bin.get(i)) {
                BooleanSet predecessor = hPrime.getBin().clone();
                predecessor.set(i, false);
                BoolsHypothesis pred = new BoolsHypothesis(predecessor);
                if (!hPrime.equals(pred)) {
                    return pred;
                }
            }
        }
        return null;
    }

    public int cardinality() {
        int cardinality = 0;
        for (boolean b : bin.getBools()) {
            if (b) {
                cardinality++;
            }
        }
        return cardinality;
    }

    public boolean isEmptyHypothesis() {
        for (boolean b : bin.getBools()) {
            if (b) {
                return false;
            }
        }
        return true;
    }

    public BooleanSet getVector() {
        return vector.clone();
    }

    public void setVector(BooleanSet vector) {
        this.vector = vector.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BoolsHypothesis))
            return false;
        BoolsHypothesis that = (BoolsHypothesis) o;
        return Arrays.equals(this.bin.getBools(), that.bin.getBools());
    }

    @Override
    public BoolsHypothesis clone() {
        return new BoolsHypothesis(this.bin.clone());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bin.getBools()); // Coerente con Arrays.equals()
    }

    public List<Hypothesis> predecessors() {
        List<Hypothesis> predecessors = new ArrayList<>();
        for (int i = 0; i < this.bin.size(); i++) {
            if (bin.get(i)) {
                BooleanSet h_prime = this.getBin().clone();
                h_prime.flip(i);
                predecessors.add(new BoolsHypothesis(h_prime));
            }
        }
        return predecessors;
    }

    @Override
    public void setVector(BitVector vector) {
        this.vector = (BooleanSet) vector;
    }

    @Override
    public int length() {
        return this.size();
    }

    @Override
    public void set(int i) {
        this.bin.set(i);
    }

    @Override
    public void flip(int i) {
        this.flip(i);
    }

    @Override
    public void or(Hypothesis other) {
        BooleanSet that = (BooleanSet) other.getVector();
        BooleanSet result = new BooleanSet(that.size());
        for (int i = 0; i < vector.size(); i++) {
            result.set(i, vector.get(i) || that.get(i));
        }
        this.vector = result;
    }

    @Override
    public void set(int i, boolean value) {
        this.bin.set(i, value);
    }

    @Override
    public void update(BitVector information) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

}