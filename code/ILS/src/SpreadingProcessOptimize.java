import java.util.Arrays;
import java.util.BitSet;

/**
 * Efficient evaluation of the spreading process: checks whether a set of seeds makes every
 * node of the graph aware.
 *
 * A node becomes aware as soon as one of its neighbours is a spreader, and becomes a spreader
 * once at least half of its neighbours are spreaders.
 */
public class SpreadingProcessOptimize {

    private final Instance instance;

    // Reusable arrays to avoid allocation overhead
    private int[] spreaderCount;
    private int[] awareCount;
    // Epoch stamps instead of booleans, to avoid an O(n) reset on every call.
    // A flag is "set" iff its entry equals currentEpoch.
    private int currentEpoch = 0;
    private int[] visitedEpoch;
    private int[] isSpreaderTauEpoch;
    private int[] isAwareEpoch;
    private int[] qSpreaders;

    public SpreadingProcessOptimize(Instance i) {
        this.instance = i;
        int n = i.getNumberNodes();
        spreaderCount = new int[n];
        awareCount = new int[n];
        visitedEpoch = new int[n];
        isSpreaderTauEpoch = new int[n];
        isAwareEpoch = new int[n];
        qSpreaders = new int[n];
    }

    public boolean isSolution(Solution sol) {
        int n = instance.getNumberNodes();

        currentEpoch++;
        Arrays.fill(spreaderCount, 0, n, 0);
        int qSize = 0;

        int awareSize = 0;
        BitSet initialSeeds = sol.getBitwiseRepresentation();
        for (int index = initialSeeds.nextSetBit(0); index >= 0; index = initialSeeds.nextSetBit(index + 1)) {
            isSpreaderTauEpoch[index] = currentEpoch;
            isAwareEpoch[index] = currentEpoch;
            awareSize++;
            qSpreaders[qSize++] = index;
        }
        awareCount = spreaderCount;

        int qHead = 0;
        while (qHead < qSize && awareSize != n) {
            int node = qSpreaders[qHead++];
            if (visitedEpoch[node] != currentEpoch) {
                visitedEpoch[node] = currentEpoch;
                for (int neigh : instance.graph.get(node)) {
                    // A neighbour of a spreader becomes aware
                    if (isAwareEpoch[neigh] != currentEpoch) {
                        isAwareEpoch[neigh] = currentEpoch;
                        awareSize++;
                        this.instance.setState(neigh, 1);
                        if (awareSize == n) break;
                    }
                    awareCount[node]++;
                    // One more spreader around neigh
                    spreaderCount[neigh]++;
                    if (isSpreaderTauEpoch[neigh] != currentEpoch && spreaderCount[neigh] >= instance.graph.get(neigh).size() * 0.5) {
                        isSpreaderTauEpoch[neigh] = currentEpoch;
                        qSpreaders[qSize++] = neigh;
                        this.instance.setState(neigh, 2);
                    }
                }
            }
        }

        sol.setAware(awareSize);
        // Live view of the reusable buffer (no copy, since this method is called very often).
        // It is valid until the next call, which is enough: callers read it right after evaluating sol.
        sol.setAwareCount(awareCount);

        return awareSize == n;
    }
}
