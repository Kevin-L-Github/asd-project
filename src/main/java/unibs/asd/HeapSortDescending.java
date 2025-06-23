package unibs.asd;

public class HeapSortDescending {

    /**
     * Ordina un array di booleani in ordine decrescente usando HeapSort.
     * Ordinamento: true (1) prima di false (0).
     */
    public static void heapSort(boolean[] array) {
        int n = array.length;

        // Costruzione di un min-heap
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(array, n, i);
        }

        // Estrazione degli elementi uno per uno e ricostruzione del min-heap
        for (int i = n - 1; i > 0; i--) {
            swap(array, 0, i);       // Sposta il minimo (false) alla fine
            heapify(array, i, 0);    // Ripristina il min-heap sulla parte rimanente
        }
    }

    /**
     * Mantiene la proprietà di min-heap per il sottoalbero radicato in i.
     */
    private static void heapify(boolean[] array, int heapSize, int i) {
        int smallest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        // Confronto con il figlio sinistro
        if (left < heapSize && toInt(array[left]) < toInt(array[smallest])) {
            smallest = left;
        }

        // Confronto con il figlio destro
        if (right < heapSize && toInt(array[right]) < toInt(array[smallest])) {
            smallest = right;
        }

        // Se il più piccolo non è il nodo corrente, scambia e ricorsione
        if (smallest != i) {
            swap(array, i, smallest);
            heapify(array, heapSize, smallest);
        }
    }

    /**
     * Converte un booleano in intero: false → 0, true → 1
     */
    private static int toInt(boolean b) {
        return b ? 1 : 0;
    }

    /**
     * Scambia due elementi in un array booleano.
     */
    private static void swap(boolean[] array, int i, int j) {
        boolean temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}