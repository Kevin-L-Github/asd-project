package unibs.asd.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Classe che rappresenta un'ipotesi come un array di valori booleani.
 * Ogni elemento dell'array rappresenta uno stato binario (true = attivo, false
 * = inattivo).
 * Fornisce metodi per manipolare e analizzare la configurazione binaria.
 */
public class Hypothesis {

    private final boolean[] bin; // Array immutabile che rappresenta lo stato binario
    private boolean[] vector;

    public Hypothesis(boolean[] bin, boolean[] vector) {
        this.bin = bin.clone();
        this.vector = vector.clone();
    }

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

    public int numberOfSuccessors() {
        return bin.length - numberOfPredecessors();
    }

    public int numberOfPredecessors() {
        int count = 0;
        for (boolean b : bin) {
            if (b) {
                count++;
            }
        }
        return count;
    }

    /**
     * Genera tutti i successori dell'ipotesi corrente come oggetti Hypothesis.
     */
    public List<Hypothesis> leftSuccessors() {
        return generateSuccessors(0, mostSignificantBit());
    }

    public List<Hypothesis> rightSuccessors() {
        return generateSuccessors(mostSignificantBit(), bin.length);
    }

    public List<Hypothesis> successors() {
        List<Hypothesis> allSuccessors = new ArrayList<>(numberOfSuccessors());
        allSuccessors.addAll(leftSuccessors());
        allSuccessors.addAll(rightSuccessors());
        return allSuccessors;
    }

    private List<Hypothesis> generateSuccessors(int start, int end) {
        List<Hypothesis> successors = new ArrayList<>(end - start + 1);
        for (int i = start; i < end; i++) {
            if (!bin[i]) {
                boolean[] successor = bin.clone();
                successor[i] = true;
                successors.add(new Hypothesis(successor));
            }
        }
        return successors;
    }

    private List<Hypothesis> generatePredecessors() {
        List<Hypothesis> predecessors = new ArrayList<>();
        for (int i = 0; i < this.size(); i++) {
            if (bin[i]) {
                boolean[] predecessor = bin.clone();
                predecessor[i] = false;
                predecessors.add(new Hypothesis(predecessor));
            }
        }
        return predecessors.reversed();
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

    public Hypothesis initial(Hypothesis hPrime) {
        if (!isValidSuccessor(hPrime, true)) {
            throw new IllegalArgumentException("h' deve essere un successore left valido di h");
        }

        if (numberOfPredecessors() == 0) {
            return hPrime;
        }

        List<Hypothesis> predecessors = hPrime.generatePredecessors();
        return predecessors.get(0);
    }

    public Hypothesis finalPred(Hypothesis hPrime) {
        if (!isValidSuccessor(hPrime, true)) {
            throw new IllegalArgumentException("h' deve essere un successore left valido di h");
        }

        List<Hypothesis> predecessors = hPrime.generatePredecessors();
        if (predecessors.isEmpty()) {
            throw new IllegalArgumentException("h' deve essere un successore left valido di h");
        }
        if (predecessors.size() < 2) {
            throw new IllegalArgumentException("Non ci sono abbastanza predecessori per h'");
        }
        return predecessors.get(predecessors.size() - 2);
    }

    private boolean isValidSuccessor(Hypothesis hPrime, boolean left) {
        if (hPrime == null || hPrime.size() != this.size()) {
            return false;
        }
        int diffCount = 0;
        int diffIndex = -1;
        for (int i = 0; i < bin.length; i++) {
            if (bin[i] != hPrime.bin[i]) {
                diffCount++;
                diffIndex = i;
                if (diffCount > 1) {
                    return false;
                }
            }
        }

        if (diffCount != 1 || !hPrime.bin[diffIndex]) {
            return false;
        }

        int msb = this.mostSignificantBit();
        return left ? diffIndex < msb : diffIndex >= msb;
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
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Hypothesis that = (Hypothesis) o;
        for (int i = 0; i < bin.length; i++) {
            if (this.bin[i] != that.bin[i]) {
                return false;
            }
        }
        return true; // Confronta gli array binari per l'uguaglianza
    }

    @Override
    public int hashCode() {
        return Objects.hash(bin); // Coerente con equals()
    }

}