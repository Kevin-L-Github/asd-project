package unibs.asd.mhs;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import unibs.asd.bitset.BitSetAdapter;
import unibs.asd.bitset.BitSetHypothesis;
import unibs.asd.enums.BitSetType;
import unibs.asd.factories.BitSetHypothesisFactory;
import unibs.asd.interfaces.BitVector;
import unibs.asd.interfaces.Hypothesis;
import unibs.asd.interfaces.HypothesisFactory;

public class BaseMHS {
    private HypothesisFactory factory;
    private List<Hypothesis<BitVector>> current;
    private List<Hypothesis<BitVector>> solutions;
    private boolean[][] instance = null;
    private boolean[][] matrix;
    private List<Integer> nonEmptyColumns;
    private int DEPTH;

    private double computationTime;
    private boolean executed;
    private boolean stopped;
    private boolean stoppedInsideLoop;

    public BaseMHS(boolean[][] instance) {
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

    public List<Hypothesis<BitVector>> run(BitSetType type, long timeoutMillis) {
        long startTime = System.nanoTime();
        long timeoutNanos = timeoutMillis * 1_000_000;

        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            throw new IllegalArgumentException("Instance must be a non-empty boolean matrix.");
        }

        int m = matrix[0].length;
        int n = matrix.length;

        Hypothesis<BitVector> emptyHypothesis;

        switch (type) {
            case BITSET:
                this.factory = new BitSetHypothesisFactory();
                emptyHypothesis = new BitSetHypothesis(m, n);
                break;
            default:
                emptyHypothesis = new BitSetHypothesis(m, n);
                break;
        }

        this.current.addAll(generateChildrenEmptyHypothesis(emptyHypothesis));
        DEPTH++;

        while (!current.isEmpty()) {
            if (System.nanoTime() - startTime > timeoutNanos) {
                System.out.println("\nTimeout reached. Stopping the algorithm.");
                this.stopped = true;
                break;
            }
            List<Hypothesis<BitVector>> next = new ArrayList<>();

            for (int i = 0; i < current.size(); i++) {
                if (System.nanoTime() - startTime > timeoutNanos) {
                    System.out.println("\nTimeout reached inside loop. Stopping.");
                    this.stopped = true;
                    this.stoppedInsideLoop = true;
                    break;
                }

                Hypothesis<BitVector> h = current.get(i);
                printStatusBar(i, DEPTH, startTime, timeoutNanos);

                if (check(h)) {
                    solutions.add(h);
                    current.remove(i);
                    i--;
                } else if (h.mostSignificantBit() != 0) {
                    Hypothesis<BitVector> global_initial = h.globalInitial();
                    int r = 0;

                    Iterator<Hypothesis<BitVector>> it = current.iterator();
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
                        List<Hypothesis<BitVector>> children = generateChildren(h);
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

    private List<Hypothesis<BitVector>> generateChildrenEmptyHypothesis(Hypothesis<BitVector> h) {
        List<Hypothesis<BitVector>> children = new ArrayList<>();
        System.out.println("Generating children for empty hypothesis");

        for (int i = 0; i < h.length(); i++) {
            BitVector newBin = h.getBin();
            newBin.set(i);
            Hypothesis<BitVector> H_new = factory.create(newBin);
            setFields(H_new);
            children.add(H_new);
        }
        return children;
    }

    private List<Hypothesis<BitVector>> generateChildren(Hypothesis<BitVector> parent) {
        List<Hypothesis<BitVector>> children = new ArrayList<>();
        BitVector binParent = parent.getBin();
        int LM1 = binParent.mostSignificantBit();

        for (int i = 0; i < LM1; i++) {
            BitVector childBin = binParent.clone();
            childBin.set(i);
            Hypothesis<BitVector> child = factory.create(childBin);
            List<Hypothesis<BitVector>> predecessors = child.predecessors();
            boolean isValid = true;
            for (Hypothesis<BitVector> predecessor : predecessors) {
                if (!binarySearchContains(predecessor)) {
                    System.out.println("Non presente");
                    isValid = false;
                    break;
                }
            }
            if (isValid) {
                setFields(child);
                propagate(parent, child);
                children.add(child);
            }
        }
        return children;
    }

    private boolean binarySearchContains(Hypothesis<BitVector> target) {
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
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return false;
    }

    private boolean isGreater(Hypothesis<BitVector> h1, Hypothesis<BitVector> h2) {
        BitVector bs1 = h1.getBin();
        BitVector bs2 = h2.getBin();
        return bs1.toNaturalValue().compareTo(bs2.toNaturalValue()) > 0;
    }

    private List<Hypothesis<BitVector>> merge(Collection<Hypothesis<BitVector>> hypotheses,
            Collection<Hypothesis<BitVector>> toMerge) {
        return Stream.concat(hypotheses.stream(), toMerge.stream())
                .distinct()
                .sorted((h1, h2) -> isGreater(h1, h2) ? -1 : 1)
                .collect(Collectors.toList());
    }

    private void setFields(Hypothesis<BitVector> h) {
        int n = matrix.length;
        BitVector vector = new BitSetAdapter(n);
        if (h.cardinality() > 0) {
            BitVector bin = h.getBin();
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

    private boolean check(Hypothesis<BitVector> h) {
        return h.getVector().cardinality() == matrix[0].length;
    }

    private void propagate(Hypothesis<BitVector> h, Hypothesis<BitVector> h_prime) {
        BitSetAdapter newVector = (BitSetAdapter) h.getVector().clone();
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
        List<Hypothesis<BitVector>> restored = new ArrayList<>();
        int originalSize = instance[0].length;
        for (Hypothesis<BitVector> h : solutions) {
            BitVector compressed = h.getBin();
            BitSetAdapter full = new BitSetAdapter(originalSize);
            for (int i = 0; i < nonEmptyColumns.size(); i++) {
                int originalIndex = nonEmptyColumns.get(i);
                full.set(originalIndex, compressed.get(i));
            }
            restored.add(factory.create(full));
        }
        this.solutions = restored;
    }

    public List<Hypothesis<BitVector>> getSolutions() {
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