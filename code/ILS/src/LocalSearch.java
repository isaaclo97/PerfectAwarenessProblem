import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Local search based on (2,1)-swaps: two seeds are removed and one non-seed node is added.
 * It follows a first-improvement strategy.
 *
 * When there are many candidate nodes, only high-degree nodes are explored: the nodes are
 * sorted by degree and, for graphs with at most 900 nodes, {@link #STEPS} of the
 * 3 * {@link #STEPS} highest-degree nodes are sampled with the weighted random sampling with
 * reservoir technique (Efraimidis and Spirakis), with weight degree^4. For larger graphs the
 * {@link #STEPS} highest-degree nodes are used.
 */
public class LocalSearch {
    private static final int STEPS = 80;
    private static final int MAX_NODES_FOR_SAMPLING = 900;
    private static final int DEGREE_EXPONENT = 4;

    Solution bestSolutionFound;

    private static class WeightedKey {
        final int node;
        final double key;

        WeightedKey(int node, double key) {
            this.node = node;
            this.key = key;
        }
    }

    public LocalSearch(Solution solution, SpreadingProcessOptimize e, Instant startTime, long timeLimitMs, Random random) {
        this.bestSolutionFound = solution;
        boolean improved = true;

        ArrayList<Integer> nodesInSolution = new ArrayList<>();
        int n = Solution.instance.getNumberNodes();
        while (improved) {
            solution = this.bestSolutionFound;
            improved = false;
            LinkedList<Integer> nodesNotInSolution = this.bestSolutionFound.nodesNotInSolution();
            if (nodesNotInSolution.isEmpty()) {
                break;
            }
            ArrayList<Integer> candidatesOut = new ArrayList<>(nodesNotInSolution);

            if (nodesNotInSolution.size() > STEPS) {
                // Sort the candidates by degree (descending)
                candidatesOut.sort((a, b) -> {
                    int degA = Solution.instance.graph.get(a).size();
                    int degB = Solution.instance.graph.get(b).size();
                    return Integer.compare(degB, degA);
                });
                if (n <= MAX_NODES_FOR_SAMPLING) {
                    // Weighted random sampling with reservoir over the 3 * STEPS highest-degree nodes
                    int topK = Math.min(3 * STEPS, candidatesOut.size());
                    List<Integer> top = new ArrayList<>(candidatesOut.subList(0, topK));

                    // Random key u^(1/w) for each node, where w = degree^4
                    List<WeightedKey> keys = new ArrayList<>(top.size());
                    for (int node : top) {
                        int degree = Solution.instance.graph.get(node).size();
                        double weight = Math.pow(degree, DEGREE_EXPONENT);
                        double u = random.nextDouble();
                        double key = Math.pow(u, 1.0 / weight);
                        keys.add(new WeightedKey(node, key));
                    }

                    // Keep the STEPS nodes with the largest keys
                    keys.sort((a, b) -> Double.compare(b.key, a.key));
                    ArrayList<Integer> sampled = new ArrayList<>(STEPS);
                    for (int i = 0; i < Math.min(STEPS, keys.size()); i++) {
                        sampled.add(keys.get(i).node);
                    }
                    candidatesOut = sampled;
                } else {
                    candidatesOut = new ArrayList<>(candidatesOut.subList(0, Math.min(STEPS, candidatesOut.size())));
                }
            } else {
                Collections.shuffle(candidatesOut, random);
            }

            nodesInSolution.clear();
            BitSet solutionBits = solution.getBitwiseRepresentation();
            for (int i = 0; i < n; i++) {
                if (solutionBits.get(i)) {
                    nodesInSolution.add(i);
                }
            }
            Collections.shuffle(nodesInSolution, random);

            for (Integer node : candidatesOut) {
                if (improved) break;
                if (timeExceeded(startTime, timeLimitMs)) return;

                for (int i = 0; i < nodesInSolution.size() && !improved; i++) {
                    int indexI = nodesInSolution.get(i);
                    for (int j = i + 1; j < nodesInSolution.size() && !improved; j++) {
                        int indexJ = nodesInSolution.get(j);

                        // Swap: remove indexI and indexJ, add node
                        BitSet bwSol = (BitSet) solutionBits.clone();
                        bwSol.clear(indexI);
                        bwSol.clear(indexJ);
                        bwSol.set(node);

                        Solution finalSol = new Solution(bwSol);
                        Solution.instance.resetState(finalSol);
                        if (e.isSolution(finalSol) && finalSol.solutionValue() < this.bestSolutionFound.solutionValue()) {
                            improved = true;
                            int improvement = this.bestSolutionFound.solutionValue() - finalSol.solutionValue();
                            long elapsedMs = Duration.between(startTime, Instant.now()).toMillis();
                            System.out.println("LocalSearch: Improvement found! " + this.bestSolutionFound.solutionValue() + " -> " + finalSol.solutionValue() + " (gain: " + improvement + ") at " + elapsedMs + " ms");
                            this.bestSolutionFound = finalSol;
                            nodesInSolution.remove((Integer) indexI);
                            nodesInSolution.remove((Integer) indexJ);
                            nodesInSolution.add(node);
                        }
                        if (timeExceeded(startTime, timeLimitMs)) return;
                    }
                }
            }
        }
    }

    private static boolean timeExceeded(Instant startTime, long timeLimitMs) {
        return Duration.between(startTime, Instant.now()).toMillis() > timeLimitMs;
    }
}
