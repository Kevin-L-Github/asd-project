package unibs.asd.project;

import java.util.*;
import java.util.stream.Collectors;

public class MHS {

    public List<Hypothesis> run(boolean[][] instance) {
        int m = instance[0].length;
        List<Hypothesis> current = new ArrayList<>(List.of(new Hypothesis(m)));
        List<Hypothesis> solutions = new ArrayList<>();

        while (!current.isEmpty()) {
            List<Hypothesis> next = new ArrayList<>();

            for (Hypothesis h : current) {
                if (check(instance, h.getBin())) {
                    solutions.add(h);
                    current.remove(h);
                } else if (isEmptyHypothesis(h)) {
                    next.addAll(generateSuccessors(h));
                } else if (h.mostSignificantBit() != 0) {
                    processNonEmptyHypothesis(h, current, next);
                }
            }

            current = next;
        }
        return solutions;
    }

    private List<Hypothesis> generateSuccessors(Hypothesis h) {
        return h.leftSuccessors().stream()
                .map(Hypothesis::new)
                .collect(Collectors.toList());
    }

    private void processNonEmptyHypothesis(Hypothesis h, List<Hypothesis> current, List<Hypothesis> next) {
        Hypothesis h_sec = h.globalInitial();
        current.removeIf(hypothesis -> isGreater(hypothesis.getBin(), h_sec.getBin()));

        if (!current.getFirst().equals(h)) {
            merge(next, generateSuccessors(h));
        }
    }

    private boolean check(boolean[][] instance, boolean[] h) {
        return Arrays.stream(instance).allMatch(row -> intersect(h, row));
    }

    private boolean intersect(boolean[] h1, boolean[] h2) {
        if (h1.length != h2.length) {
            throw new IllegalArgumentException("Arrays must be of the same length");
        }

        for (int i = 0; i < h1.length; i++) {
            if (h1[i] && h2[i]) {
                return true;
            }
        }
        return false;
    }

    private boolean isEmptyHypothesis(Hypothesis h) {
        for (boolean b : h.getBin()) {
            if (b) {
                return false;
            }
        }
        return true;
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
}
