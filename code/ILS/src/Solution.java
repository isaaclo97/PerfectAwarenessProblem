import java.time.Duration;
import java.time.Instant;
import java.util.BitSet;
import java.util.LinkedList;

/**
 * A solution is a set of seed nodes (initial spreaders), stored as a bit set.
 */
public class Solution {
    public static Instance instance;

    private BitSet solutionBw;
    private int solutionValue;
    private int numberAware;
    private int[] awareNeighs;

    public Solution(BitSet solution) {
        // Clone so this solution owns its state: callers cannot modify it through the BitSet they passed.
        this.solutionBw = (BitSet) solution.clone();
        this.solutionValue = this.solutionBw.cardinality();
        this.numberAware = this.solutionValue;
    }

    public Solution() {
        this.solutionBw = new BitSet();
        this.solutionValue = 0;
        this.numberAware = 0;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("(");
        for (int index = solutionBw.nextSetBit(0); index >= 0; index = solutionBw.nextSetBit(index + 1)) {
            s.append(index).append(", ");
        }
        return s.append(")").toString();
    }

    /** Objective function: number of seed nodes. */
    public int solutionValue() {
        return solutionValue;
    }

    /** Adds a seed and updates the state of its neighbourhood. */
    public void addNode(int node) {
        if (!this.isIn(node)) {
            this.solutionBw.set(node);
            this.solutionValue = this.solutionBw.cardinality();
            this.numberAware = this.solutionValue;
            instance.setState(node, 2);
            for (int neigh : instance.graph.get(node)) {
                instance.setState(neigh, 1);
            }
        }
    }

    /** Adds a seed without updating the state of the instance. */
    public void addNodeUnique(int node) {
        this.solutionBw.set(node);
        this.solutionValue = this.solutionBw.cardinality();
        this.numberAware = this.solutionValue;
    }

    public void removeNode(int node) {
        this.solutionBw.clear(node);
        this.solutionValue = this.solutionBw.cardinality();
        this.numberAware = this.solutionValue;
        instance.setState(node, 0);
    }

    public boolean isIn(int node) {
        return this.solutionBw.get(node);
    }

    public LinkedList<Integer> nodesNotInSolution() {
        LinkedList<Integer> ll = new LinkedList<>();
        for (int j : instance.getNodes()) if (!this.isIn(j)) ll.add(j);
        return ll;
    }

    public BitSet getBitwiseRepresentation() {
        return this.solutionBw;
    }

    /**
     * Removes redundant seeds: a seed with at least half of its neighbours being spreaders
     * is removed if the solution remains feasible without it.
     */
    public void removeUnneeded(SpreadingProcessOptimize eval, Instant startTime, long timeLimitMs) {
        // Snapshot so that removals made inside the loop do not change the nodes visited.
        BitSet snapshot = (BitSet) this.solutionBw.clone();
        for (int index = snapshot.nextSetBit(0); index >= 0; index = snapshot.nextSetBit(index + 1)) {
            if (Duration.between(startTime, Instant.now()).toMillis() > timeLimitMs) {
                return;
            }
            if (this.getSpreaderNeighs(index) >= instance.graph.get(index).size() * 0.5 && this.isIn(index)) {
                this.removeNode(index);
                instance.resetState(this);
                if (!eval.isSolution(this)) {
                    // The node was needed to trigger the cascade of other nodes: restore it.
                    this.addNodeUnique(index);
                    instance.resetState(this);
                    eval.isSolution(this); // refresh the aware counters and states of the restored solution
                } else {
                    long elapsedMs = Duration.between(startTime, Instant.now()).toMillis();
                    System.out.println("Solution.removeUnneeded: node " + index + " removed, solutionValue now " + this.solutionValue + " at " + elapsedMs + " ms");
                }
            }
        }
    }

    /** Number of spreader neighbours of a node, as computed by the last call to isSolution(). */
    public int getAwareNeighs(int node) {
        return awareNeighs[node];
    }

    public int getSpreaderNeighs(int node) {
        int numberOf = 0;
        for (int i : instance.graph.get(node)) {
            if (instance.getNodeState(i) == 2) numberOf++;
        }
        return numberOf;
    }

    public void setAwareCount(int[] aware) {
        this.awareNeighs = aware;
    }

    public void setAware(int nAware) {
        this.numberAware = nAware;
    }

    public int getAware() {
        return this.numberAware;
    }
}
