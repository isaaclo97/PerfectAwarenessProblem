import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Graph instance of the Perfect Awareness Problem.
 *
 * Input format:
 * <pre>
 * seed
 * m0
 * number of nodes
 * number of edges
 * u v        (one edge per line)
 * </pre>
 *
 * The graph is preprocessed when it is read (see {@link #preprocessing}), so node ids
 * used by the algorithm refer to the reduced graph.
 */
public class Instance {
    public final String name;
    private int numberNodes;
    private int[] state; // 0: ignorant, 1: aware, 2: spreader
    HashMap<Integer, ArrayList<Integer>> graph = new HashMap<>();

    static class UnionFind {
        int[] parent, rank;

        UnionFind(int n) {
            parent = new int[n];
            rank = new int[n];
            for (int i = 0; i < n; i++) parent[i] = i;
        }

        int find(int x) {
            if (parent[x] != x) parent[x] = find(parent[x]);
            return parent[x];
        }

        void union(int a, int b) {
            int ra = find(a), rb = find(b);
            if (ra == rb) return;
            if (rank[ra] < rank[rb]) parent[ra] = rb;
            else if (rank[ra] > rank[rb]) parent[rb] = ra;
            else { parent[rb] = ra; rank[ra]++; }
        }
    }

    /**
     * Merges adjacent nodes u, v when ceil(0.5 * degree(u)) = ceil(0.5 * degree(v)) = 1,
     * i.e., degree(u) <= 2 and degree(v) <= 2.
     *
     * @param edges list of edges (each int[]{u, v}) with arbitrary integer node labels
     * @return reduced edge list (nodes relabeled 0..k-1)
     */
    public static ArrayList<int[]> preprocessing(ArrayList<int[]> edges) {
        if (edges.isEmpty()) return new ArrayList<>();

        // Step A: map arbitrary node labels to compact indices
        HashMap<Integer, Integer> labelToIndex = new HashMap<>();
        ArrayList<Integer> indexToLabel = new ArrayList<>();
        for (int[] e : edges) {
            int u = e[0], v = e[1];
            if (!labelToIndex.containsKey(u)) {
                labelToIndex.put(u, indexToLabel.size());
                indexToLabel.add(u);
            }
            if (!labelToIndex.containsKey(v)) {
                labelToIndex.put(v, indexToLabel.size());
                indexToLabel.add(v);
            }
        }

        int n = indexToLabel.size();
        int[] degree = new int[n];

        // Step B: compute node degrees (self-loops are ignored)
        for (int[] e : edges) {
            int iu = labelToIndex.get(e[0]);
            int iv = labelToIndex.get(e[1]);
            if (iu == iv) continue;
            degree[iu]++;
            degree[iv]++;
        }

        // Step C: merge u and v if both degrees are <= 2
        UnionFind uf = new UnionFind(n);
        for (int[] e : edges) {
            int iu = labelToIndex.get(e[0]);
            int iv = labelToIndex.get(e[1]);
            if (iu == iv) continue;
            if (degree[iu] <= 2 && degree[iv] <= 2) {
                uf.union(iu, iv);
            }
        }

        // Step D: assign new ids only to nodes that appear in non-self-loop edges
        HashMap<Integer, Integer> repToNew = new HashMap<>();
        int nextId = 0;
        HashSet<Long> seen = new HashSet<>();
        ArrayList<int[]> newEdges = new ArrayList<>();

        for (int[] e : edges) {
            int iu = labelToIndex.get(e[0]);
            int iv = labelToIndex.get(e[1]);
            int repU = uf.find(iu);
            int repV = uf.find(iv);
            if (repU == repV) continue; // self-loop after merging

            if (!repToNew.containsKey(repU)) repToNew.put(repU, nextId++);
            if (!repToNew.containsKey(repV)) repToNew.put(repV, nextId++);

            int a = repToNew.get(repU);
            int b = repToNew.get(repV);
            int min = Math.min(a, b), max = Math.max(a, b);
            long key = (((long) min) << 32) | (max & 0xffffffffL);
            if (seen.add(key)) {
                newEdges.add(new int[]{min, max});
            }
        }

        return newEdges;
    }

    public Instance(File file) {
        this.name = file.getName();
        try (Scanner reader = new Scanner(file)) {
            reader.nextLine(); // seed
            reader.nextLine(); // m0
            reader.nextLine(); // number of nodes
            int givenNumberEdges = Integer.parseInt(reader.nextLine().trim());
            ArrayList<int[]> edgeList = new ArrayList<>();
            for (int i = 0; i < givenNumberEdges; i++) {
                String[] sEdge = reader.nextLine().split(" ");
                edgeList.add(new int[]{Integer.parseInt(sEdge[0]), Integer.parseInt(sEdge[1])});
            }

            for (int[] edge : preprocessing(edgeList)) {
                if (edge[0] != edge[1]) {
                    ArrayList<Integer> edgeListStart = graph.getOrDefault(edge[0], new ArrayList<>());
                    ArrayList<Integer> edgeListEnd = graph.getOrDefault(edge[1], new ArrayList<>());
                    if (!edgeListStart.contains(edge[1])) edgeListStart.add(edge[1]);
                    if (!edgeListEnd.contains(edge[0])) edgeListEnd.add(edge[0]);
                    graph.put(edge[0], edgeListStart);
                    graph.put(edge[1], edgeListEnd);
                }
            }
            numberNodes = graph.size();
            state = new int[numberNodes];
        } catch (Exception e) {
            throw new RuntimeException("Error reading instance " + file.getPath() + ": " + e.getMessage(), e);
        }
    }

    public int getNumberNodes() {
        return numberNodes;
    }

    /** Nodes of the (reduced) graph, sorted to guarantee a deterministic order. */
    public ArrayList<Integer> getNodes() {
        ArrayList<Integer> nodes = new ArrayList<>(this.graph.keySet());
        Collections.sort(nodes);
        return nodes;
    }

    /** Updates the state of a node; a node never goes back from spreader to aware. */
    public void setState(int node, int newState) {
        switch (newState) {
            case 0:
                this.state[node] = 0;
                break;
            case 1:
                if (this.state[node] <= 1) {
                    this.state[node] = 1;
                    break;
                }
            case 2:
                if (this.state[node] <= 2) {
                    this.state[node] = 2;
                    break;
                }
        }
    }

    /** Seeds become spreaders and every other node becomes ignorant. */
    public void resetState(Solution sol) {
        for (int node = 0; node < numberNodes; node++) {
            this.state[node] = sol.isIn(node) ? 2 : 0;
        }
    }

    public int getNodeState(int node) {
        return this.state[node];
    }
}
