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

    /**
     * Costruttore che inizializza una nuova ipotesi della dimensione specificata.
     * Tutti i valori sono inizializzati a false.
     * 
     * @param size dimensione dell'ipotesi
     * @throws IllegalArgumentException se size è negativo
     */
    public Hypothesis(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("La dimensione non può essere negativa");
        }
        this.bin = new boolean[size];
        for (int i = 0; i < size; i++) {
            bin[i] = false; // Inizializza tutti i bit a false
        }
    }

    public boolean[] getBin() {
        return bin; // Restituisce una copia dell'array per garantire l'immutabilità
    }

    public Hypothesis(boolean[] bin) {
        if (bin == null) {
            throw new IllegalArgumentException("L'array binario non può essere null");
        }
        this.bin = bin.clone(); // Clona l'array per garantire l'immutabilità
    }

    /**
     * Restituisce la dimensione dell'ipotesi.
     * 
     * @return la dimensione dell'array binario
     */
    public int size() {
        return bin.length;
    }

    /**
     * Conta il numero di bit a false (successori potenziali).
     * 
     * @return numero di bit a false
     */
    public int numberOfSuccessors() {
        return bin.length - numberOfPredecessors();
    }

    /**
     * Conta il numero di bit a true (predecessori potenziali).
     * 
     * @return numero di bit a true
     */
    public int numberOfPredecessors() {
        int count = 0;
        // Utilizzo di un loop standard per potenziale ottimizzazione JVM
        for (int i = 0; i < bin.length; i++) {
            if (bin[i]) {
                count++;
            }
        }
        return count;
    }

    /**
     * Genera tutti i successori "left" (prima del bit più significativo).
     * 
     * @return lista di successori left
     */
    public List<boolean[]> leftSuccessors() {
        return generateSuccessors(0, mostSignificantBit());
    }

    /**
     * Genera tutti i successori "right" (dal bit più significativo alla fine).
     * 
     * @return lista di successori right
     */
    public List<boolean[]> rightSuccessors() {
        return generateSuccessors(mostSignificantBit(), bin.length);
    }

    /**
     * Genera tutti i possibili successori dell'ipotesi corrente.
     * 
     * @return lista combinata di tutti i successori
     */
    public List<boolean[]> successors() {
        int capacity = numberOfSuccessors();
        List<boolean[]> allSuccessors = new ArrayList<>(capacity);
        allSuccessors.addAll(leftSuccessors());
        allSuccessors.addAll(rightSuccessors());
        return allSuccessors;
    }

    /**
     * Metodo helper per generare successori in un range specifico.
     * 
     * @param start indice di partenza (inclusivo)
     * @param end   indice di fine (esclusivo)
     * @return lista di successori nel range specificato
     */
    private List<boolean[]> generateSuccessors(int start, int end) {
        List<boolean[]> successors = new ArrayList<>(end - start);
        for (int i = start; i < end; i++) {
            if (!bin[i]) {
                boolean[] successor = bin.clone();
                successor[i] = true;
                successors.add(successor);
            }
        }
        return successors;
    }

    private List<boolean[]> generatePredecessors() {
        List<boolean[]> predecessors = new ArrayList<>();
        for (int i = 0; i < this.size(); i++) {
            if (bin[i]) {
                boolean[] successor = bin.clone();
                successor[i] = false; // Complementa il bit corrente
                predecessors.add(successor);
            }
        }
        return predecessors;
    }

    /**
     * Trova l'indice del bit più significativo (primo bit true).
     * 
     * @return indice del primo bit true, o -1 se non trovato
     */
    public int mostSignificantBit() {
        // Ottimizzazione: loop standard invece di enhanced for
        for (int i = 0; i < bin.length; i++) {
            if (bin[i]) {
                return i;
            }
        }
        return -1;
    }

    public int leastSignificantBit() {
        // Ottimizzazione: loop standard invece di enhanced for
        for (int i = bin.length - 1; i >= 0; i--) {
            if (bin[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Confronta questa ipotesi con un altro oggetto per uguaglianza.
     * 
     * @param obj oggetto da confrontare
     * @return true se gli oggetti sono ipotesi equivalenti
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Hypothesis))
            return false;

        Hypothesis other = (Hypothesis) obj;
        if (bin.length != other.bin.length)
            return false;

        // Ottimizzazione: confronto diretto degli array
        for (int i = 0; i < bin.length; i++) {
            if (bin[i] != other.bin[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Genera un hash code per l'ipotesi.
     * 
     * @return valore hash code
     */
    @Override
    public int hashCode() {
        int result = 1;
        for (boolean b : bin) {
            result = 31 * result + (b ? 1231 : 1237);
        }
        return result;
    }

    public List<Hypothesis> getHseconds() {

        int msb = mostSignificantBit();
        int lsb = leastSignificantBit();
        if (mostSignificantBit() == -1 || msb == 0 || lsb == -1) {
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
                    list.add(new Hypothesis(h_sec.clone()));
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

    /**
     * Calcola l'ipotesi globale iniziale come definito nella specifica.
     * 
     * @return l'ipotesi globalInitial
     * @throws IllegalArgumentException se l'ipotesi è vuota o se bin[1] non è 0
     */
    public Hypothesis globalInitial() {
        if (bin.length == 0) {
            throw new IllegalArgumentException("L'ipotesi non può essere vuota");
        }
        if (bin.length <= 1 || bin[0]) {
            throw new IllegalArgumentException("bin(h)[1] deve essere 0");
        }

        // Clona l'array per non modificare l'originale
        boolean[] globalInitialBin = bin.clone();

        // Complementa l'occorrenza di 0 in posizione 1
        globalInitialBin[0] = !globalInitialBin[0];

        // Trova e complementa l'occorrenza meno significativa di 1
        int lsb = leastSignificantBit();
        if (lsb != -1) {
            globalInitialBin[lsb] = !globalInitialBin[lsb];
        }

        return new Hypothesis(globalInitialBin);
    }

    /**
     * Calcola l'ipotesi initial come definito nella specifica.
     * 
     * @param hPrime un successore left di questa ipotesi
     * @return l'ipotesi initial(h, h')
     * @throws IllegalArgumentException se h' non è un successore left valido
     */
    public Hypothesis initial(Hypothesis hPrime) {
        if (!isValidSuccessor(hPrime, true)) {
            throw new IllegalArgumentException("h' deve essere un successore left valido di h");
        }

        // Se siamo al primo livello, initial e final coincidono con h'
        if (numberOfPredecessors() == 0) {
            return hPrime;
        }

        // Trova tutte le ipotesi che condividono lo stesso successore h'
        List<boolean[]> predecessors = hPrime.generatePredecessors();

        // Prendi il predecessore più a sinistra (con MSB più piccolo)
        return new Hypothesis(predecessors.getLast());
    }

    /**
     * Calcola l'ipotesi final come definito nella specifica.
     * 
     * @param hPrime un successore left di questa ipotesi
     * @return l'ipotesi final(h, h')
     * @throws IllegalArgumentException se h' non è un successore left valido
     */
    public Hypothesis finalPred(Hypothesis hPrime) {
        if (!isValidSuccessor(hPrime, true)) {
            throw new IllegalArgumentException("h' deve essere un successore left valido di h");
        }

        // Se siamo al primo livello, initial e final coincidono con h'
        if (numberOfPredecessors() == 0) {
            return hPrime;
        }

        // Trova tutte le ipotesi che condividono lo stesso successore h'
        List<boolean[]> predecessors = hPrime.generatePredecessors();

        if (predecessors.get(1) == null || predecessors.size() < 2) {
            throw new IllegalArgumentException("Non ci sono predecessori per h'");
        }

        // Prendi il predecessore più a sinistra (con MSB più piccolo)
        return new Hypothesis(predecessors.get(1));
    }

    // Metodo helper per verificare se h' è un successore valido (left o right)
    private boolean isValidSuccessor(Hypothesis hPrime, boolean left) {
        if (hPrime == null || hPrime.size() != this.size()) {
            return false;
        }

        // Verifica che h' sia un successore di h
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

        if (diffCount != 1 || hPrime.bin[diffIndex] != true) {
            return false;
        }

        // Verifica se è left o right in base alla posizione del bit cambiato
        int msb = this.mostSignificantBit();
        if (left) {
            return diffIndex < msb;
        } else {
            return diffIndex >= msb;
        }
    }

    public List<Hypothesis> generateChildren(List<Hypothesis> current) {

        List<Hypothesis> children = new ArrayList<>();

        if (this.isEmptyHypothesis()) {
            children = this.leftSuccessors().stream()
                    .map(Hypothesis::new)
                    .toList();
            
            return children;
        }
        
        Hypothesis h_p = current.getFirst();

        for (int i = 0; i < mostSignificantBit(); i++){
            boolean[] h_prime = this.getBin().clone();
            h_prime[0] = true;

            // SET_FIELDS e PROPAGATE

            Hypothesis h_s_i = this.initial(new Hypothesis(h_prime));
            Hypothesis h_s_f = this.finalPred(new Hypothesis(h_prime));
            int counter = 0;
             
            while (!h_p.isGreater(h_s_i.getBin()) && h_p.isGreater(h_s_f.getBin()) ) {
                if ((h_p.distance(new Hypothesis(h_prime)) == 1) && (h_p.distance(this) == 2)){
                    // propagate
                    counter++;
                }
                h_p = current.get(1);
            }

            if (counter == this.cardinality()) {
                children.add(new Hypothesis(h_prime));
            }



        }

        return children;
    }


    private int cardinality() {
        int cardinality = 0;

        for (int i = 0; i < bin.length; i++){
            if (bin[i]){
                cardinality++;
            }
        }

        return cardinality;
    }

    public int distance(Hypothesis h2){
        int distance = 0;
        for (int i = 0; i < h2.getBin().length; i++){
            if (this.bin[i] != h2.getBin()[i]){
                distance++;
            }
        }
        return distance;
    }

    public boolean isEmptyHypothesis() {
        for (boolean b : this.getBin()) {
            if (b) {
                return false;
            }
        }
        return true;
    }

    public boolean isGreater(boolean[] bin2) {
        boolean[] trimmed1 = trimLeadingZeros(bin);
        boolean[] trimmed2 = trimLeadingZeros(bin2);

        if (trimmed1.length != trimmed2.length) {
            return trimmed1.length > trimmed2.length;
        }

        for (int i = 0; i < trimmed1.length; i++) {
            if (trimmed1[i] != trimmed2[i]) {
                return trimmed1[i];
            }
        }
        return false;
    }

    private boolean[] trimLeadingZeros(boolean[] binary) {
        int firstOne = -1;
        for (int i = 0; i < binary.length; i++) {
            if (binary[i]) {
                firstOne = i;
                break;
            }
        }

        return firstOne == -1 ? new boolean[] { false } : Arrays.copyOfRange(binary, firstOne, binary.length);
    }
}