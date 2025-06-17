package unibs.asd.project;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MHS {

    private List<Hypothesis> current;
    private List<Hypothesis> solutions;
    private boolean[][] instance = null;

    public MHS(boolean[][] instance) {
        current = new ArrayList<>();
        solutions = new ArrayList<>();
        this.instance = instance;
        System.out.println("MHS initialized with instance matrix:");
        printInstanceMatrix();
    }

    private void printInstanceMatrix() {
        if (instance == null) {
            System.out.println("Instance matrix is null");
            return;
        }
        for (boolean[] row : instance) {
            System.out.println(Arrays.toString(row));
        }
    }

    public List<Hypothesis> getSolutions() {
        return solutions;
    }

    public boolean[][] getInstance() {
        return instance;
    }

    public void setInstance(boolean[][] instance) {
        this.instance = instance;
        System.out.println("Instance matrix updated:");
        printInstanceMatrix();
    }

    public List<Hypothesis> getCurrent() {
        return current;
    }

    public List<Hypothesis> run() {
        System.out.println("\nStarting MHS algorithm...");
        if (instance == null || instance.length == 0 || instance[0].length == 0) {
            throw new IllegalArgumentException("Instance must be a non-empty boolean matrix.");
        }
        int m = instance[0].length;
        int n = instance.length;
        System.out.println("Matrix dimensions: " + n + " rows x " + m + " columns");

        current.add(new Hypothesis(m, n));
        System.out.println("Initial hypothesis added: " + current.get(0));

        int iteration = 0;
        while (!current.isEmpty()) {
            iteration++;
            System.out.println("\n--- Iteration " + iteration + " ---");
            System.out.println("Current hypotheses: " + current.size());
            List<Hypothesis> next = new ArrayList<>();

            for (int i = 0; i < current.size(); i++) {
                Hypothesis h = current.get(i);
                System.out.println("\nEvaluating hypothesis: " + h);
                if (check(h)) {
                    System.out.println("Found solution: " + h);
                    solutions.add(h);
                    current.remove(i);
                    i--;
                } else if (h.isEmptyHypothesis()) {
                    List<Hypothesis> children = generateChildren(h);
                    next.addAll(children);
                } else if (h.mostSignificantBit() != 0) {
                    Hypothesis h_sec = h.globalInitial();
                    System.out.println("Global initial: " + h_sec);

                    current.removeIf(hyp -> isGreater(hyp, h_sec));

                    if (!current.getFirst().equals(h)) {
                        System.out.println("Current first hypothesis is different, merging successors");
                        next = merge(next, generateChildren(h));
                        System.out.println("Merged result size: " + next.size());
                    }
                }
            }
            current = next;
        }

        return solutions;
    }

    public static boolean isGreater(boolean[] bin1, boolean[] bin2) {
        if (bin1.length != bin2.length) {
            throw new IllegalArgumentException("Gli array devono avere la stessa lunghezza");
        }

        for (int i = 0; i < bin1.length; i++) {
            if (bin1[i] != bin2[i]) {
                return bin1[i]; // true > false
            }
        }

        return true;
    }

    public static boolean isGreater(Hypothesis h1, Hypothesis h2) {
        return isGreater(h1.getBin(), h2.getBin());
    }

    public static boolean isGreaterEqual(boolean[] bin1, boolean[] bin2) {
        if (bin1.length != bin2.length) {
            throw new IllegalArgumentException("Gli array devono avere la stessa lunghezza");
        }

        for (int i = 0; i < bin1.length; i++) {
            if (bin1[i] != bin2[i]) {
                return bin1[i]; // true > false
            }
        }

        // Sono uguali
        return true;
    }

    private static List<Hypothesis> merge(Collection<Hypothesis> hypotheses, Collection<Hypothesis> toMerge) {
        return Stream.concat(hypotheses.stream(), toMerge.stream())
                .distinct()
                .sorted(Comparator.comparing(
                        Hypothesis::getBin,
                        (bin1, bin2) -> isGreaterEqual(bin1, bin2) ? -1 : 1))
                .collect(Collectors.toList());
    }

    public void setFields(Hypothesis h) {
        System.out.println("Setting fields for hypothesis: " + h);
        int n = instance.length;
        int m = instance[0].length;
        boolean[] vector = new boolean[n];
        if (!h.isEmptyHypothesis()) {
            boolean[] bin = h.getBin();
            for (int i = 0; i < m; i++) {
                if (bin[i]) {
                    for (int j = 0; j < n; j++) {
                        if (instance[j][i]) {
                            vector[j] = true;
                        }
                    }
                }
            }
            h.setVector(vector);
            System.out.println("Vector set for hypothesis: " + Arrays.toString(vector));
        } else {
            Arrays.fill(vector, false);
            h.setVector(vector);
        }
    }

    public boolean check(Hypothesis h) {
        boolean[] vector = h.getVector();
        for (int i = 0; i < vector.length; i++) {
            if (!vector[i]) {
                return false;
            }
        }
        System.out.println("Hypothesis IS a solution");
        return true;
    }

    public void propagate(Hypothesis h, Hypothesis h_prime) {
        System.out.println("Propagating from " + h + " to " + h_prime);
        boolean[] vector = h.getVector();
        boolean[] vector_prime = h_prime.getVector();
        boolean[] newVector = new boolean[vector.length];

        for (int i = 0; i < vector.length; i++) {
            newVector[i] = vector[i] || vector_prime[i];
        }
        h_prime.setVector(newVector);
        System.out.println(" New vector after propagation: " + Arrays.toString(newVector));
    }

    public List<Hypothesis> generateChildren(Hypothesis h) {
        List<Hypothesis> children = new ArrayList<>();

        if (h.isEmptyHypothesis()) {
            System.out.println("Generating children for empty hypothesis");
            for (int i = 0; i < h.getBin().length; i++) {
                boolean[] h_new = h.getBin().clone();
                h_new[i] = true;
                Hypothesis H_new = new Hypothesis(h_new);
                setFields(H_new);
                children.add(H_new);
                System.out.println("Generated child: " + H_new);
            }
            return children;
        }

        Iterator<Hypothesis> iterator = current.iterator();
        Hypothesis h_p = iterator.next();
        System.out.println("Using h_p: " + h_p);

        for (int i = 0; i < h.mostSignificantBit(); i++) {

            System.out.println("Processing bit position: " + i);
            boolean[] h_pr = h.getBin().clone();
            h_pr[i] = true;
            Hypothesis h_prime = new Hypothesis(h_pr);

            setFields(h_prime);
            propagate(h, h_prime);

            Hypothesis h_s_i = h.initial_(h_prime);
            Hypothesis h_s_f = h.final_(h_prime);
            System.out.println("h_s_i: " + h_s_i);
            System.out.println("h_s_f: " + h_s_f);

            int counter = 0;

            while (isLessEqual(h_p, h_s_i) && isGreaterEqual(h_p, h_s_f)) {
                System.out.println("While loop iteration with h_p: " + h_p);
                if ((distance(h_p, h_prime) == 1) && (distance(h_p, h) == 2)) {
                    propagate(h_p, h_prime);
                    counter++;
                    System.out.println("Propagation counter increased to: " + counter);
                }

                if (!iterator.hasNext()) {
                    System.out.println("No more hypotheses to process, breaking loop");
                    break;
                } else {
                    System.out.println("Moving to next hypothesis in iterator");
                }
                h_p = iterator.next();
                System.out.println("Using h_p: " + h_p);
            }

            if (counter == h.cardinality()) {
                System.out.println("Adding child (counter matches cardinality): " + h_prime);
                children.add(h_prime);
            }
        }

        return children;
    }

    public boolean isGreaterEqual(Hypothesis h1, Hypothesis h2) {
        boolean result = isGreaterEqual(h1.getBin(), h2.getBin());
        System.out.println(" Comparing " + h1 + " >= " + h2 + ": " + result);
        return result;
    }

    public int distance(Hypothesis h1, Hypothesis h2) {
        int distance = 0;
        for (int i = 0; i < h2.getBin().length; i++) {
            if (h1.getBin()[i] != h2.getBin()[i]) {
                distance++;
            }
        }
        System.out.println("Distance between " + h1 + " and " + h2 + ": " + distance);
        return distance;
    }

    public static boolean isLessEqual(boolean[] bin1, boolean[] bin2) {
        if (bin1.length != bin2.length) {
            throw new IllegalArgumentException("Gli array devono avere la stessa lunghezza");
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
        System.out.println(" Comparing " + h1 + " <= " + h2 + ": " + result);
        return result;
    }

}
