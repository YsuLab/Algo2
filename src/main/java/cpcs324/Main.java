package cpcs324;

import java.io.PrintStream;

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
     * @return process exit code: 0 on success, non-zero if the input is missing or invalid
     */
    static int run(String[] args, PrintStream out) {
        // TODO(Yasir):
        //  1. Parse the flags above.
        //  2. Graph.readGraphFromFile(INPUT_FILE); report IOException / malformed input and return 1.
        //  3. Print "Graph Statistics" with the four numbers.
        //  4. graph.validate(): print "Graph Validation: Successful", or the problems and return 1.
        //  5. new PrimMST(graph).run(graph.indexOf("B1"), true); print the trace.
        //  6. Print "MST Summary" and the n - 1 check.
        //  7. Unless --skip-experiments: Experiments.runAll(out).
        throw new UnsupportedOperationException("TODO(Yasir): Main.run");
    }
}
