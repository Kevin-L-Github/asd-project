package unibs.asd.mhs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.PriorityQueue;
import unibs.asd.factories.*;
import unibs.asd.interfaces.*;
import unibs.asd.enums.BitSetType;
import unibs.asd.structures.bitset.BitSetHypothesis;
import unibs.asd.structures.bools.BoolsHypothesis;
import unibs.asd.structures.fastbitset.FastHypothesis;
import unibs.asd.structures.roaringbitmap.RoaringHypothesis;
import unibs.asd.structures.sparse.SparseHypothesis;

public class BoostMHS implements MHS {

    private HypothesisFactory factory;
    private PriorityQueue<Hypothesis> current;
    private List<Hypothesis> solutions;
    private LinkedHashMap<BitVector, BitVector> bucket;
    private boolean[][] instance = null;
    private boolean[][] matrix;
    private List<Integer> nonEmptyColumns;
    private int DEPTH;
    private long startTime;
    private int depthLimit;

    private double computationTime;
    private boolean executed;
    private boolean stopped;
    private boolean stoppedInsideLoop;
    private boolean outOfMemoryError;

    public BoostMHS(boolean[][] instance) {
        this.current = new PriorityQueue<>(
                (a, b) -> isGreater(a.getBin(), b.getBin()) ? -1 : isGreater(b.getBin(), a.getBin()) ? 1 : 0);
        this.solutions = new ArrayList<>();
        this.bucket = new LinkedHashMap<>();
        this.instance = instance;
        this.DEPTH = 0;
        this.computationTime = 0;
        this.executed = false;
        this.stopped = false;
        this.stoppedInsideLoop = false;
        this.outOfMemoryError = false;
        this.cleanMatrix();
        this.depthLimit = Math.min(matrix.length, matrix[0].length);

    }

