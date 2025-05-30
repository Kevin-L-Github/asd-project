package unibs.asd.project;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe che rappresenta un'ipotesi come un array di valori booleani.
 * Ogni elemento dell'array rappresenta uno stato binario (true = attivo, false = inattivo).
 * Fornisce metodi per manipolare e analizzare la configurazione binaria.
 */
public class Hypothesis {
    private final boolean[] bin;  // Array immutabile che rappresenta lo stato binario

    /**
     * Costruttore che inizializza una nuova ipotesi della dimensione specificata.
     * Tutti i valori sono inizializzati a false.
     * @param size dimensione dell'ipotesi
     * @throws IllegalArgumentException se size è negativo
     */
    public Hypothesis(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("La dimensione non può essere negativa");
        }
        this.bin = new boolean[size];
    }

    public Hypothesis(boolean[] bin) {
        if (bin == null) {
            throw new IllegalArgumentException("L'array binario non può essere null");
        }
        this.bin = bin.clone();  // Clona l'array per garantire l'immutabilità
    }

    /**
     * Restituisce la dimensione dell'ipotesi.
     * @return la dimensione dell'array binario
     */
    public int size() {
        return bin.length; 
    }

    /**
     * Conta il numero di bit a false (successori potenziali).
     * @return numero di bit a false
     */
    public int numberOfSuccessors() {
        return bin.length - numberOfPredecessors();
    }

    /**
     * Conta il numero di bit a true (predecessori potenziali).
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
     * @return lista di successori left
     */
    public List<boolean[]> leftSuccessors() {
        return generateSuccessors(0, mostSignificantBit());
    }

    /**
     * Genera tutti i successori "right" (dal bit più significativo alla fine).
     * @return lista di successori right
     */
    public List<boolean[]> rightSuccessors() {
        return generateSuccessors(mostSignificantBit(), bin.length);
    }

    /**
     * Genera tutti i possibili successori dell'ipotesi corrente.
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
     * @param start indice di partenza (inclusivo)
     * @param end indice di fine (esclusivo)
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

    /**
     * Trova l'indice del bit più significativo (primo bit true).
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
     * @param obj oggetto da confrontare
     * @return true se gli oggetti sono ipotesi equivalenti
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Hypothesis)) return false;
        
        Hypothesis other = (Hypothesis) obj;
        if (bin.length != other.bin.length) return false;
        
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
        if (mostSignificantBit() == -1 || msb == 0 || lsb == -1 ) {
            throw new IllegalArgumentException("Non è possibile calcolare gli hsecondi per questa ipotesi");
        }

        List<Hypothesis> list = new ArrayList<>();

        for (int i = 0; i < msb; i++) {
            boolean[] h_prime = bin.clone();
            h_prime[i] = true;
            for (int j = i+1; j <= lsb; j++) {
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
}