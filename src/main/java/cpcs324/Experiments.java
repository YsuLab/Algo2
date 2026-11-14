package cpcs324;

import java.io.PrintStream;
import java.util.Locale;

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
        int expectedEdges = graph.getVertexCount() - 1;
        PrimMST prim = new PrimMST(graph);
        // PrimMST.run resets its state on every call.
        prim.run(0, false);
        long expectedCost = checkedCost(prim, expectedEdges);

        double[] times = new double[RUNS];
        for (int i = 0; i < times.length; i++) {
            long start = System.nanoTime();
            prim.run(0, false);
            long elapsed = System.nanoTime() - start;

            long cost = checkedCost(prim, expectedEdges);
            if (cost != expectedCost) {
                throw new IllegalStateException("Prim run " + (i + 1) + " returned cost "
                    + cost + "; expected " + expectedCost);
            }
            times[i] = elapsed / 1_000_000.0;
        }
        return times;
    }

    private static long checkedCost(PrimMST prim, int expectedEdges) {
        int actualEdges = prim.getMstEdges().size();
        if (actualEdges != expectedEdges) {
            throw new IllegalStateException("Prim returned " + actualEdges
                + " MST edges; expected " + expectedEdges);
        }
        return prim.getTotalCost();
    }

    /**
     * Runs Experiment A and Experiment B and prints one table per experiment:
     * n, m, the five run times, and the average (ms).
     *
     * @param out where to print the tables
     */
    public static void runAll(PrintStream out) {
        printExperiment(out, "Experiment A", EXPERIMENT_A);
        out.println();
        printExperiment(out, "Experiment B", EXPERIMENT_B);
    }

    private static void printExperiment(PrintStream out, String title, int[][] configurations) {
        out.println(title + " (ms)");
        out.printf(Locale.ROOT, "%6s %6s", "n", "m");
        for (int i = 1; i <= RUNS; i++) {
            out.printf(Locale.ROOT, " %10s", "Run " + i);
        }
        out.printf(Locale.ROOT, " %10s%n", "Average");

        for (int[] configuration : configurations) {
            int n = configuration[0];
            int m = configuration[1];
            Graph graph = RandomGraphGenerator.generateRandomGraph(n, m);
            double[] times = timePrim(graph);

            out.printf(Locale.ROOT, "%6d %6d", n, m);
            double total = 0;
            for (double time : times) {
                out.printf(Locale.ROOT, " %10.3f", time);
                total += time;
            }
            out.printf(Locale.ROOT, " %10.3f%n", total / times.length);
        }
    }
}
