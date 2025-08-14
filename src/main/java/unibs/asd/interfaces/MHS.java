package unibs.asd.interfaces;

import java.util.List;

import unibs.asd.enums.BitSetType;

public interface MHS {

    /**
     * Esegue l'algoritmo MHS con il tipo di BitSet specificato e un timeout.
     *
     * @param type          Il tipo di BitSet da utilizzare.
     * @param timeoutMillis Il timeout in millisecondi.
     * @return Una lista di ipotesi trovate.
     */
    List<Hypothesis> run(BitSetType type, long timeoutMillis);

    /**
     * Restituisce le soluzioni trovate dall'algoritmo.
     *
     * @return Una lista di ipotesi che rappresentano le soluzioni.
     */
    List<Hypothesis> getSolutions();

    /**
     * Restituisce l'istanza su cui è stato eseguito l'algoritmo. Ovvero la matrice booleana
     * che rappresenta il problema MHS, e che potrebbe avere colonne vuote.
     * @return L'istanza come matrice booleana.
     */
    boolean[][] getInstance();

    /**
     * Restituisce la matrice su cui è stato eseguito l'algoritmo priva di colonne vuote.
     * @return La matrice originale come matrice booleana.
     */
    boolean[][] getMatrix();

    /**
     * Restituisce le colonne non vuote della matrice.
     * @return Una lista di indici delle colonne non vuote.
     */
    List<Integer> getNonEmptyColumns();

    /**
     * Restituisce la profondità più alta raggiunta durante l'esecuzione dell'algoritmo.
     * @return La profondità più alta come intero.
     */
    int getDEPTH();


    /**
     * Restituisce il tempo di esecuzione dell'algoritmo in nanosecondi.
     * @return Il tempo di esecuzione come double.
     */
    double getComputationTime();

    /**
     * Indica se l'algoritmo è stato eseguito.
     * @return true se l'algoritmo è stato eseguito, false altrimenti.
     */
    boolean isExecuted();

    /**
     * Indica se l'algoritmo è stato fermato duranrte l'esecuzione.
     * Questo può essere causato da un timeout o da un errore di memoria.
     * @return true se l'algoritmo è stato fermato, false altrimenti.
     */
    boolean isStopped();

    /**
     * Indica se l'algoritmo è stato fermato all'interno di un ciclo.
     * Questo può essere causato da un timeout o da un errore di memoria.
     * @return true se l'algoritmo è stato fermato all'interno di un ciclo, false altrimenti.
     */
    boolean isStoppedInsideLoop();

    /**
     * Indica se l'algoritmo ha incontrato un errore di memoria durante l'esecuzione.
     * @return true se l'algoritmo ha incontrato un errore di memoria, false altrimenti.
     */
    boolean isOutOfMemoryError();

    /**
     * Restituisce il limite di profondità dell'algoritmo.
     * @return Il limite di profondità come intero.
     */
    public int getDepthLimit();
    
}
