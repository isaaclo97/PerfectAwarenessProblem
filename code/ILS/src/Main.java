import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Iterated Local Search for the Perfect Awareness Problem.
 *
 * Usage:
 * <pre>
 * java -jar PerfectAwarenessProblem.jar &lt;instance.in | instances_folder&gt; &lt;time_limit_seconds&gt; [output_folder]
 * </pre>
 *
 * For each instance, the best solution is written to {@code <output_folder>/ils_<instance>.txt}:
 * <pre>
 * instance name
 * objective value (number of seed nodes)
 * (seed node ids)
 * time in ms
 * </pre>
 * and a line is appended to {@code <output_folder>/summary.csv}. Seed node ids refer to the
 * graph after preprocessing (see {@link Instance#preprocessing}).
 */
public class Main {
    private static final String DEFAULT_OUTPUT_FOLDER = "solutions";

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java -jar PerfectAwarenessProblem.jar <instance.in | instances_folder> <time_limit_seconds> [output_folder]");
            System.err.println("  instance.in | instances_folder: a single instance file or a folder with .in files");
            System.err.println("  time_limit_seconds: time limit per instance, in seconds (e.g., 300 or 3600)");
            System.err.println("  output_folder: folder for the solution files (default: " + DEFAULT_OUTPUT_FOLDER + ")");
            System.exit(1);
        }

        File input = new File(args[0]);
        long timeLimitMs = Math.round(Double.parseDouble(args[1]) * 1000);
        File outputFolder = new File(args.length > 2 ? args[2] : DEFAULT_OUTPUT_FOLDER);

        List<File> instances = listInstances(input);
        if (instances.isEmpty()) {
            System.err.println("No .in instance files found in " + input.getPath());
            System.exit(1);
        }
        if (!outputFolder.exists() && !outputFolder.mkdirs()) {
            System.err.println("Could not create output folder " + outputFolder.getPath());
            System.exit(1);
        }

        System.out.println(String.format("Running ILS on %d instance(s), time limit %d s, output folder %s",
                instances.size(), timeLimitMs / 1000, outputFolder.getPath()));
        for (File instanceFile : instances) {
            solveInstance(instanceFile, timeLimitMs, outputFolder);
        }
    }

    private static List<File> listInstances(File input) {
        List<File> instances = new ArrayList<>();
        if (input.isFile()) {
            instances.add(input);
        } else if (input.isDirectory()) {
            File[] files = input.listFiles((dir, name) -> name.endsWith(".in"));
            if (files != null) {
                Arrays.sort(files, Comparator.comparing(File::getName));
                instances.addAll(Arrays.asList(files));
            }
        }
        return instances;
    }

    private static void solveInstance(File instanceFile, long timeLimitMs, File outputFolder) throws IOException {
        String instanceName = instanceFile.getName().replaceFirst("\\.in$", "");
        System.out.println(String.format("------------------------ INSTANCE %s ------------------------", instanceName));

        Instance instance = new Instance(instanceFile);
        Solution.instance = instance;
        Instant initTime = Instant.now();
        SpreadingProcessOptimize eval = new SpreadingProcessOptimize(instance);
        IteratedLocalSearch ils = new IteratedLocalSearch(instance, eval, timeLimitMs);
        Solution best = ils.run();
        long executionTime = Duration.between(initTime, Instant.now()).toMillis();

        // Independent feasibility check of the final solution
        ArrayList<Integer> seeds = new ArrayList<>();
        for (int i = 0; i < instance.getNumberNodes(); i++) {
            if (best.isIn(i)) seeds.add(i);
        }
        boolean feasible = instance.graph.isEmpty() || new SpreadingProcessCheck(instance).isSolution(seeds);
        if (!feasible) {
            System.err.println("WARNING: the solution for " + instanceName + " is infeasible!");
        }

        File solutionFile = new File(outputFolder, "ils_" + instanceName + ".txt");
        try (PrintWriter writer = new PrintWriter(solutionFile, "UTF-8")) {
            writer.println(instanceName);
            writer.println(best.solutionValue());
            writer.println(best);
            writer.println(executionTime);
        }

        File summary = new File(outputFolder, "summary.csv");
        boolean writeHeader = !summary.exists();
        try (PrintWriter writer = new PrintWriter(new FileWriter(summary, true))) {
            if (writeHeader) writer.println("instance,objective,time_ms,time_to_best_ms,feasible");
            writer.println(String.join(",", instanceName, String.valueOf(best.solutionValue()),
                    String.valueOf(executionTime), String.valueOf(ils.getTimeToBestMs()), String.valueOf(feasible)));
        }

        System.out.println(String.format("RESULT %s: objective = %d, time = %d ms, time to best = %d ms, feasible = %s",
                instanceName, best.solutionValue(), executionTime, ils.getTimeToBestMs(), feasible));
        System.out.println("RESULT " + instanceName + ": seeds = " + best);
        System.out.println("RESULT " + instanceName + ": solution written to " + solutionFile.getPath());
    }
}
