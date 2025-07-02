package unibs.asd.mhs;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import unibs.asd.bitset.BitSetHypothesis;
import unibs.asd.bools.BoolsHypothesis;
import unibs.asd.enums.BitSetType;
import unibs.asd.factories.BitSetHypothesisFactory;
import unibs.asd.factories.BoolsHypothesisFactory;
import unibs.asd.factories.FastBitSetHypothesisFactory;
import unibs.asd.factories.RoaringHypothesisFactory;
import unibs.asd.fastbitset.FastHypothesis;
import unibs.asd.interfaces.BitVector;
import unibs.asd.interfaces.Hypothesis;
import unibs.asd.interfaces.HypothesisFactory;
import unibs.asd.roaringbitmap.RoaringHypothesis;

public class BaseMHS {

    private HypothesisFactory factory;
    private List<Hypothesis> current;
    private List<Hypothesis> solutions;
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

    public List<Hypothesis> run(BitSetType type, long timeoutMillis) {

        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            throw new IllegalArgumentException("Instance must be a non-empty boolean matrix.");
        }

        long startTime = System.nanoTime();
        long timeoutNanos = timeoutMillis * 1_000_000;

        int m = matrix[0].length;
        int n = matrix.length;

        Hypothesis emptyHypothesis = getInitialHypothesis(type, m, n);

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
                    Hypothesis global_initial = h.globalInitial();
                    int r = 0;

                    Iterator<Hypothesis> it = current.iterator();
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
                        List<Hypothesis> children = generateChildren(h);
                        next = merge(next, children);
                    }
                }
            }
            this.current = next;
            DEPTH++;
        }
        this.computationTime = (System.nanoTime() - startTime) / 1000000000F;
        this.restoreSolutions();
        this.executed = true;
        return solutions;
    }

    private Hypothesis getInitialHypothesis(BitSetType type, int m, int n) {
        switch (type) {
            case BitSetType.BITSET:
                this.factory = new BitSetHypothesisFactory();
                return  new BitSetHypothesis(m, n);
            case BitSetType.BOOLS_ARRAY:
                this.factory = new BoolsHypothesisFactory();
                return  new BoolsHypothesis(m, n);
            case BitSetType.ROARING_BIT_MAP:
                this.factory = new RoaringHypothesisFactory();
                return  new RoaringHypothesis(m, n);
            case BitSetType.FAST_BITSET:
                this.factory = new FastBitSetHypothesisFactory();
                return new FastHypothesis(m, n);
            default:
                throw new IllegalArgumentException("Scegliere un tipo di implementazione");
        }
    }

    private List<Hypothesis> generateChildrenEmptyHypothesis(Hypothesis parent) {
        List<Hypothesis> children = new ArrayList<>();
        //System.out.println("Generating children for empty hypothesis");

        for (int i = 0; i < parent.length(); i++) {
            Hypothesis child = factory.create(parent.getBin());
            child.set(i);
            setFields(child);
            children.add(child);
        }
        return children;
    }

    private List<Hypothesis> generateChildren(Hypothesis parent) {
        List<Hypothesis> children = new ArrayList<>();
        int LM1 = parent.getBin().mostSignificantBit();
        for (int i = 0; i < LM1; i++) {
            Hypothesis child = parent.clone();
            child.set(i);
            List<Hypothesis> predecessors = child.predecessors();
            boolean isValid = true;
            for (Hypothesis predecessor : predecessors) {
                if (!binarySearchContains(predecessor)) {
                    isValid = false;
                    break;
                }
            }
            if (isValid) {
                setFields(child);
                //propagate(parent, child);
                children.add(child);
            }
        }
        return children;
    }

    private boolean binarySearchContains(Hypothesis target) {
        //System.out.println("Is " + target + " in current?");
        int low = 0;
        int high = current.size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            Hypothesis middle = current.get(mid);
            //System.out.println("Middle: " + middle);
            if (target.equals(middle)) {
                //System.out.println("Founded!");
                return true;
            } else if (isGreater(middle, target)) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return false;
    }

    public static boolean isGreater(Hypothesis h1, Hypothesis h2) {
        BitVector bs1 = h1.getBin();
        BitVector bs2 = h2.getBin();
        for (int i = 0; i < h1.length(); i++) {
            if (bs1.get(i) != bs2.get(i)) {
                return bs1.get(i);
            }
        }
        return false;
    }

    private List<Hypothesis> merge(Collection<Hypothesis> hypotheses,
            Collection<Hypothesis> toMerge) {
        return Stream.concat(hypotheses.stream(), toMerge.stream())
                .sorted((h1, h2) -> isGreater(h1, h2) ? -1 : 1)
                .collect(Collectors.toList());
    }

    private void setFields(Hypothesis hypothesis) {
        int n = matrix.length;
        BitVector vector = this.factory.createVector(n);
        if (hypothesis.cardinality() > 0) {
            BitVector bin = hypothesis.getBin();
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
        hypothesis.setVector(vector);
    }

    private boolean check(Hypothesis h) {
        return h.isSolution();
    }

    private void propagate(Hypothesis parent, Hypothesis child) {
        child.or(parent);
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
        List<Hypothesis> restored = new ArrayList<>();
        int originalSize = instance[0].length;
        for (Hypothesis h : solutions) {
            BitVector compressed = h.getBin();
            Hypothesis full = factory.create(originalSize);
            for (int i = 0; i < nonEmptyColumns.size(); i++) {
                int originalIndex = nonEmptyColumns.get(i);
                full.set(originalIndex, compressed.get(i));
            }
            restored.add(full);
        }
        this.solutions = restored;
    }

    public List<Hypothesis> getSolutions() {
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