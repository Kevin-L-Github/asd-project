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
            List<Hypothesis> toRemove = new ArrayList<>();
            List<Hypothesis> toAdd = new ArrayList<>();

            // Fase 1: Identificare elementi da rimuovere e nuove ipotesi
            for (Hypothesis h : current) {
                System.out.println("\n  Evaluating hypothesis: " + h);

                if (check(h)) {
                    System.out.println("    Found solution: " + h);
                    solutions.add(h);
                    toRemove.add(h);
                } else if (h.isEmptyHypothesis()) {
                    System.out.println("    Hypothesis is empty, generating children");
                    List<Hypothesis> children = generateChildren(h);
                    System.out.println("    Generated " + children.size() + " children");
                    toAdd.addAll(children);
                    toRemove.add(h);
                } else if (h.mostSignificantBit() != 0) {
                    System.out.println("    Processing non-empty hypothesis with MSB: " + h.mostSignificantBit());
                    Hypothesis h_sec = h.globalInitial();
                    System.out.println("    Global initial: " + h_sec);

                    // Aggiungi alla lista di rimozione le ipotesi maggiori
                    for (Hypothesis hypothesis : current) {
                        if (isGreater(hypothesis.getBin(), h_sec.getBin())) {
                            toRemove.add(hypothesis);
                        }
                    }

                    if (!current.isEmpty() && !current.get(0).equals(h)) {
                        System.out.println("    Current first hypothesis is different, merging successors");
                        List<Hypothesis> successors = generateChildren(h);
                        List<Hypothesis> merged = merge(toAdd, successors);
                        System.out.println("    Merged result size: " + merged.size());
                        toAdd.clear();
                        toAdd.addAll(merged);
                    }
                }
            }

            // Fase 2: Applicare tutte le modifiche
            current.removeAll(toRemove);
            next.addAll(toAdd);

            System.out.println("Removed " + toRemove.size() + " hypotheses");
            System.out.println("Next iteration will process " + next.size() + " hypotheses");

            current = next;
        }

        return solutions;
    }

    public static boolean isGreater(boolean[] bin1, boolean[] bin2) {
        // Assicuriamoci che gli array abbiano la stessa lunghezza
        if (bin1.length != bin2.length) {
            throw new IllegalArgumentException("Arrays must have the same length");
        }

        // Confronta bit per bit da sinistra a destra (MSB to LSB)
        for (int i = 0; i < bin1.length; i++) {
            if (bin1[i] && !bin2[i]) {
                return true; // bin1 ha un 1 dove bin2 ha 0
            } else if (!bin1[i] && bin2[i]) {
                return false; // bin1 ha un 0 dove bin2 ha 1
            }
            // Se i bit sono uguali, continua a confrontare
        }

        // Se tutti i bit sono uguali
        return true;
    }

    private static List<Hypothesis> merge(Collection<Hypothesis> hypotheses, Collection<Hypothesis> toMerge) {
        System.out.println("    Merging " + hypotheses.size() + " with " + toMerge.size() + " new hypotheses");
        Set<Hypothesis> uniqueHyps = new LinkedHashSet<>();
        uniqueHyps.addAll(hypotheses);
        uniqueHyps.addAll(toMerge);

        List<Hypothesis> merged = new ArrayList<>(uniqueHyps);
        merged.sort(Comparator.comparing(Hypothesis::getBin, (bin1, bin2) -> isGreater(bin1, bin2) ? -1 : 1));

        System.out.println("    After merge: " + merged.size() + " unique hypotheses");
        return merged;
    }

    public void setFields(Hypothesis h) {
        System.out.println("    Setting fields for hypothesis: " + h);
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

    public boolean check(Hypothesis h) {
        boolean[] vector = h.getVector();
        //System.out.println("    Checking hypothesis with vector: " + Arrays.toString(vector));
        for (int i = 0; i < vector.length; i++) {
            if (!vector[i]) {
                System.out.println("    Hypothesis is NOT a solution (false at position " + i + ")");
                return false;
            }
        }
        System.out.println("    Hypothesis IS a solution");
        return true;
    }

    public void propagate(Hypothesis h, Hypothesis h_prime) {
        System.out.println("    Propagating from " + h + " to " + h_prime);
        boolean[] vector = h.getVector();
        boolean[] vector_prime = h_prime.getVector();
        boolean[] newVector = new boolean[vector.length];

        for (int i = 0; i < vector.length; i++) {
            newVector[i] = vector[i] || vector_prime[i];
        }
        h_prime.setVector(newVector);
        //System.out.println("    New vector after propagation: " + Arrays.toString(newVector));
    }

    public List<Hypothesis> generateChildren(Hypothesis h) {
        System.out.println("    Generating children for: " + h);
        List<Hypothesis> children = new ArrayList<>();

        if (h.isEmptyHypothesis()) {
            System.out.println("    Generating children for empty hypothesis");
            for (int i = 0; i < h.getBin().length; i++) {
                boolean[] h_new = h.getBin().clone();
                h_new[i] = true;
                Hypothesis H_new = new Hypothesis(h_new);
                setFields(H_new);
                children.add(H_new);
                System.out.println("      Generated child: " + H_new);
            }
            return children;
        }

        Hypothesis h_p = current.getFirst();
        System.out.println("    Using h_p: " + h_p);

        for (int i = 0; i < h.mostSignificantBit(); i++) {
            System.out.println("    Processing bit position: " + i);
            boolean[] h_pr = h.getBin().clone();
            h_pr[i] = true;
            Hypothesis h_prime = new Hypothesis(h_pr);

            setFields(h_prime);
            propagate(h, h_prime);

            Hypothesis h_s_i = h.initial(h_prime);
            Hypothesis h_s_f = h.finalPred(h_prime);
            System.out.println("    h_s_i: " + h_s_i);
            System.out.println("    h_s_f: " + h_s_f);

            int counter = 0;
            
            while (!isGreater(h_p, h_s_i) && isGreater(h_p, h_s_f)) {
                System.out.println("    While loop iteration with h_p: " + h_p);
                if ((distance(h_p, h_prime) == 1) && (distance(h_p, h) == 2)) {
                    propagate(h_p, h_prime);
                    counter++;
                    System.out.println("      Propagation counter increased to: " + counter);
                }
                h_p = current.get(1);
            }

            if (counter == h.cardinality()) {
                System.out.println("    Adding child (counter matches cardinality): " + h_prime);
                children.add(h_prime);
            }
        }

        return children;
    }

    public boolean isGreater(Hypothesis h1, Hypothesis h2) {
        boolean result = isGreater(h1.getBin(), h2.getBin());
        System.out.println("    Comparing " + h1 + " > " + h2 + ": " + result);
        return result;
    }

    public int distance(Hypothesis h1, Hypothesis h2) {
        int distance = 0;
        for (int i = 0; i < h2.getBin().length; i++) {
            if (h1.getBin()[i] != h2.getBin()[i]) {
                distance++;
            }
        }
        System.out.println("    Distance between " + h1 + " and " + h2 + ": " + distance);
        return distance;
    }
}