package unibs.asd.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Classe che rappresenta un'ipotesi come un array di valori booleani.
 * Ogni elemento dell'array rappresenta uno stato binario (true = attivo, false
 * = inattivo).
 * Fornisce metodi per manipolare e analizzare la configurazione binaria.
 */
public class Hypothesis {

    private final boolean[] bin; // Array immutabile che rappresenta lo stato binario
    private boolean[] vector;
    private int cardinality = -1; // Cache per cardinalità

    /**
     * Costruttore che inizializza una nuova ipotesi della dimensione specificata.
     * Tutti i valori sono inizializzati a false.
     * 
     * @param size dimensione dell'ipotesi
     * @throws IllegalArgumentException se size è negativo
     */
    public Hypothesis(int size, int n) {
        if (size < 0) {
            throw new IllegalArgumentException("La dimensione non può essere negativa");
        }
        this.bin = new boolean[size];
        Arrays.fill(this.bin, false);
        this.vector = new boolean[n];
        Arrays.fill(this.vector, false);
    }

    public Hypothesis(boolean[] bin) {
        if (bin == null) {
            throw new IllegalArgumentException("L'array binario non può essere null");
        }
        this.bin = bin.clone();
        this.vector = new boolean[0];
    }

    public boolean[] getBin() {
        return bin.clone(); // Restituisce una copia per garantire l'immutabilità
    }

    public int size() {
        return bin.length;
    }
/* 
    public int numberOfSuccessors() {
        return bin.length - numberOfPredecessors();
    }

    private int numberOfPredecessors() {
        int count = 0;
        for (boolean b : bin) {
            if (b) {
                count++;
            }
        }
        return count;
    }
*/

    public int numberOfPredecessors() {
        if (cardinality == -1) cardinality = calculateCardinality();
        return cardinality;
    }

    private int calculateCardinality() {
        int count = 0;
        for (boolean b : bin) if (b) count++;
        return count;
    }

    public int mostSignificantBit() {
        for (int i = 0; i < bin.length; i++) {
            if (bin[i]) {
                return i;
            }
        }
        return -1;
    }

    public int leastSignificantBit() {
        for (int i = bin.length - 1; i >= 0; i--) {
            if (bin[i]) {
                return i;
            }
        }
        return -1;
    }

    public List<Hypothesis> getHseconds() {
        int msb = mostSignificantBit();
        int lsb = leastSignificantBit();
        if (msb == -1 || msb == 0 || lsb == -1) {
            throw new IllegalArgumentException("Non è possibile calcolare gli hsecondi per questa ipotesi");
        }

        List<Hypothesis> list = new ArrayList<>();
        /*
        for (int i = 0; i < msb; i++) {
            boolean[] h_prime = bin.clone();
            h_prime[i] = true;
            for (int j = i + 1; j <= lsb; j++) {
                if (h_prime[j]) {
                    boolean[] h_sec = h_prime.clone();
                    h_sec[j] = false;
                    list.add(new Hypothesis(h_sec));
                }
            }
        }
        */
        boolean[] base = bin.clone();
        base[msb] = false; // Rimuovi MSB una volta sola

        for (int i = 0; i < msb; i++) {
            if (base[i]) continue; // Salta bit già attivi
            boolean[] hSec = base.clone();
            hSec[i] = true;
            list.add(new Hypothesis(hSec));
        }
        return list;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (boolean b : bin) {
            sb.append(b ? "1" : "0");
        }
        return sb.toString();
    }

    public Hypothesis globalInitial() {
        if (bin.length == 0) {
            throw new IllegalArgumentException("L'ipotesi non può essere vuota");
        }
        if (bin.length <= 1 || bin[0]) {
            throw new IllegalArgumentException("bin(h)[1] deve essere 0");
        }

        boolean[] globalInitialBin = bin.clone();
        globalInitialBin[0] = !globalInitialBin[0];

        int lsb = leastSignificantBit();
        if (lsb != -1) {
            globalInitialBin[lsb] = !globalInitialBin[lsb];
        }

        return new Hypothesis(globalInitialBin);
    }

    public Hypothesis initial_(Hypothesis hPrime) {

        if (numberOfPredecessors() == 0) {
            return hPrime;
        }

        for (int i = this.size() - 1; i >= 0; i--) {
            if (bin[i]) {
                boolean[] predecessor = hPrime.getBin().clone();
                predecessor[i] = false;
                Hypothesis pred = new Hypothesis(predecessor);
                if (!this.equals(pred)) {
                    return pred;
                }
            }
        }
        return null;
    }

    public Hypothesis final_(Hypothesis hPrime) {

        if (numberOfPredecessors() == 0) {
            return hPrime;
        }
        for (int i = 0; i < this.size(); i++) {
            if (bin[i]) {
                boolean[] predecessor = hPrime.getBin().clone();
                predecessor[i] = false;
                Hypothesis pred = new Hypothesis(predecessor);
                if (!hPrime.equals(pred)) {
                    return pred;
                }
            }
        }
        return null;
    }

    public int cardinality() {
        int cardinality = 0;
        for (boolean b : bin) {
            if (b) {
                cardinality++;
            }
        }
        return cardinality;
    }

    public boolean isEmptyHypothesis() {
        for (boolean b : this.getBin()) {
            if (b) {
                return false;
            }
        }
        return true;
    }

    public boolean[] getVector() {
        return vector.clone();
    }

    public void setVector(boolean[] vector) {
        this.vector = vector.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Ottimizzazione per lo stesso oggetto
        // if (o == null || getClass() != o.getClass())
        if (!(o instanceof Hypothesis)) return false; // Controllo di tipo

        Hypothesis that = (Hypothesis) o;
        // Confronta gli array elemento per elemento
        return Arrays.equals(this.bin, that.bin);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bin); // Coerente con Arrays.equals()
    }

    public List<Hypothesis> predecessors() {
        List<Hypothesis> predecessors = new ArrayList<>();
        for (int i = 0; i < this.bin.length; i++){
            if(bin[i]){
                boolean[] h_prime = this.getBin().clone();
                h_prime[i] =!h_prime[i];
                predecessors.add(new Hypothesis(h_prime));
            }
        }
        return predecessors;
    }

}