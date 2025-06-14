package unibs.asd.project;

import java.util.*;

public class MHS {

    /**
     * Computes the Minimal Hitting Sets (MHS) for a given instance represented as a
     * boolean matrix.
     *
     * @param instance A boolean matrix where each row represents a set of features.
     * @return A list of hypotheses representing the MHS.
     */
    public List<Hypothesis> run(boolean[][] instance) {
        if (instance == null || instance.length == 0 || instance[0].length == 0) {
            throw new IllegalArgumentException("Instance must be a non-empty boolean matrix.");
        }
        int m = instance[0].length;
        int n = instance.length;
        List<Hypothesis> current = new ArrayList<>(List.of(new Hypothesis(m, n)));
        List<Hypothesis> solutions = new ArrayList<>();

        while (!current.isEmpty()) {
            List<Hypothesis> next = new ArrayList<>();

            for (Hypothesis h : current) {
                if (check(h)) {
                    solutions.add(h);
                    current.remove(h);
                } else if (h.isEmptyHypothesis()) {
                    next.addAll(generateChildren(h, current));
                } else if (h.mostSignificantBit() != 0) {
                    processNonEmptyHypothesis(h, current, next);

                }
            }
            current = next;
        }
        return solutions;
    }

    private List<Hypothesis> generateSuccessors(Hypothesis h) {
        return h.leftSuccessors();
    }

    private void processNonEmptyHypothesis(Hypothesis h, List<Hypothesis> current, List<Hypothesis> next) {
        Hypothesis h_sec = h.globalInitial();
        current.removeIf(hypothesis -> isGreater(hypothesis.getBin(), h_sec.getBin()));

        if (!current.getFirst().equals(h)) {
            merge(next, generateSuccessors(h));
        }
    }

    public static boolean isGreater(boolean[] bin1, boolean[] bin2) {
        boolean[] trimmed1 = trimLeadingZeros(bin1);
        boolean[] trimmed2 = trimLeadingZeros(bin2);

        if (trimmed1.length != trimmed2.length) {
            return trimmed1.length > trimmed2.length;
        }

        for (int i = 0; i < trimmed1.length; i++) {
            if (trimmed1[i] != trimmed2[i]) {
                return trimmed1[i];
            }
        }
        return false;
    }

    private static boolean[] trimLeadingZeros(boolean[] binary) {
        int firstOne = -1;
        for (int i = 0; i < binary.length; i++) {
            if (binary[i]) {
                firstOne = i;
                break;
            }
        }
        return firstOne == -1 ? new boolean[] { false } : Arrays.copyOfRange(binary, firstOne, binary.length);
    }

    private static List<Hypothesis> merge(Collection<Hypothesis> hypotheses, Collection<Hypothesis> toMerge) {
        Set<Hypothesis> uniqueHyps = new LinkedHashSet<>();
        uniqueHyps.addAll(hypotheses);
        uniqueHyps.addAll(toMerge);

        List<Hypothesis> merged = new ArrayList<>(uniqueHyps);
        merged.sort(Comparator.comparing(Hypothesis::getBin, (bin1, bin2) -> isGreater(bin1, bin2) ? -1 : 1));

        return merged;
    }
 
    public void setFields(Hypothesis h, int m, int n, boolean[][] instance) {
        if (h.isEmptyHypothesis()) {
            boolean[] vector = new boolean[n];
            for (int i = 0; i < n; i++) {
                vector[i] = instance[i][m];
            }
            h.setVector(vector);
        } else {
            boolean[] vector = new boolean[m];
            Arrays.fill(vector, false);
            h.setVector(vector);
        }
    }

    /**
     * Checks if a hypothesis is a hitting set.
     *
     * @param h The hypothesis to check.
     * @return true if the hypothesis is a hitting set, false otherwise.
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

    /**
     * Propagates the hypothesis h_prime from h.
     * This method updates the vector of h_prime by performing a logical OR
     * operation
     * with the vector of h.
     *
     * @param h       The original hypothesis.
     * @param h_prime The hypothesis to be updated.
     */
    public void propagate(Hypothesis h, Hypothesis h_prime) {
        boolean[] vector = h.getVector();
        boolean[] vector_prime = h_prime.getVector();
        boolean[] newVector = new boolean[vector.length];

        for (int i = 0; i < vector.length; i++) {
            newVector[i] = vector[i] || vector_prime[i];
        }
        h_prime.setVector(newVector);
    }

    public List<Hypothesis> generateChildren(Hypothesis h, List<Hypothesis> current) {
        List<Hypothesis> children = new ArrayList<>();

        if (h.isEmptyHypothesis()) {
            return h.leftSuccessors();
        }

        Hypothesis h_p = current.get(0);
        for (int i = 0; i < h.mostSignificantBit(); i++) {
            boolean[] h_pr = h.getBin().clone();
            h_pr[i] = true;
            Hypothesis h_prime = new Hypothesis(h_pr);

            Hypothesis h_s_i = h.initial(h_prime);
            Hypothesis h_s_f = h.finalPred(h_prime);
            int counter = 0;

            while (!h_p.isGreater(h_s_i.getBin()) && h_p.isGreater(h_s_f.getBin())) {
                if ((h_p.distance(h_prime) == 1) && (h_p.distance(h) == 2)) {
                    counter++;
                }
                h_p = current.get(1);
            }

            if (counter == h.cardinality()) {
                children.add(h_prime);
            }
        }

        return children;
    }

}
