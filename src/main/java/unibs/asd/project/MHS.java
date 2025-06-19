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

    public MHS(boolean[][] instance) {
        current = new ArrayList<>();
        solutions = new ArrayList<>();
        this.instance = instance;
        this.cleanMatrix();
        System.out.println("MHS initialized with instance matrix:");
        DEPTH = 0;
    }

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

    public List<Hypothesis> run() {
        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            throw new IllegalArgumentException("Instance must be a non-empty boolean matrix.");
        }
        int m = matrix[0].length;
        int n = matrix.length;

        Hypothesis emptyHypothesis = new Hypothesis(m, n);
        this.current.addAll(generateChildrenEmHypothesis(emptyHypothesis));
        DEPTH++;
        System.out.println("Initial hypothesis added: " + current.get(0));

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

    private void printStatusBar(int i) {
        int progress = (int) (((i + 1) / (double) current.size()) * 100);
        System.out.print("\rProcessing: " + (i + 1) + "/" + current.size() +
                " [" + ">".repeat(progress / 10) +
                " ".repeat(10 - progress / 10) + "] " +
                progress + "%" +
                " - Solutions: " + solutions.size());
    }

    public List<Hypothesis> generateChildrenEmHypothesis(Hypothesis h) {
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
        Iterator<Hypothesis> it = current.iterator();
        Hypothesis h_p = it.next();

        for (int i = 0; i < h.mostSignificantBit(); i++) {
            boolean[] h_pr = h.getBin().clone();
            h_pr[i] = true;
            Hypothesis h_prime = new Hypothesis(h_pr);

            setFields(h_prime);
            propagate(h, h_prime);

            Hypothesis h_s_i = h.initial_(h_prime);

            if (current.contains(h_s_i)) {
                Hypothesis h_s_f = h.final_(h_prime);
                int counter = 0;
                while (!h_p.equals(h_s_i) && it.hasNext()) {
                    h_p = it.next();
                }
                while (isLessEqual(h_p, h_s_i) && isGreaterEqual(h_p, h_s_f) && it.hasNext()) {

                    if (distance(h_p, h_prime) == 1 && distance(h_p, h) == 2) {
                        propagate(h_p, h_prime);
                        counter++;
                    }
                    h_p = it.next();
                }
                if (counter == DEPTH) {
                    children.add(h_prime);
                }

            } else {
                if (it.hasNext()) {
                    h_p = it.next();
                }
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
                return bin1[i]; // true > false
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

    public int distance(Hypothesis h1, Hypothesis h2) {
        int distance = 0;
        for (int i = 0; i < h2.getBin().length; i++) {
            if (h1.getBin()[i] != h2.getBin()[i]) {
                distance++;
            }
        }
        return distance;
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
}