    public List<Hypothesis> run(BitSetType type, long timeoutMillis) {

        try {
            if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
                throw new IllegalArgumentException("Instance must be a non-empty boolean matrix.");
            }

            int m = matrix[0].length;
            int n = matrix.length;

            //System.out.println("Starting MHS with timeout: " + timeoutMillis + " milliseconds");
            //System.out.println("Matrix size: " + matrix.length + "x" + matrix[0].length);
            //System.out.println("Depth limit: " + depthLimit);

            Hypothesis emptyHypothesis = getInitialHypothesis(type, m, n);
            startTime = System.nanoTime();
            long timeoutNanos = timeoutMillis * 1_000_000;
            List<Hypothesis> initialChildren = generateChildrenEmptyHypothesis(emptyHypothesis);
            this.current.addAll(initialChildren);
            for (Hypothesis init : initialChildren) {
                this.bucket.put(init.getBin(), init.getVector());
            }

            DEPTH++;
            // int i = 0;
            while (!current.isEmpty() && DEPTH <= depthLimit) {
                if (System.nanoTime() - startTime > timeoutNanos) {
                    this.stopped = true;
                    break;
                }
                PriorityQueue<Hypothesis> next = new PriorityQueue<>(
                        (a, b) -> isGreater(a.getBin(), b.getBin()) ? -1 : isGreater(b.getBin(), a.getBin()) ? 1 : 0);
                Hypothesis first = current.peek();
                while (!current.isEmpty()) {
                    if (System.nanoTime() - startTime > timeoutNanos) {
                        this.stopped = true;
                        this.stoppedInsideLoop = true;
                        break;
                    }
                    Hypothesis hypothesis = current.poll();
                    // i++;
                    // printStatusBar(i, DEPTH, startTime, timeoutNanos);
                    this.bucket.put(hypothesis.getBin(), hypothesis.getVector());
                    if (check(hypothesis)) {
                        solutions.add(hypothesis);
                        bucket.remove(hypothesis.getBin());
                    } else if (hypothesis.mostSignificantBit() != 0) {
                        BitVector globalInitial = hypothesis.globalInitial().getBin();
                        Iterator<BitVector> it = bucket.keySet().iterator();
                        while (it.hasNext()) {
                            BitVector element = it.next();
                            if (isGreater(element, globalInitial)) {
                                it.remove();
                            } else {
                                break;
                            }
                        }
                        if (!first.equals(hypothesis)) {
                            List<Hypothesis> children = generateChildren(hypothesis);
                            next.addAll(children);
                        }
                    }

                }
                DEPTH++;
                this.current = next;
                this.bucket.clear();
            }
        } catch (OutOfMemoryError e) {
            this.outOfMemoryError = true;
            this.stopped = true;
            this.stoppedInsideLoop = true;
        }
        this.computationTime = (System.nanoTime() - startTime);
        DEPTH--;
        this.restoreSolutions();
        this.executed = true;
        return solutions;
    }

    private List<Hypothesis> generateChildrenEmptyHypothesis(Hypothesis parent) {
        List<Hypothesis> children = new ArrayList<>();
        for (int i = 0; i < parent.length(); i++) {
            BitVector childBin = parent.getBin();
            childBin.set(i);
            Hypothesis child = this.factory.create(childBin);
            setFields(child);
            children.add(child);
        }
        return children;
    }

    private Hypothesis getInitialHypothesis(BitSetType type, int m, int n) {
        switch (type) {
            case BitSetType.BITSET:
                this.factory = new BitSetHypothesisFactory();
                return new BitSetHypothesis(m, n);
            case BitSetType.BOOLS_ARRAY:
                this.factory = new BoolsHypothesisFactory();
                return new BoolsHypothesis(m, n);
            case BitSetType.ROARING_BIT_MAP:
                this.factory = new RoaringHypothesisFactory();
                return new RoaringHypothesis(m, n);
            case BitSetType.FAST_BITSET:
                this.factory = new FastBitSetHypothesisFactory();
                return new FastHypothesis(m, n);
            case BitSetType.SPARSE:
                this.factory = new SparseHypothesisFactory();
                return new SparseHypothesis(m, n);
            default:
                throw new IllegalArgumentException("Scegliere un tipo di implementazione");
        }
    }

    /**
     * Genera i figli di un'ipotesi padre.
     * 
     * @param parent
     * @return
     */
    private List<Hypothesis> generateChildren(Hypothesis parent) {
        List<Hypothesis> children = new ArrayList<>();
        BitVector bin = parent.getBin();
        int length = bin.mostSignificantBit();
        for (int i = 0; i < length; i++) {
            BitVector childBin = (BitVector) bin.clone();
            childBin.set(i);
            Hypothesis child = this.factory.create(childBin);
            child.setVector(this.factory.createVector(matrix.length));
            List<Hypothesis> predecessors = child.predecessors();
            boolean isValid = true;
            for (Hypothesis predecessor : predecessors) {
                BitVector match = bucket.get(predecessor.getBin());
                if (match == null) {
                    isValid = false;
                    break;
                } else {
                    propagate(child, match);
                }
            }
            if (isValid) {
                propagate(parent, child);
                children.add(child);
            }
        }
        return children;
    }

    /**
     * Confronta due BitVector e restituisce true se il primo è maggiore del
     * secondo.
     * 
     * @param a
     * @param b
     * @return
     */
    private static boolean isGreater(BitVector a, BitVector b) {
        for (int i = 0; i < a.size(); i++) {
            boolean aBit = a.get(i);
            boolean bBit = b.get(i);
            if (aBit != bBit) {
                return aBit;
            }
        }
        return false;
    }

    /**
     * Imposta i campi dell'ipotesi in base alla matrice e al binario.
     * 
     * @param hypothesis
     */
    private void setFields(Hypothesis hypothesis) {
        int n = matrix.length;
        BitVector vector = this.factory.createVector(n);
        BitVector bin = hypothesis.getBin();
        for (int i = 0; i < matrix[0].length; i++) {
            if (bin.get(i)) {
                for (int j = 0; j < n; j++) {
                    if (matrix[j][i]) {
                        vector.set(j);
                    }
                }
            }
        }
        hypothesis.setVector(vector);
    }

    /**
     * Controlla se l'ipotesi è una soluzione.
     * 
     * @param hypothesis
     * @return
     */
    private boolean check(Hypothesis hypothesis) {
        return hypothesis.isSolution();
    }

    /**
     * Propaga le informazioni da un predecessore a un successore.
     * 
     * @param predecessor
     * @param successor
     */
    private void propagate(Hypothesis predecessor, Hypothesis successor) {
        successor.or(predecessor);
    }

    /**
     * Propaga le informazioni da un predecessore a un successore.
     * 
     * @param successor
     * @param information
     */
    private void propagate(Hypothesis successor, BitVector information) {
        successor.update(information);
    }

    /**
     * Pulisce la matrice rimuovendo le colonne vuote.
     */
    private void cleanMatrix() {
        if (instance == null || instance.length == 0) {
            matrix = new boolean[0][0];
            nonEmptyColumns = new ArrayList<>();
            return;
        }
        int rows = instance.length;
        int cols = instance[0].length;
        nonEmptyColumns = new ArrayList<>();
        for (int j = 0; j < cols; j++) {
            boolean hasTrue = false;
            for (int i = 0; i < rows; i++) {
                if (instance[i][j]) {
                    hasTrue = true;
                    break;
                }
            }
            if (hasTrue) {
                nonEmptyColumns.add(j);
            }
        }
        int newCols = nonEmptyColumns.size();
        matrix = new boolean[rows][newCols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < newCols; j++) {
                matrix[i][j] = instance[i][nonEmptyColumns.get(j)];
            }
        }
    }

    /**
     * Ripristina le soluzioni originali dalla matrice compressa.
     */
    private void restoreSolutions() {
        List<Hypothesis> restored = new ArrayList<>();
        int originalSize = instance[0].length;

        for (Hypothesis solution : solutions) {
            BitVector compressed = solution.getBin();
            BitVector full = this.factory.createVector(originalSize);
            for (int i = 0; i < nonEmptyColumns.size(); i++) {
                int originalIndex = nonEmptyColumns.get(i);
                full.set(originalIndex, compressed.get(i));
            }
            restored.add(this.factory.create(full));
        }

        this.solutions = restored;
    }

    public List<Hypothesis> getSolutions() {
        return solutions;
    }

    public boolean[][] getInstance() {
        return instance;
    }

    public boolean[][] getMatrix() {
        return matrix;
    }

    public List<Integer> getNonEmptyColumns() {
        return nonEmptyColumns;
    }

    public int getDEPTH() {
        return DEPTH;
    }

    public double getComputationTime() {
        return computationTime;
    }

    public boolean isExecuted() {
        return executed;
    }

    public boolean isStopped() {
        return stopped;
    }

    public boolean isStoppedInsideLoop() {
        return stoppedInsideLoop;
    }

    public boolean isOutOfMemoryError() {
        return outOfMemoryError;
    }

    public int getDepthLimit() {
        return depthLimit;
    }

    @SuppressWarnings("unused")
    private void printStatusBar(int processedCount, int depth, long startTime, long timeoutNanos) {
        long elapsedMillis = (System.nanoTime() - startTime) / 1_000_000;
        long remainingMillis = Math.max(0, (timeoutNanos / 1_000_000) - elapsedMillis);

        int remainingItems = current.size() + processedCount; // Total items at start
        int itemsLeft = remainingItems - processedCount;

        String elapsedStr = String.format("%d:%02d",
                elapsedMillis / 60000, (elapsedMillis % 60000) / 1000);
        String remainingStr = String.format("%d:%02d",
                remainingMillis / 60000, (remainingMillis % 60000) / 1000);

        StringBuilder status = new StringBuilder("\r");
        status.append(String.format("Depth %-3d | ", depth));
        status.append(String.format("Remaining: %-5d | ", itemsLeft));
        status.append(String.format("Sol %-4d | ", solutions.size()));
        status.append(String.format("Processed: %-5d | ", processedCount));
        status.append(String.format("Time: %s | ", elapsedStr));
        status.append(String.format("Timer: %s", remainingStr));

        if (remainingMillis < 10000) {
            status.append(" [WARNING: Timeout soon!]");
        }

        System.out.print(status.toString());
        System.out.print("\033[K");
    }

}
