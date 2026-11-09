package cpcs324;

import java.util.List;

/**
 * Prim's algorithm with a min-priority queue ({@link java.util.PriorityQueue}).
 *
 * <p>Rules from the assignment:
 * <ul>
 *   <li>Write the algorithm yourselves. No MST library and no copied implementation.</li>
 *   <li>At every step, pick the minimum-weight edge from a visited vertex to an unvisited one.</li>
 *   <li>Record a step-by-step trace for the campus run; turn tracing off for experiments,
 *       because building the "Visited" string is O(n) per step and would distort the timing.</li>
 * </ul>
 *
 * <p>Each call to {@link #run(int, boolean)} must start from a clean state, so the
 * experiments can call it five times on the same graph.
 *
 * <p><b>Owner:</b> Yasir.
 */
public class PrimMST {

    // TODO(Yasir): fields: the graph, visited[], the selected edges, total cost, trace lines.

    /**
     * @param graph a connected, validated graph (see {@link Graph#validate()})
     */
    public PrimMST(Graph graph) {
        throw new UnsupportedOperationException("TODO(Yasir): PrimMST constructor");
    }

    /**
     * Builds the MST starting from {@code startVertex}, replacing any previous result.
     *
     * @param startVertex  index of the start vertex (0 = B1)
     * @param recordTrace  {@code true} to record {@link #getTrace()} lines; {@code false} for timing runs
     */
    public void run(int startVertex, boolean recordTrace) {
        throw new UnsupportedOperationException("TODO(Yasir): PrimMST.run");
    }

    /** @return the selected MST edges, in the order they were selected */
    public List<Edge> getMstEdges() {
        throw new UnsupportedOperationException("TODO(Yasir): PrimMST.getMstEdges");
    }

    /** @return total weight of the selected edges (13 for the campus graph) */
    public long getTotalCost() {
        throw new UnsupportedOperationException("TODO(Yasir): PrimMST.getTotalCost");
    }

    /**
     * Trace lines in exactly this format (from the assignment's expected output):
     * <pre>
     * Step 1: B1 - B3 | Weight: 2 | Visited: {B1, B3}
     * </pre>
     * The first label is the already-visited endpoint; the second is the newly visited one.
     *
     * @return one line per selected edge; empty if the last run had {@code recordTrace == false}
     */
    public List<String> getTrace() {
        throw new UnsupportedOperationException("TODO(Yasir): PrimMST.getTrace");
    }
}
