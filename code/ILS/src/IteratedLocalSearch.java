import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Iterated Local Search for the Perfect Awareness Problem.
 *
 * 1. Greedy constructive phase followed by local search.
 * 2. Until the time limit: perturbation (random removal of seeds and random repair) and
 *    local search, accepting the new solution if it is not worse than the current one.
 * 3. After {@link #MAX_ITERATIONS_WITHOUT_IMPROVEMENT} iterations without improvement,
 *    the search restarts from a new greedy solution.
 */
public class IteratedLocalSearch {
    private static final long SEED = 1111;
    private static final int MAX_ITERATIONS_WITHOUT_IMPROVEMENT = 20;
    // On very large graphs the feasibility of the greedy solution is only checked every few insertions
    private static final int LARGE_GRAPH_NODES = 800000;
    private static final int LARGE_GRAPH_CHECK_EVERY = 5;

    private final Instance instance;
    private final SpreadingProcessOptimize eval;
    private final long timeLimitMs;
    private final Random random;
    private final ArrayList<Integer> nodesNotInSolution;
    private long timeToBestMs = -1;

    public IteratedLocalSearch(Instance instance, SpreadingProcessOptimize eval, long timeLimitMs) {
        this.instance = instance;
        this.eval = eval;
        this.timeLimitMs = timeLimitMs;
        this.random = new Random(SEED);
        this.nodesNotInSolution = new ArrayList<>(instance.getNumberNodes());
    }

    /** Elapsed time (ms) from the start of the search to the best solution found. */
    public long getTimeToBestMs() {
        return timeToBestMs;
    }

    private boolean timeExceeded(Instant start) {
        return Duration.between(start, Instant.now()).toMillis() > timeLimitMs;
    }

    /**
     * Greedy constructive phase: at each step adds the node that maximises the number of
     * currently ignorant nodes that would become aware (see {@link #scoreForNode}).
     * Scores are updated lazily (CELF): a node is re-scored only when it reaches the top of
     * the priority queue with an outdated score.
     */
    public Solution greedyConstructivePhase(Instant startTime) {
        Solution solution = new Solution();
        instance.resetState(solution);
        boolean feasible = eval.isSolution(solution); // initialise aware counters and node states
        PriorityQueue<PairVal> celfHeap = null;
        int[] celfStamp = null;
        int celfGeneration = 0;

        while (!feasible) {
            if (timeExceeded(startTime)) {
                break;
            }
            System.out.println("ILS: Greedy constructive: " + solution.solutionValue() + " " + solution.getAware());
            if (celfHeap == null) {
                celfHeap = new PriorityQueue<>((a, b) -> Double.compare(b.val, a.val));
                celfStamp = new int[instance.getNumberNodes()];
                java.util.Arrays.fill(celfStamp, -1);
                for (int node : instance.getNodes()) {
                    if (solution.isIn(node)) continue;
                    celfHeap.add(new PairVal(node, scoreForNode(node, solution)));
                    celfStamp[node] = celfGeneration;
                }
            }

            int bestNode = -1;
            ArrayList<PairVal> parked = new ArrayList<>(); // up-to-date entries with zero gain, reinserted after this step
            while (!celfHeap.isEmpty()) {
                PairVal top = celfHeap.poll();
                if (solution.isIn(top.node)) continue; // outdated duplicate
                if (celfStamp[top.node] == celfGeneration) {
                    if (top.val <= 0) {
                        parked.add(top);
                        continue;
                    }
                    bestNode = top.node; // up-to-date score with positive gain: best remaining candidate
                    break;
                }
                int freshScore = scoreForNode(top.node, solution);
                celfStamp[top.node] = celfGeneration;
                celfHeap.add(new PairVal(top.node, freshScore));
            }
            celfHeap.addAll(parked);

            if (bestNode == -1) break; // should never happen on a connected graph
            solution.addNode(bestNode);
            celfGeneration++;
            if (instance.getNumberNodes() > LARGE_GRAPH_NODES && solution.solutionValue() % LARGE_GRAPH_CHECK_EVERY == 0) {
                instance.resetState(solution);
                feasible = eval.isSolution(solution);
            } else if (instance.getNumberNodes() < LARGE_GRAPH_NODES) {
                instance.resetState(solution);
                feasible = eval.isSolution(solution);
            }
        }

        System.out.println("ILS: Greedy constructive completed with value: " + solution.solutionValue());
        return solution;
    }

    /**
     * Estimates the number of currently ignorant nodes that would become aware if
     * {@code node} were added as a seed.
     *
     * First order:  the node itself (if ignorant) and every ignorant neighbour.
     * Second order: for each neighbour that is not a spreader yet, if adding {@code node}
     *               makes it reach the spreader threshold (half of its neighbours), its
     *               ignorant neighbours are also counted.
     *
     * Requires {@code eval.isSolution(solution)} to have been called beforehand, so that
     * the aware counters and the node states are up to date.
     */
    private int scoreForNode(int node, Solution solution) {
        if (instance.getNodeState(node) == 2) return 0;
        int score = 0;

        // The node itself becomes aware when it is seeded
        if (instance.getNodeState(node) == 0) score++;

        for (int neigh : instance.graph.get(node)) {
            // First order: every ignorant neighbour becomes aware
            if (instance.getNodeState(neigh) == 0) score++;

            // Second order: if neigh reaches the spreader threshold, its ignorant neighbours become aware
            if (!solution.isIn(neigh) && instance.getNodeState(neigh) != 2) {
                int spreaderNeighs = solution.getAwareNeighs(neigh);
                if (spreaderNeighs + 1 >= instance.graph.get(neigh).size() * 0.5) {
                    for (int nn : instance.graph.get(neigh)) {
                        if (instance.getNodeState(nn) == 0) score++;
                    }
                }
            }
        }

        return score;
    }

    /** Removes redundant seeds and applies the swap-based local search. */
    public Solution localSearch(Solution solution, Instant startTime) {
        solution.removeUnneeded(eval, startTime, timeLimitMs);
        Solution improvedSol = solution;

        if (improvedSol.solutionValue() >= 2) {
            LocalSearch ls = new LocalSearch(improvedSol, eval, startTime, timeLimitMs, random);
            improvedSol = ls.bestSolutionFound;
            improvedSol.removeUnneeded(eval, startTime, timeLimitMs);
        }
        return improvedSol;
    }

    /**
     * Perturbation: removes {@code nodesToRemove} random seeds and repairs the solution by
     * adding random nodes until it is feasible again.
     */
    public Solution perturbation(Solution solution, int nodesToRemove) {
        Instant perturbationStart = Instant.now();
        Solution perturbedSol = new Solution(solution.getBitwiseRepresentation());

        ArrayList<Integer> nodesInSolution = new ArrayList<>();
        for (int i = 0; i < instance.getNumberNodes(); i++) {
            if (perturbedSol.isIn(i)) {
                nodesInSolution.add(i);
            }
        }
        if (nodesInSolution.isEmpty()) {
            return perturbedSol;
        }

        destructRandom(nodesToRemove, nodesInSolution, perturbedSol);
        instance.resetState(perturbedSol);

        while (!eval.isSolution(perturbedSol)) {
            if (timeExceeded(perturbationStart)) {
                return solution;
            }
            prepareNodesNotInSolution(perturbedSol);
            addRandomNode(perturbedSol);
        }
        return perturbedSol;
    }

    /** Fills {@link #nodesNotInSolution} with the non-seed nodes in random order. */
    private void prepareNodesNotInSolution(Solution solution) {
        nodesNotInSolution.clear();
        for (int i : instance.getNodes()) {
            if (!solution.isIn(i)) {
                nodesNotInSolution.add(i);
            }
        }
        if (nodesNotInSolution.isEmpty()) {
            System.out.println("ILS: Warning - no nodes available to add!");
        }
        Collections.shuffle(nodesNotInSolution, random);
    }

    private void addRandomNode(Solution solution) {
        int nodeToAdd = nodesNotInSolution.removeLast();
        solution.addNode(nodeToAdd);
        instance.resetState(solution);
    }

    private void destructRandom(int nodesToRemove, ArrayList<Integer> nodesInSolution, Solution perturbedSol) {
        nodesToRemove %= nodesInSolution.size() + 1;
        for (int i = 0; i < nodesToRemove; i++) {
            int randomIndex = random.nextInt(nodesInSolution.size());
            int nodeToRemove = nodesInSolution.get(randomIndex);
            perturbedSol.removeNode(nodeToRemove);
            nodesInSolution.set(randomIndex, nodesInSolution.get(nodesInSolution.size() - 1));
            nodesInSolution.remove(nodesInSolution.size() - 1);
        }
    }

    /** Runs the ILS until the time limit and returns the best solution found. */
    public Solution run() {
        Instant startTime = Instant.now();
        Solution bestSolution = new Solution();

        if (instance.graph.isEmpty()) {
            // The whole graph collapsed into a single node during preprocessing
            bestSolution.addNodeUnique(0);
            timeToBestMs = Duration.between(startTime, Instant.now()).toMillis();
            return bestSolution;
        }

        // Initial solution: greedy constructive + local search
        Solution currentSolution = greedyConstructivePhase(startTime);
        int constructiveValue = currentSolution.solutionValue();
        currentSolution = localSearch(currentSolution, startTime);
        int bestValue = currentSolution.solutionValue();
        bestSolution = new Solution(currentSolution.getBitwiseRepresentation());
        timeToBestMs = Duration.between(startTime, Instant.now()).toMillis();
        System.out.println(String.format("ILS: Initial solution - Constructive: %d, after local search: %d, at %d ms",
                constructiveValue, bestValue, timeToBestMs));

        int iteration = 0;
        int iterationsWithoutImprovement = 0;

        while (Duration.between(startTime, Instant.now()).toMillis() < timeLimitMs) {
            iteration++;
            if (bestValue == 1) break;
            Solution perturbedSolution = perturbation(currentSolution, iteration);
            Solution localOptimum = localSearch(perturbedSolution, startTime);

            // Acceptance criterion: accept if better or equal
            if (localOptimum.solutionValue() <= currentSolution.solutionValue()) {
                currentSolution = localOptimum;
                iterationsWithoutImprovement = 0;
                if (localOptimum.solutionValue() < bestValue) {
                    bestSolution = new Solution(localOptimum.getBitwiseRepresentation());
                    bestValue = bestSolution.solutionValue();
                    timeToBestMs = Duration.between(startTime, Instant.now()).toMillis();
                    System.out.println("ILS: New best solution at iteration " + iteration + " with value " + bestValue + " (" + timeToBestMs + " ms)");
                    if (bestValue == 1) break;
                }
            } else {
                iterationsWithoutImprovement++;
            }

            // Restart from a new greedy solution after too many iterations without improvement
            if (iterationsWithoutImprovement >= MAX_ITERATIONS_WITHOUT_IMPROVEMENT) {
                currentSolution = greedyConstructivePhase(startTime);
                currentSolution = localSearch(currentSolution, startTime);
                iterationsWithoutImprovement = 0;
                if (currentSolution.solutionValue() < bestValue) {
                    bestSolution = new Solution(currentSolution.getBitwiseRepresentation());
                    bestValue = bestSolution.solutionValue();
                    timeToBestMs = Duration.between(startTime, Instant.now()).toMillis();
                    System.out.println("ILS: New best solution at iteration " + iteration + " with value " + bestValue + " (" + timeToBestMs + " ms)");
                    if (bestValue == 1) break;
                }
            }
        }
        System.out.println(String.format("ILS: Completed %d iterations in %d ms, best value: %d",
                iteration, Duration.between(startTime, Instant.now()).toMillis(), bestValue));
        return bestSolution;
    }
}
