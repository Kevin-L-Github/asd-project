package unibs.asd.fastbitset;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class eMHS {

    private PriorityQueue<FastHypothesis> current;
    private List<FastHypothesis> solutions;
    private LinkedHashSet<FastBitSet> bucket;
    private boolean[][] instance = null;
    private boolean[][] matrix;
    private List<Integer> nonEmptyColumns;
    private int DEPTH;

    private double computationTime;
    private boolean executed;
    private boolean stopped;
    private boolean stoppedInsideLoop;

    public eMHS(boolean[][] instance) {
        this.current = new PriorityQueue<>(
                (a, b) -> isGreater(a.getBin(), b.getBin()) ? -1 : isGreater(b.getBin(), a.getBin()) ? 1 : 0);
        this.solutions = new ArrayList<>();
        this.bucket = new LinkedHashSet<>();
        this.instance = instance;
        this.DEPTH = 0;
        this.computationTime = 0;
        this.executed = false;
        this.stopped = false;
        this.stoppedInsideLoop = false;
        this.cleanMatrix();
    }

    public List<FastHypothesis> run(long timeoutMillis) {
        long startTime = System.nanoTime();
        long timeoutNanos = timeoutMillis * 1_000_000;

        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            throw new IllegalArgumentException("Instance must be a non-empty boolean matrix.");
        }

        int m = matrix[0].length;
        int n = matrix.length;

        FastHypothesis emptyHypothesis = new FastHypothesis(m, n);
        List<FastHypothesis> initialChildren = generateChildrenEmptyHypothesis(emptyHypothesis);
        this.current.addAll(initialChildren);
        this.bucket.addAll(initialChildren.stream().map(FastHypothesis::getBin).collect(Collectors.toList()));
        DEPTH++;
        boolean computing = true;
        while (computing) {
            if (System.nanoTime() - startTime > timeoutNanos) {
                System.out.println("\nTimeout reached. Stopping the algorithm.");
                this.stopped = true;
                break;
            }
            PriorityQueue<FastHypothesis> next = new PriorityQueue<>(
                    (a, b) -> isGreater(a.getBin(), b.getBin()) ? -1 : isGreater(b.getBin(), a.getBin()) ? 1 : 0);
            FastHypothesis first = current.peek();
            //int i = 0;
            while (!current.isEmpty()) {
                //i++;
                //printStatusBar(i, DEPTH, startTime, timeoutNanos);
                if (System.nanoTime() - startTime > timeoutNanos) {
                    System.out.println("\nTimeout reached inside loop. Stopping.");
                    this.stopped = true;
                    this.stoppedInsideLoop = true;
                    break;
                }
                FastHypothesis hypothesis = current.poll();
                this.bucket.add(hypothesis.getBin());
                if (check(hypothesis)) {
                    solutions.add(hypothesis);
                    bucket.remove(hypothesis.getBin());
                } else if (hypothesis.mostSignificantBit() != 0) {
                    FastBitSet globalInitial = hypothesis.globalInitial().getBin();
                    Iterator<FastBitSet> it = bucket.iterator();
                    while (it.hasNext()) {
                        FastBitSet element = it.next();
                        if (isGreater(element, globalInitial)) {
                            it.remove();
                        } else {
                            break;
                        }
                    }
                    if (!first.equals(hypothesis)) {
                        List<FastHypothesis> children = generateChildren(hypothesis);
                        next.addAll(children);
                    }
                }
            }
            DEPTH++;
            if (next.isEmpty()) {
                computing = false;
            } else {
                this.current = next;
                this.bucket.clear();
            }
        }
        this.computationTime = (System.nanoTime() - startTime) / 1000000000F;
        this.restoreSolutions();
        this.executed = true;
        return solutions;
    }

    private List<FastHypothesis> generateChildrenEmptyHypothesis(FastHypothesis h) {
        List<FastHypothesis> children = new ArrayList<>();
        for (int i = 0; i < h.length(); i++) {
            FastBitSet newBin = h.getBin();
            newBin.set(i);
            FastHypothesis H_new = new FastHypothesis(newBin);
            setFields(H_new);
            children.add(H_new);
        }
        return children;
    }

    private List<FastHypothesis> generateChildren(FastHypothesis parent) {
        List<FastHypothesis> children = new ArrayList<>();
        FastBitSet bin = parent.getBin();
        int length = bin.nextSetBit(0);
        for (int i = 0; i < length; i++) {
            FastBitSet childBin = (FastBitSet) bin.clone();
            childBin.set(i);
            FastHypothesis child = new FastHypothesis(childBin);
            List<FastHypothesis> predecessors = child.predecessors();
            boolean isValid = true;
            for (FastHypothesis p : predecessors) {
                if (!bucket.contains(p.getBin())) {
                    isValid = false;
                    break;
                }
            }
            if (isValid) {
                setFields(child);
                propagate(parent, child);
                children.add(child);
            }
        }
        return children;
    }

    public static boolean isGreater(FastBitSet a, FastBitSet b) {
        for (int i = 0; i < a.logicalSize; i++) {
            boolean aBit = a.get(i);
            boolean bBit = b.get(i);
            if (aBit != bBit) {
                return aBit;
            }
        }
        return false;
    }

    private void setFields(FastHypothesis h) {
        int n = matrix.length;
        FastBitSet vector = new FastBitSet(n);
        if (h.cardinality() > 0) {
            FastBitSet bin = h.getBin();
            for (int i = 0; i < matrix[0].length; i++) {
                if (bin.get(i)) {
                    for (int j = 0; j < n; j++) {
                        if (matrix[j][i]) {
                            vector.set(j);
                        }
                    }
                }
            }
        }
        h.setVector(vector);
    }

    private boolean check(FastHypothesis h) {
        return h.isSolution();
    }

    private void propagate(FastHypothesis h, FastHypothesis h_prime) {
        FastBitSet newVector = (FastBitSet) h.getVector().clone();
        newVector.or(h_prime.getVector());
        h_prime.setVector(newVector);
    }

    private void cleanMatrix() {
        if (instance == null || instance.length == 0) {
            matrix = new boolean[0][0];
            nonEmptyColumns = new ArrayList<>();
            return;
        }

        int rows = instance.length;
        int cols = instance[0].length;

        System.out.println("Righe: " + rows);
        System.out.println("Colonne: " + cols);

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

        System.out.println("Colonne eliminate: " + (cols - newCols));
    }

    private void restoreSolutions() {
        List<FastHypothesis> restored = new ArrayList<>();
        int originalSize = instance[0].length;

        for (FastHypothesis h : solutions) {
            FastBitSet compressed = h.getBin();
            FastBitSet full = new FastBitSet(originalSize);

            for (int i = 0; i < nonEmptyColumns.size(); i++) {
                int originalIndex = nonEmptyColumns.get(i);
                full.set(originalIndex, compressed.get(i));
            }

            restored.add(new FastHypothesis(full));
        }

        this.solutions = restored;
    }

    public List<FastHypothesis> getSolutions() {
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

    @SuppressWarnings("unused")
    private void printStatusBar(int processedCount, int depth, long startTime, long timeoutNanos) {
        long elapsedMillis = (System.nanoTime() - startTime) / 1_000_000;
        long remainingMillis = Math.max(0, (timeoutNanos / 1_000_000) - elapsedMillis);

        int remainingItems = current.size() + processedCount; // Total items at start
        int itemsLeft = remainingItems - processedCount;

        String elapsedStr = String.format("%d:%02d.%03d",
                elapsedMillis / 60000, (elapsedMillis % 60000) / 1000, elapsedMillis % 1000);
        String remainingStr = String.format("%d:%02d",
                remainingMillis / 60000, (remainingMillis % 60000) / 1000);

        StringBuilder status = new StringBuilder("\r");
        status.append(String.format("Depth %-3d | ", depth));
        status.append(String.format("Remaining: %-5d | ", itemsLeft));
        status.append(String.format("Sol %-4d | ", solutions.size()));
        status.append(String.format("Processed: %-5d | ", processedCount));
        status.append(String.format("Time: %s | ", elapsedStr));
        status.append(String.format("Remaining: %s", remainingStr));

        if (remainingMillis < 10000) {
            status.append(" [WARNING: Timeout soon!]");
        }

        System.out.print(status.toString());
        System.out.print("\033[K");
    }
}