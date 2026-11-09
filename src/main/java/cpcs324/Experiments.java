package cpcs324;

import java.io.PrintStream;

/**
 * Experiments A and B: running time of heap-based Prim versus graph size and edge count.
 *
 * <p>Rules from the assignment:
 * <ul>
 *   <li>Generate <em>one</em> graph per configuration, then run Prim {@value #RUNS} times on it.</li>
 *   <li>Time only Prim: graph generation and printing stay outside the measured block.</li>
 *   <li>Tracing is off during timing ({@code prim.run(start, false)}).</li>
 *   <li>Report each run and the average, in milliseconds.</li>
 * </ul>
 *
 * <p>Tip: the JVM compiles hot code while it runs, so the first run is usually slower.
 * One untimed warm-up run before the five timed ones gives steadier numbers.
 * If you do it, say so in the report's method section.
 *
 * <p><b>Owner:</b> Ahmed.
 */
public final class Experiments {

    /** Timed runs per configuration. */
    public static final int RUNS = 5;

    /** Experiment A: {n, m} with m/n = 3 fixed. */
    public static final int[][] EXPERIMENT_A = {
        {500, 1500}, {1000, 3000}, {2000, 6000}, {5000, 15000}
    };

    /** Experiment B: {n, m} with n = 1000 fixed. */
    public static final int[][] EXPERIMENT_B = {
        {1000, 2000}, {1000, 5000}, {1000, 10000}, {1000, 20000}
    };

    private Experiments() {
        // static utility class
    }

    /**
     * Times Prim {@value #RUNS} times on the same graph.
     *
     * @param graph a connected graph
     * @return the running time of each run in milliseconds, length {@value #RUNS}
     */
    public static double[] timePrim(Graph graph) {
        throw new UnsupportedOperationException("TODO(Ahmed): Experiments.timePrim");
    }

    /**
     * Runs Experiment A and Experiment B and prints one table per experiment:
     * n, m, the five run times, and the average (ms).
     *
     * @param out where to print the tables
     */
    public static void runAll(PrintStream out) {
        throw new UnsupportedOperationException("TODO(Ahmed): Experiments.runAll");
    }
}
