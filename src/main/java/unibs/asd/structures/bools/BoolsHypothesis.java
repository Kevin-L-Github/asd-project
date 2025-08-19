package unibs.asd.structures.bools;

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
        return this.bin.mostSignificantBit();
    }

    public int leastSignificantBit() {
        return this.bin.leastSignificantBit();
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
        return this.bin.isEmpty();
    }

    public BooleanSet getVector() {
        return this.vector.clone();
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
        return this.bin.size();
    }

    @Override
    public void set(int i) {
        this.bin.set(i);
    }

    @Override
    public void flip(int i) {
        this.bin.flip(i);
    }

    @Override
    public void or(Hypothesis other) {
        BooleanSet that = (BooleanSet) other.getVector();
        this.vector.or(that);
    }

    @Override
    public void set(int i, boolean value) {
        this.bin.set(i, value);
    }

    @Override
    public void update(BitVector information) {
        for (int i = 0; i < vector.size(); i++) {
            this.vector.set(i, vector.get(i) || information.get(i));
        }
    }

    @Override
    public boolean isSolution() {
        return this.vector.isFull();
    }

}