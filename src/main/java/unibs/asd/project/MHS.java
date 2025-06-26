package unibs.asd.project;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MHS {

    private List<Hypothesis> current;
    private List<Hypothesis> solutions;
    private boolean[][] instance = null;
    private boolean[][] matrix;
    private List<Integer> nonEmptyColumns;
    private int DEPTH;

    /**
     * Attributi usati per la scrittura dei benchmark
     */
    private double computationTime;
    private boolean executed;
    private boolean stopped;
    private boolean stoppedInsideLoop;

    public MHS(boolean[][] instance) {
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

    public List<Hypothesis> run() {
        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            throw new IllegalArgumentException("Instance must be a non-empty boolean matrix.");
        }
        int m = matrix[0].length;
        int n = matrix.length;

        Hypothesis emptyHypothesis = new Hypothesis(m, n);
        this.current.addAll(generateChildrenEmptyHypothesis(emptyHypothesis));
        DEPTH++;

        int iteration = 0;
        while (!current.isEmpty()) {
            iteration++;
            System.out.println("\n--- Iteration " + iteration + " ---");
            System.out.println("Current hypotheses count: " + current.size());
            List<Hypothesis> next = new ArrayList<>();

            for (int i = 0; i < current.size(); i++) {
                Hypothesis h = current.get(i);
                printStatusBar(i);
                if (check(h)) {
                    solutions.add(h);
                    current.remove(i);
                    i--;
                } else if (h.mostSignificantBit() != 0) {
                    Hypothesis h_sec = h.globalInitial();
                    int size = current.size();
                    current.removeIf(hyp -> isGreater(hyp, h_sec));
                    int diff = size - current.size();
                    i -= diff;
                    if (!current.getFirst().equals(h)) {
                        List<Hypothesis> children = generateChildren(h);
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

    public List<Hypothesis> run(long timeoutMillis) {
        long startTime = System.nanoTime();
        long timeoutNanos = timeoutMillis * 1_000_000;

        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            throw new IllegalArgumentException("Instance must be a non-empty boolean matrix.");
        }

        int m = matrix[0].length;
        int n = matrix.length;

        Hypothesis emptyHypothesis = new Hypothesis(m, n);
        this.current.addAll(generateChildrenEmptyHypothesis(emptyHypothesis));
        DEPTH++;

        while (!current.isEmpty()) {
            if (System.nanoTime() - startTime > timeoutNanos) {
                System.out.println("\nTimeout reached. Stopping the algorithm.");
                this.stopped = true;
                break;
            }
            List<Hypothesis> next = new ArrayList<>();
            for (int i = 0; i < current.size(); i++) {
                if (System.nanoTime() - startTime > timeoutNanos) {
                    System.out.println("\nTimeout reached inside loop. Stopping.");
                    this.stopped = true;
                    this.stoppedInsideLoop = true;
                    break;
                }
                Hypothesis h = current.get(i);
                //printStatusBar(i, DEPTH, startTime, timeoutNanos);

                if (check(h)) {
                    solutions.add(h);
                    current.remove(i);
                    i--;
                } else if (h.mostSignificantBit() != 0) {
                    Hypothesis h_sec = h.globalInitial();
                    int size = current.size();
                    boolean removed = current.removeIf(hyp -> isGreater(hyp, h_sec));
                    if (removed) {
                        int diff = size - current.size();
                        i -= diff;
                    }
                    if (!current.getFirst().equals(h)) {
                        List<Hypothesis> children = generateChildren(h);
                        next = merge(next, children);
                    }
                }
            }
            this.current = next;
            DEPTH++;
            //System.out.println("\nEnd of iteration. Next hypotheses: " + current.size());
        }
        this.computationTime = (System.nanoTime() - startTime) / 1000000000F;
        System.out.println("\nAlgorithm completed. Solutions found: " + solutions.size());
        System.out.println("Computation Time: " + this.computationTime);
        this.restoreSolutions();
        this.executed = true;
        return solutions;
    }

    private void printStatusBar(int i) {
        int progress = (int) (((i + 1) / (double) current.size()) * 100);
        System.out.print("\rProcessing: " + (i + 1) + "/" + current.size() +
                " [" + ">".repeat(progress / 10) +
                " ".repeat(10 - progress / 10) + "] " +
                progress + "%" +
                " - Solutions: " + solutions.size());
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

    public List<Hypothesis> generateChildrenEmptyHypothesis(Hypothesis h) {
        List<Hypothesis> children = new ArrayList<>();
        System.out.println("Generating children for empty hypothesis");
        for (int i = 0; i < h.getBin().length; i++) {
            boolean[] h_new = h.getBin().clone();
            h_new[i] = true;
            Hypothesis H_new = new Hypothesis(h_new);
            setFields(H_new);
            children.add(H_new);
        }
        return children;
    }

    public List<Hypothesis> generateChildren(Hypothesis h) {
        List<Hypothesis> children = new ArrayList<>();
        for (int i = 0; i < h.mostSignificantBit(); i++) {
            boolean[] h_pr = h.getBin().clone();
            h_pr[i] = true;
            Hypothesis h_prime = new Hypothesis(h_pr);

            List<Hypothesis> predecessors = h_prime.predecessors();
            predecessors.remove(h);
            int count = 0;

            for (Hypothesis pred : predecessors) {
                if (current.contains(pred)) {
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

    public static boolean isGreater(boolean[] bin1, boolean[] bin2) {
        if (bin1.length != bin2.length) {
            throw new IllegalArgumentException("Arrays must have same length");
        }
        for (int i = 0; i < bin1.length; i++) {
            if (bin1[i] != bin2[i]) {
                return bin1[i];
            }
        }
        return false;
    }

    public static boolean isGreater(Hypothesis h1, Hypothesis h2) {
        return isGreater(h1.getBin(), h2.getBin());
    }

    public static boolean isGreaterEqual(boolean[] bin1, boolean[] bin2) {
        if (bin1.length != bin2.length) {
            throw new IllegalArgumentException("Arrays must have same length");
        }
        for (int i = 0; i < bin1.length; i++) {
            if (bin1[i] != bin2[i]) {
                return bin1[i];
            }
        }
        return true;
    }

    private static List<Hypothesis> merge(Collection<Hypothesis> hypotheses, Collection<Hypothesis> toMerge) {
        return Stream.concat(hypotheses.stream(), toMerge.stream())
                .distinct()
                .sorted(Comparator.comparing(
                        Hypothesis::getBin,
                        (bin1, bin2) -> isGreater(bin1, bin2) ? -1 : 1))
                .collect(Collectors.toList());
    }

    public void setFields(Hypothesis h) {
        int n = matrix.length;
        int m = matrix[0].length;
        boolean[] vector = new boolean[n];
        if (!h.isEmptyHypothesis()) {
            boolean[] bin = h.getBin();
            for (int i = 0; i < m; i++) {
                if (bin[i]) {
                    for (int j = 0; j < n; j++) {
                        if (matrix[j][i]) {
                            vector[j] = true;
                        }
                    }
                }
            }
            h.setVector(vector);
        } else {
            Arrays.fill(vector, false);
            h.setVector(vector);
        }
    }

    /**
     * Checks if the hypothesis is a solution.
     * A solution is a hypothesis where all elements in the vector are true.
     * 
     * @param h
     * @return true if the hypothesis is a solution, false otherwise
     */
    public boolean check(Hypothesis h) {
        boolean[] vector = h.getVector();
        for (int i = 0; i < vector.length; i++) {
            if (!vector[i]) {
                return false;
            }
        }
        return true;
    }

    public void propagate(Hypothesis h, Hypothesis h_prime) {
        boolean[] vector = h.getVector();
        boolean[] vector_prime = h_prime.getVector();
        boolean[] newVector = new boolean[vector.length];

        for (int i = 0; i < vector.length; i++) {
            newVector[i] = vector[i] || vector_prime[i];
        }
        h_prime.setVector(newVector);
    }

    public boolean isGreaterEqual(Hypothesis h1, Hypothesis h2) {
        boolean result = isGreaterEqual(h1.getBin(), h2.getBin());
        return result;
    }

    public static boolean isLessEqual(boolean[] bin1, boolean[] bin2) {
        if (bin1.length != bin2.length) {
        }
        for (int i = 0; i < bin1.length; i++) {
            if (bin1[i] != bin2[i]) {
                return !bin1[i];
            }
        }
        return true;
    }

    public boolean isLessEqual(Hypothesis h1, Hypothesis h2) {
        boolean result = isLessEqual(h1.getBin(), h2.getBin());
        return result;
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

    public void restoreSolutions() {
        List<Hypothesis> restored = new ArrayList<>();

        int originalSize = instance[0].length;

        for (Hypothesis h : solutions) {
            boolean[] compressed = h.getBin();
            boolean[] full = new boolean[originalSize];

            for (int i = 0; i < nonEmptyColumns.size(); i++) {
                int originalIndex = nonEmptyColumns.get(i);
                full[originalIndex] = compressed[i];
            }

            restored.add(new Hypothesis(full));
        }

        this.solutions = restored;
    }

    public List<Hypothesis> getSolutions() {
        return solutions;
    }

    public boolean[][] getInstance() {
        return instance;
    }

    public void setInstance(boolean[][] instance) {
        this.instance = instance;
    }

    public List<Hypothesis> getCurrent() {
        return current;
    }

    public boolean isStoppedInsideLoop() {
        return stoppedInsideLoop;
    }

    public boolean[][] getMatrix() {
        return matrix;
    }

}