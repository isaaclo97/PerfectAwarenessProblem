import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Straightforward (non-optimized) evaluation of the spreading process, used to double-check
 * the feasibility of the final solution.
 */
public class SpreadingProcessCheck {

    private final Instance instance;

    public SpreadingProcessCheck(Instance i) {
        this.instance = i;
    }

    public boolean isSolution(ArrayList<Integer> sol) {
        int n = instance.getNumberNodes();
        Queue<Integer> qSpreaders = new LinkedList<>();
        boolean[] spreaders = new boolean[n];
        boolean[] aware = new boolean[n];
        boolean[] visited = new boolean[n];
        int[] spreaderCount = new int[n]; // number of spreader neighbours of each node

        int awareSize = 0;
        for (Integer i : sol) {
            qSpreaders.add(i);
            spreaders[i] = true;
            aware[i] = true;
            awareSize++;
        }
        while (!qSpreaders.isEmpty() && awareSize != n) {
            int node = qSpreaders.poll();
            if (!visited[node]) {
                visited[node] = true;
                for (Integer neigh : instance.graph.get(node)) {
                    // A neighbour of a spreader becomes aware
                    if (!aware[neigh]) {
                        aware[neigh] = true;
                        awareSize++;
                    }
                    // One more spreader around neigh
                    spreaderCount[neigh]++;
                    if (!spreaders[neigh] && spreaderCount[neigh] >= instance.graph.get(neigh).size() * 0.5) {
                        spreaders[neigh] = true;
                        qSpreaders.add(neigh);
                    }
                }
            }
        }
        return awareSize == n;
    }
}
