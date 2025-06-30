package unibs.asd.fastbitset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import unibs.asd.interfaces.Hypothesis;

public class FastMHS {

    private List<FastHypothesis> current;
    private List<FastHypothesis> solutions;
    private HashSet<FastBitSet> bucket;
    private boolean[][] instance = null;
    private boolean[][] matrix;
    private List<Integer> nonEmptyColumns;
    private int DEPTH;

    private double computationTime;
    private boolean executed;
    private boolean stopped;
    private boolean stoppedInsideLoop;

    public FastMHS(boolean[][] instance) {
        this.current = new ArrayList<>();
        this.solutions = new ArrayList<>();
        this.bucket = new HashSet<>();
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

        while (!current.isEmpty()) {
            if (System.nanoTime() - startTime > timeoutNanos) {
                System.out.println("\nTimeout reached. Stopping the algorithm.");
                this.stopped = true;
                break;
            }
            List<FastHypothesis> next = new ArrayList<>();
            HashSet<FastBitSet> nextBucket = new HashSet<>();

            for (int i = 0; i < current.size(); i++) {
                if (System.nanoTime() - startTime > timeoutNanos) {
                    System.out.println("\nTimeout reached inside loop. Stopping.");
                    this.stopped = true;
                    this.stoppedInsideLoop = true;
                    break;
                }

                FastHypothesis h = current.get(i);
                printStatusBar(i, DEPTH, startTime, timeoutNanos);

                if (check(h)) {
                    solutions.add(h);
                    current.remove(i);
                    bucket.remove(h.getBin());
                    i--;
                } else if (h.mostSignificantBit() != 0) {
                    FastHypothesis global_initial = h.globalInitial();
                    int r = 0;
                    Iterator<FastHypothesis> it = current.iterator();
                    boolean searching = true;
                    while (it.hasNext() && searching) {
                        FastHypothesis hyp = it.next();
                        if (isGreater(hyp.getBin(), global_initial.getBin())) {
                            r++;
                            it.remove();
                            bucket.remove(hyp.getBin());
                        } else {
                            searching = false;
                        }
                    }
                    if (r > 0) {
                        i -= r;
                    }
                    if (!current.isEmpty() && !current.get(0).equals(h)) {
                        List<FastHypothesis> children = generateChildren(h);
                        next = merge(next, children);
                        children.forEach(child -> nextBucket.add(child.getBin()));
                    }
                }


            }
            this.current = next;
            this.bucket = nextBucket;
            DEPTH++;
        }
        this.computationTime = (System.nanoTime() - startTime) / 1000000000F;
        System.out.println("\nAlgorithm completed. Solutions found: " + solutions.size());
        System.out.println("Computation Time: " + this.computationTime);
        this.restoreSolutions();
        this.executed = true;
        return solutions;
    }

    private List<FastHypothesis> generateChildrenEmptyHypothesis(FastHypothesis h) {
        List<FastHypothesis> children = new ArrayList<>();
        System.out.println("Generating children for empty hypothesis");

        for (int i = 0; i < h.length(); i++) {
            FastBitSet newBin = h.getBin();
            newBin.set(i);
            FastHypothesis H_new = new FastHypothesis(newBin);
            setFields(H_new);
            children.add(H_new);
        }
        return children;
    }

    private List<FastHypothesis> generateChildren(FastHypothesis h) {
        List<FastHypothesis> children = new ArrayList<>();
        FastBitSet hBin = h.getBin();
        int length = hBin.nextSetBit(0);
        for (int i = 0; i < length; i++) {
            FastBitSet hPrimeBin = (FastBitSet) hBin.clone();
            hPrimeBin.set(i);
            FastHypothesis hPrime = new FastHypothesis(hPrimeBin);
            List<Hypothesis> predecessors = hPrime.predecessors();
            boolean isValid = true;
            for (Hypothesis p : predecessors) {
                if (!bucket.contains(p.getBin())) {
                    isValid = false;
                    break;
                }
            }
            if (isValid) {
                setFields(hPrime);
                propagate(h, hPrime);
                children.add(hPrime);
            }
        }
        return children;
    }

    public static boolean isGreater(FastBitSet a, FastBitSet b) {
        if (a.logicalSize != b.logicalSize) {
            throw new IllegalArgumentException("FastBitSets must have the same logical size");
        }
        for (int i = 0; i < a.logicalSize; i++) {
            boolean aBit = a.get(i);
            boolean bBit = b.get(i);

            if (aBit != bBit) {
                return aBit;
            }
        }
        return false;
    }

    /**
     * Unisce i due insiemi e li riordina in base al loro valore naturale
     * 
     * @param hypotheses
     * @param toMerge
     * @return
     */
    private List<FastHypothesis> merge(Collection<FastHypothesis> hypotheses,
            Collection<FastHypothesis> toMerge) {
        return Stream.concat(hypotheses.stream(), toMerge.stream())
                .sorted((h1, h2) -> isGreater(h1.getBin(), h2.getBin()) ? -1 : 1)
                .collect(Collectors.toList());
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

    private void printStatusBar(int i, int depth, long startTime, long timeoutNanos) {
        int progress = (int) (((i + 1) / (double) current.size()) * 100);
        long elapsedNanos = System.nanoTime() - startTime;
        long remainingSeconds = Math.max((timeoutNanos - elapsedNanos) / 1_000_000_000, 0);

        String progressBar = String.format("%-10s", ">".repeat(progress / 10)).replace(' ', ' ');

        String output = String.format(
                "\rProcess: %3d/%-3d [%s] %3d%%, Solutions: %4d, Depth: %3d, Time: %2ds",
                i + 1,
                current.size(),
                progressBar,
                progress,
                solutions.size(),
                depth,
                remainingSeconds);

        System.out.print(output);
    }

    public List<FastHypothesis> run() {
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

        int iteration = 0;
        while (!current.isEmpty()) {
            iteration++;
            System.out.println("\n--- Iteration " + iteration + " ---");
            System.out.println("Current hypotheses count: " + current.size());
            List<FastHypothesis> next = new ArrayList<>();
            HashSet<FastBitSet> nextBucket = new HashSet<>();

            for (int i = 0; i < current.size(); i++) {
                FastHypothesis h = current.get(i);

                if (check(h)) {
                    solutions.add(h);
                    current.remove(i);
                    bucket.remove(h.getBin());
                    i--;
                } else if (h.mostSignificantBit() != -1 && h.mostSignificantBit() != 0) {
                    FastHypothesis h_sec = h.globalInitial();
                    int size = current.size();

                    Iterator<FastHypothesis> it = current.iterator();
                    while (it.hasNext()) {
                        FastHypothesis hyp = it.next();
                        if (isGreater(hyp.getBin(), h_sec.getBin())) {
                            it.remove();
                            bucket.remove(hyp.getBin());
                        }
                    }

                    i -= (size - current.size());

                    if (!current.isEmpty() && !current.get(0).equals(h)) {
                        List<FastHypothesis> children = generateChildren(h);
                        next = merge(next, children);
                        children.forEach(child -> nextBucket.add(child.getBin()));
                    }
                }
            }
            this.current = next;
            this.bucket = nextBucket;
            DEPTH++;
            System.out.println("\nEnd of iteration. Next hypotheses: " + current.size());
        }
        System.out.println("\nAlgorithm completed. Solutions found: " + solutions.size());
        this.restoreSolutions();
        return solutions;
    }
}