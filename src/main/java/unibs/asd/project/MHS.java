package unibs.asd.project;

import java.util.*;

public class MHS {

    private List<Hypothesis> current;
    private List<Hypothesis> solutions;
    private boolean[][] instance = null;
    
    public MHS(boolean[][] instance) {
        current = new ArrayList<>();
        solutions = new ArrayList<>();
        this.instance = instance;
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

    /**
     * Computes the Minimal Hitting Sets (MHS) for a given instance represented as a
     * boolean matrix.
     *
     * @param instance A boolean matrix where each row represents a set of features.
     * @return A list of hypotheses representing the MHS.
     */
    public List<Hypothesis> run() {
        if (instance == null || instance.length == 0 || instance[0].length == 0) {
            throw new IllegalArgumentException("Instance must be a non-empty boolean matrix.");
        }
        int m = instance[0].length;
        int n = instance.length;

        current.add(new Hypothesis(m,n));

        while (!current.isEmpty()) {
            List<Hypothesis> next = new ArrayList<>();

            for (Hypothesis h : current) {
                if (check(h)) {
                    solutions.add(h);
                    current.remove(h);
                } else if (h.isEmptyHypothesis()) {
                    next.addAll(generateChildren(h));
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
 
    public void setFields(Hypothesis h) {
        int n = instance.length;
        int m = instance[0].length;

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

    public List<Hypothesis> generateChildren(Hypothesis h) {
        List<Hypothesis> children = new ArrayList<>();

        if (h.isEmptyHypothesis()) {
            for (int i = 0; i < h.getBin().length; i++){
                boolean[] h_new = h.getBin().clone();
                h_new[i] = true;
                Hypothesis H_new = new Hypothesis(h_new);
                setFields(H_new);
                children.add(H_new);
            }
            return children;
        }

        Hypothesis h_p = current.get(0);
        for (int i = 0; i < h.mostSignificantBit(); i++) {
            boolean[] h_pr = h.getBin().clone();
            h_pr[i] = true;
            Hypothesis h_prime = new Hypothesis(h_pr);

            setFields(h_prime);
            propagate(h, h_prime);
            
            Hypothesis h_s_i = h.initial(h_prime);
            Hypothesis h_s_f = h.finalPred(h_prime);
            int counter = 0;

            while (!isGreater(h_p, h_s_i) && isGreater(h_p, h_s_f)) {
                if ((distance(h_p, h_prime) == 1) && (distance(h_p, h) == 2)) {
                    propagate(h_p, h_prime);
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

    public boolean isGreater(Hypothesis h1, Hypothesis h2) {
        return isGreater(h1.getBin(), h2.getBin());
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



}
