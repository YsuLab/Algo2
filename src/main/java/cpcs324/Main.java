package cpcs324;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 * Entry point. Run from the repository root so {@code campus_graph.txt} is found.
 *
 * <pre>
 * java -jar target/algo2.jar                      full run: campus graph + experiments
 * java -jar target/algo2.jar --skip-experiments   campus graph only (used by the tests)
 * java -jar target/algo2.jar --experiments-only   experiments only
 * </pre>
 *
 * <p><b>Owner:</b> Yasir (integration with Badr's and Ahmed's parts).
 */
public final class Main {

    /** Input file required by the assignment. */
    public static final String INPUT_FILE = "campus_graph.txt";

    /** Prim starts from this building, as the assignment requires. */
    private static final String START_LABEL = "B1";

    private Main() {
    }

    public static void main(String[] args) {
        int exitCode = run(args, System.out);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    /**
     * Runs the program and prints to {@code out}. Kept separate from {@link #main(String[])}
     * so tests can capture the output without starting a new JVM.
     *
     * <p>Output must contain these blocks, as in the assignment's expected output:
     * "Graph Statistics", "Graph Validation: Successful", "Prim's Algorithm (Starting from B1)"
     * with the step trace, and "MST Summary" with the n - 1 verification line.
     * See {@code docs/expected-output.txt}.
     *
     * @param args command-line flags (see class comment)
     * @param out  where to print
     * @return process exit code: 0 on success, 1 if the input is missing or invalid,
     *         2 if a command-line flag is not recognized
     */
    static int run(String[] args, PrintStream out) {
        boolean runCampus = true;
        boolean runExperiments = true;
        for (String arg : args) {
            if (arg.equals("--skip-experiments")) {
                runExperiments = false;
            } else if (arg.equals("--experiments-only")) {
                runCampus = false;
            } else {
                out.println("Unknown option: " + arg);
                out.println("Usage: java -jar algo2.jar [--skip-experiments | --experiments-only]");
                return 2;
            }
        }
        if (!runCampus && !runExperiments) {
            out.println("--skip-experiments and --experiments-only cannot be used together");
            return 2;
        }

        if (runCampus) {
            int exitCode = runCampus(out);
            if (exitCode != 0) {
                return exitCode; // invalid input: do not go on to Prim or the experiments
            }
        }
        if (runExperiments) {
            if (runCampus) {
                out.println();
            }
            Experiments.runAll(out);
        }
        return 0;
    }

    /** Requirements 1 and 2: read and validate the campus graph, then run Prim from B1. */
    private static int runCampus(PrintStream out) {
        Graph graph;
        try {
            graph = Graph.readGraphFromFile(INPUT_FILE);
        } catch (IOException ex) {
            out.println("Error: cannot read " + INPUT_FILE + " (" + ex.getMessage() + ")");
            out.println("Run the program from the folder that contains " + INPUT_FILE + ".");
            return 1;
        } catch (IllegalArgumentException ex) {
            out.println("Error: " + INPUT_FILE + " is malformed. " + ex.getMessage());
            return 1;
        }

        int vertexCount = graph.getVertexCount();
        out.println("Graph Statistics");
        out.println("----------------------------");
        out.println("Number of Vertices: " + vertexCount);
        out.println("Number of Edges: " + graph.getEdgeCount());
        out.println("Number of Connected Components: " + graph.countConnectedComponents());
        out.println("Total Edge Weight: " + graph.getTotalEdgeWeight());
        out.println();

        List<String> problems = graph.validate();
        if (!problems.isEmpty()) {
            out.println("Graph Validation: Failed");
            for (String problem : problems) {
                out.println("- " + problem);
            }
            return 1;
        }
        out.println("Graph Validation: Successful");
        out.println();

        PrimMST prim = new PrimMST(graph);
        prim.run(graph.indexOf(START_LABEL), true);

        out.println("Prim's Algorithm (Starting from " + START_LABEL + ")");
        out.println("------------------------------------------");
        for (String step : prim.getTrace()) {
            out.println(step);
        }
        out.println();

        int mstEdgeCount = prim.getMstEdges().size();
        boolean isSpanningTree = mstEdgeCount == vertexCount - 1;
        out.println("MST Summary");
        out.println("------------------------------------------");
        out.println("Number of MST Edges: " + mstEdgeCount);
        out.println("Total MST Cost: " + prim.getTotalCost());
        out.println("MST Edge Count Verification: "
                + (isSpanningTree ? "Passed (" : "Failed (")
                + mstEdgeCount + (isSpanningTree ? " = " : " != ") + vertexCount + " - 1)");
        return isSpanningTree ? 0 : 1;
    }
}
