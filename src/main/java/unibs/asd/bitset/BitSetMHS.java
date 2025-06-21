package unibs.asd.bitset;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BitSetMHS {
    private List<BitSetHypothesis> current;
    private List<BitSetHypothesis> solutions;
    private boolean[][] instance = null;
    private boolean[][] matrix;
    private List<Integer> nonEmptyColumns;
    private int DEPTH;

    // Attributi per benchmark
    private double computationTime;
    private boolean executed;
    private boolean stopped;
    private boolean stoppedInsideLoop;

    public BitSetMHS(boolean[][] instance) {
        this.current = new ArrayList<>();
        this.solutions = new ArrayList<>();
        this.instance = instance;
        this.DEPTH = 0;
        this.computationTime = 0;
        this.executed = false;
        this.stopped = false;
        this.stoppedInsideLoop = false;
        this.cleanMatrix();
    }

    public List<BitSetHypothesis> run() {
        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            throw new IllegalArgumentException("Instance must be a non-empty boolean matrix.");
        }

        int m = matrix[0].length;
        int n = matrix.length;

        BitSetHypothesis emptyHypothesis = new BitSetHypothesis(m, n);
        this.current.addAll(generateChildrenEmptyHypothesis(emptyHypothesis));
        DEPTH++;

        int iteration = 0;
        while (!current.isEmpty()) {
            iteration++;
            System.out.println("\n--- Iteration " + iteration + " ---");
            System.out.println("Current hypotheses count: " + current.size());
            List<BitSetHypothesis> next = new ArrayList<>();

            for (int i = 0; i < current.size(); i++) {
                BitSetHypothesis h = current.get(i);

                if (check(h)) {
                    solutions.add(h);
                    current.remove(i);
                    i--;
                } else if (h.mostSignificantBit() != -1 && h.mostSignificantBit() != 0) {
                    BitSetHypothesis h_sec = h.globalInitial();
                    int size = current.size();
                    current.removeIf(hyp -> isGreater(hyp, h_sec));
                    int diff = size - current.size();
                    i -= diff;

                    if (!current.isEmpty() && !current.get(0).equals(h)) {
                        List<BitSetHypothesis> children = generateChildren(h);
                        next = merge(next, children);
                    }
                }
            }
            this.current = next;
            DEPTH++;
            System.out.println("\nEnd of iteration. Next hypotheses: " + current.size());
        }
        System.out.println("\nAlgorithm completed. Solutions found: " + solutions.size());
        this.restoreSolutions();
        return solutions;
    }

    public List<BitSetHypothesis> run(long timeoutMillis) {
        long startTime = System.nanoTime();
        long timeoutNanos = timeoutMillis * 1_000_000;

        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            throw new IllegalArgumentException("Instance must be a non-empty boolean matrix.");
        }

        int m = matrix[0].length;
        int n = matrix.length;

        BitSetHypothesis emptyHypothesis = new BitSetHypothesis(m, n);
        this.current.addAll(generateChildrenEmptyHypothesis(emptyHypothesis));
        DEPTH++;

        while (!current.isEmpty()) {
            if (System.nanoTime() - startTime > timeoutNanos) {
                System.out.println("\nTimeout reached. Stopping the algorithm.");
                this.stopped = true;
                break;
            }
            List<BitSetHypothesis> next = new ArrayList<>();

            for (int i = 0; i < current.size(); i++) {
                if (System.nanoTime() - startTime > timeoutNanos) {
                    System.out.println("\nTimeout reached inside loop. Stopping.");
                    this.stopped = true;
                    this.stoppedInsideLoop = true;
                    break;
                }

                BitSetHypothesis h = current.get(i);
                printStatusBar(i, DEPTH, startTime, timeoutNanos);

                if (check(h)) {
                    solutions.add(h);
                    current.remove(i);
                    i--;
                } else if (h.mostSignificantBit() != -1 && h.mostSignificantBit() != 0) {
                    BitSetHypothesis h_sec = h.globalInitial();
                    int size = current.size();
                    current.removeIf(hyp -> isGreater(hyp, h_sec));
                    int diff = size - current.size();
                    i -= diff;

                    if (!current.isEmpty() && !current.get(0).equals(h)) {
                        List<BitSetHypothesis> children = generateChildren(h);
                        next = merge(next, children);
                    }
                }
            }
            this.current = next;
            DEPTH++;
            System.out.println("\nEnd of iteration. Next hypotheses: " + current.size());
        }
        this.computationTime = (System.nanoTime() - startTime) / 1000000000F;
        System.out.println("\nAlgorithm completed. Solutions found: " + solutions.size());
        System.out.println("Computation Time: " + this.computationTime);
        this.restoreSolutions();
        this.executed = true;
        return solutions;
    }

    private List<BitSetHypothesis> generateChildrenEmptyHypothesis(BitSetHypothesis h) {
        List<BitSetHypothesis> children = new ArrayList<>();
        System.out.println("Generating children for empty hypothesis");

        for (int i = 0; i < h.length(); i++) {
            BitSet newBin = h.getBin();
            newBin.set(i);
            BitSetHypothesis H_new = new BitSetHypothesis(newBin);
            setFields(H_new);
            children.add(H_new);
        }
        return children;
    }

    private List<BitSetHypothesis> generateChildren(BitSetHypothesis h) {
        List<BitSetHypothesis> children = new ArrayList<>();
        int msb = h.mostSignificantBit();

        if (msb == -1)
            return children;

        for (int i = 0; i < msb; i++) {
            BitSet h_pr = h.getBin();
            h_pr.set(i);
            BitSetHypothesis h_prime = new BitSetHypothesis(h_pr);

            // Implementazione semplificata dei predecessori per BitSet
            int count = 0;
            for (BitSetHypothesis hyp : current) {
                BitSet hypBin = hyp.getBin();
                if (isSubset(h_prime.getBin(), hypBin) && !h_prime.equals(hyp)) {
                    count++;
                }
            }

            if (count == DEPTH) {
                setFields(h_prime);
                propagate(h, h_prime);
                children.add(h_prime);
            }
        }
        return children;
    }

    private boolean isSubset(BitSet subset, BitSet superset) {
        BitSet temp = (BitSet) subset.clone();
        temp.andNot(superset);
        return temp.isEmpty();
    }

    private boolean isGreater(BitSetHypothesis h1, BitSetHypothesis h2) {
        BitSet bs1 = h1.getBin();
        BitSet bs2 = h2.getBin();

        // Confronto lessicografico per BitSet
        int firstDiff = bs1.nextSetBit(0) - bs2.nextSetBit(0);
        if (firstDiff != 0)
            return firstDiff > 0;

        return bs1.cardinality() > bs2.cardinality();
    }

    private List<BitSetHypothesis> merge(Collection<BitSetHypothesis> hypotheses,
            Collection<BitSetHypothesis> toMerge) {
        return Stream.concat(hypotheses.stream(), toMerge.stream())
                .distinct()
                .sorted((h1, h2) -> isGreater(h1, h2) ? -1 : 1)
                .collect(Collectors.toList());
    }

    private void setFields(BitSetHypothesis h) {
        int n = matrix.length;
        BitSet vector = new BitSet(n);

        if (h.cardinality() > 0) {
            BitSet bin = h.getBin();
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

    private boolean check(BitSetHypothesis h) {
        BitSet vector = h.getVector();
        return vector.cardinality() == matrix.length;
    }

    private void propagate(BitSetHypothesis h, BitSetHypothesis h_prime) {
        BitSet newVector = (BitSet) h.getVector().clone();
        newVector.or(h_prime.getVector());
        h_prime.setVector(newVector);
    }

    private void cleanMatrix() {
        // Implementazione identica alla versione originale
        // ...
    }

    private void restoreSolutions() {
        List<BitSetHypothesis> restored = new ArrayList<>();
        int originalSize = instance[0].length;

        for (BitSetHypothesis h : solutions) {
            BitSet compressed = h.getBin();
            BitSet full = new BitSet(originalSize);

            for (int i = 0; i < nonEmptyColumns.size(); i++) {
                int originalIndex = nonEmptyColumns.get(i);
                full.set(originalIndex, compressed.get(i));
            }

            restored.add(new BitSetHypothesis(full));
        }

        this.solutions = restored;
    }

    public List<BitSetHypothesis> getSolutions() {
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
                "\rProcess: %3d/%-3d [%s] %3d%% | Solutions: %4d | Depth: %3d | Time: %2ds",
                i + 1,
                current.size(),
                progressBar,
                progress,
                solutions.size(),
                depth,
                remainingSeconds);

        System.out.print(output);
    }
}