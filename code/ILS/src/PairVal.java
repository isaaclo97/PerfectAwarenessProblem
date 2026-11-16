/**
 * A node together with its score (used by the priority queue of the greedy constructive).
 */
class PairVal {
    final int node;
    final double val;

    PairVal(int node, double val) {
        this.node = node;
        this.val = val;
    }
}
