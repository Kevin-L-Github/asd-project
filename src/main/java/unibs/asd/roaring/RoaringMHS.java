package unibs.asd.roaring;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RoaringMHS {

    private List<RoaringHypothesis> current;
    private List<RoaringHypothesis> solutions;
    private boolean[][] instance = null;
    private boolean[][] matrix;
    private List<Integer> nonEmptyColumns;
    private int DEPTH;

    // Attributi per benchmark
    private double computationTime;
    private boolean executed;
    private boolean stopped;
    private boolean stoppedInsideLoop;

    public RoaringMHS(boolean[][] instance) {
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

    public List<RoaringHypothesis> run() {
        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            throw new IllegalArgumentException("Instance must be a non-empty boolean matrix.");
        }

        int m = matrix[0].length;
        int n = matrix.length;

        RoaringHypothesis emptyHypothesis = new RoaringHypothesis(m, n);
        this.current.addAll(generateChildrenEmptyHypothesis(emptyHypothesis));
        DEPTH++;

        int iteration = 0;
        while (!current.isEmpty()) {
            iteration++;
            System.out.println("\n--- Iteration " + iteration + " ---");
            System.out.println("Current hypotheses count: " + current.size());
            List<RoaringHypothesis> next = new ArrayList<>();

            for (int i = 0; i < current.size(); i++) {
                RoaringHypothesis h = current.get(i);

                if (check(h)) {
                    solutions.add(h);
                    current.remove(i);
                    i--;
                } else if (h.mostSignificantBit() != -1 && h.mostSignificantBit() != 0) {
                    RoaringHypothesis h_sec = h.globalInitial();
                    int size = current.size();
                    current.removeIf(hyp -> isGreater(hyp, h_sec));
                    int diff = size - current.size();
                    i -= diff;

                    if (!current.isEmpty() && !current.get(0).equals(h)) {
                        List<RoaringHypothesis> children = generateChildren(h);
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

    public List<RoaringHypothesis> run(long timeoutMillis) {
        long startTime = System.nanoTime();
        long timeoutNanos = timeoutMillis * 1_000_000;

        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            throw new IllegalArgumentException("Instance must be a non-empty boolean matrix.");
        }

        int m = matrix[0].length;
        int n = matrix.length;

        RoaringHypothesis emptyHypothesis = new RoaringHypothesis(m, n);
        this.current.addAll(generateChildrenEmptyHypothesis(emptyHypothesis));
        DEPTH++;

        while (!current.isEmpty()) {
            if (System.nanoTime() - startTime > timeoutNanos) {
                System.out.println("\nTimeout reached. Stopping the algorithm.");
                this.stopped = true;
                break;
            }
            List<RoaringHypothesis> next = new ArrayList<>();

            for (int i = 0; i < current.size(); i++) {
                if (System.nanoTime() - startTime > timeoutNanos) {
                    System.out.println("\nTimeout reached inside loop. Stopping.");
                    this.stopped = true;
                    this.stoppedInsideLoop = true;
                    break;
                }

                RoaringHypothesis h = current.get(i);
                printStatusBar(i, DEPTH, startTime, timeoutNanos);

                if (check(h)) {
                    solutions.add(h);
                    current.remove(i);
                    i--;
                } else if (h.mostSignificantBit() != 0) {
                    RoaringHypothesis global_initial = h.globalInitial();
                    int r = 0;

                    Iterator<RoaringHypothesis> it = current.iterator();
                    boolean searching = true;
                    while (it.hasNext() && searching) {
                        if (isGreater(it.next(), global_initial)) {
                            r++;
                            it.remove();
                        } else {
                            searching = false;
                        }
                    }
                    if (r > 0) {
                        i -= r;
                    }
                    if (!current.isEmpty() && !current.get(0).equals(h)) {
                        List<RoaringHypothesis> children = generateChildren(h);
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

    private List<RoaringHypothesis> generateChildrenEmptyHypothesis(RoaringHypothesis h) {
        List<RoaringHypothesis> children = new ArrayList<>();
        System.out.println("Generating children for empty hypothesis");

        for (int i = 0; i < h.length(); i++) {
            RoaringBitmapAdapter newBin = h.getBin();
            newBin.set(i);
            RoaringHypothesis H_new = new RoaringHypothesis(newBin);
            setFields(H_new);
            children.add(H_new);
        }
        return children;
    }

    private List<RoaringHypothesis> generateChildren(RoaringHypothesis h) {
        List<RoaringHypothesis> children = new ArrayList<>();
        RoaringBitmapAdapter hBin = h.getBin();
        int length = hBin.mostSignificantBit();

        for (int i = 0; i < length; i++) {
            RoaringBitmapAdapter hPrimeBin = (RoaringBitmapAdapter) hBin.clone(); // Clona invece di modificare direttamente
            hPrimeBin.set(i);
            RoaringHypothesis hPrime = new RoaringHypothesis(hPrimeBin);
            List<RoaringHypothesis> predecessors = hPrime.predecessors();
            boolean isValid = true;
            for (RoaringHypothesis p : predecessors) {
                if (!binarySearchContains(p)) {
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

    private boolean binarySearchContains(RoaringHypothesis target) {
        BigInteger targetValue = target.getBin().toNaturalValue();
        int low = 0;
        int high = current.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            BigInteger midValue = current.get(mid).getBin().toNaturalValue();

            int cmp = midValue.compareTo(targetValue);

            if (cmp == 0) {
                return true;
            } else if (cmp > 0) {
                // Lista decrescente: target è "più grande", quindi va a sinistra
                low = mid + 1;
            } else {
                // target è "più piccolo", quindi va a destra
                high = mid - 1;
            }
        }

        return false;
    }

    private boolean isGreater(RoaringHypothesis h1, RoaringHypothesis h2) {
        RoaringBitmapAdapter bs1 = h1.getBin();
        RoaringBitmapAdapter bs2 = h2.getBin();
        return bs1.toNaturalValue().compareTo(bs2.toNaturalValue()) > 0;
    }

    private List<RoaringHypothesis> merge(Collection<RoaringHypothesis> hypotheses,
            Collection<RoaringHypothesis> toMerge) {
        return Stream.concat(hypotheses.stream(), toMerge.stream())
                .distinct()
                .sorted((h1, h2) -> isGreater(h1, h2) ? -1 : 1)
                .collect(Collectors.toList());
    }

    private void setFields(RoaringHypothesis h) {
        int n = matrix.length;
        RoaringBitmapAdapter vector = new RoaringBitmapAdapter(n);
        if (h.cardinality() > 0) {
            RoaringBitmapAdapter bin = h.getBin();
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

    private boolean check(RoaringHypothesis h) {
        return h.getVector().cardinality() == matrix[0].length;
    }

    private void propagate(RoaringHypothesis h, RoaringHypothesis h_prime) {
        RoaringBitmapAdapter newVector = (RoaringBitmapAdapter) h.getVector().clone();
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
        List<RoaringHypothesis> restored = new ArrayList<>();
        int originalSize = instance[0].length;

        for (RoaringHypothesis h : solutions) {
            RoaringBitmapAdapter compressed = h.getBin();
            RoaringBitmapAdapter full = new RoaringBitmapAdapter(originalSize);

            for (int i = 0; i < nonEmptyColumns.size(); i++) {
                int originalIndex = nonEmptyColumns.get(i);
                full.set(originalIndex, compressed.get(i));
            }

            restored.add(new RoaringHypothesis(full));
        }

        this.solutions = restored;
    }

    public List<RoaringHypothesis> getSolutions() {
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
