package cpcs324;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

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
 * <p>This is the "lazy" version of Prim. The queue holds candidate edges that leave the
 * visited set. An edge can become useless while it waits in the queue (its far end gets
 * visited through a cheaper edge), so every edge taken from the queue is checked again
 * and skipped if it would close a cycle. Each edge enters the queue at most twice, so the
 * running time is O(m log m), which is O(m log n) because m is at most n squared.
 *
 * <p><b>Owner:</b> Yasir.
 */
public class PrimMST {

    /** An edge leaving the visited set: {@code from} is visited, {@code to} was not when queued. */
    private static final class Candidate implements Comparable<Candidate> {
        private final Edge edge;
        private final int from;
        private final int to;

        Candidate(Edge edge, int from) {
            this.edge = edge;
            this.from = from;
            this.to = edge.other(from);
        }

        /** Cheapest first. Ties are broken by vertex index so every run gives the same tree. */
        @Override
        public int compareTo(Candidate other) {
            int byWeight = Integer.compare(edge.getWeight(), other.edge.getWeight());
            if (byWeight != 0) {
                return byWeight;
            }
            int byTarget = Integer.compare(to, other.to);
            return byTarget != 0 ? byTarget : Integer.compare(from, other.from);
        }
    }

    private final Graph graph;
    private List<Edge> mstEdges = new ArrayList<>();
    private List<String> trace = new ArrayList<>();
    private long totalCost;

    /**
     * @param graph a connected, validated graph (see {@link Graph#validate()})
     */
    public PrimMST(Graph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("graph must not be null");
        }
        this.graph = graph;
    }

    /**
     * Builds the MST starting from {@code startVertex}, replacing any previous result.
     *
     * @param startVertex  index of the start vertex (0 = B1)
     * @param recordTrace  {@code true} to record {@link #getTrace()} lines; {@code false} for timing runs
     */
    public void run(int startVertex, boolean recordTrace) {
        int vertexCount = graph.getVertexCount();

        // Fresh state on every call: nothing survives from the previous run.
        boolean[] visited = new boolean[vertexCount];
        List<Edge> selected = new ArrayList<>(vertexCount - 1);
        List<String> steps = new ArrayList<>();
        PriorityQueue<Candidate> queue = new PriorityQueue<>();
        long cost = 0;

        // "B1, B3, B2": grows by one label per step, only when tracing.
        StringBuilder visitedLabels = recordTrace ? new StringBuilder() : null;

        visit(startVertex, visited, queue);
        if (recordTrace) {
            visitedLabels.append(graph.getLabel(startVertex));
        }

        // A spanning tree has exactly n - 1 edges, so stop as soon as we have them.
        while (selected.size() < vertexCount - 1 && !queue.isEmpty()) {
            Candidate cheapest = queue.poll();
            if (visited[cheapest.to]) {
                continue; // both ends are visited now: taking it would make a cycle
            }

            selected.add(cheapest.edge);
            cost += cheapest.edge.getWeight();
            visit(cheapest.to, visited, queue);

            if (recordTrace) {
                visitedLabels.append(", ").append(graph.getLabel(cheapest.to));
                steps.add("Step " + selected.size() + ": "
                        + graph.getLabel(cheapest.from) + " - " + graph.getLabel(cheapest.to)
                        + " | Weight: " + cheapest.edge.getWeight()
                        + " | Visited: {" + visitedLabels + "}");
            }
        }

        mstEdges = selected;
        trace = steps;
        totalCost = cost;
    }

    /** Marks {@code vertex} visited and queues its edges that lead to unvisited vertices. */
    private void visit(int vertex, boolean[] visited, PriorityQueue<Candidate> queue) {
        visited[vertex] = true;
        for (Edge edge : graph.getNeighbors(vertex)) {
            if (!visited[edge.other(vertex)]) {
                queue.add(new Candidate(edge, vertex));
            }
        }
    }

    /** @return the selected MST edges, in the order they were selected */
    public List<Edge> getMstEdges() {
        return Collections.unmodifiableList(mstEdges);
    }

    /** @return total weight of the selected edges (13 for the campus graph) */
    public long getTotalCost() {
        return totalCost;
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
        return Collections.unmodifiableList(trace);
    }
}
