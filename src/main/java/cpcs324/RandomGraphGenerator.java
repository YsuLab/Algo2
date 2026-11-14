package cpcs324;

import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Generates random connected graphs for the performance experiments (and for Part II).
 *
 * <p>Kept in its own file so Ahmed and Badr never edit the same file at the same time.
 * It builds graphs only through the public {@link Graph} API.
 *
 * <p><b>Owner:</b> Ahmed.
 */
public final class RandomGraphGenerator {

    /** Smallest allowed random weight (inclusive). */
    public static final int MIN_WEIGHT = 1;

    /** Largest allowed random weight (inclusive). */
    public static final int MAX_WEIGHT = 100;

    private RandomGraphGenerator() {
        // static utility class
    }

    /**
     * Generates a connected, undirected, weighted graph with exactly {@code n} vertices
     * (B1.Bn) and exactly {@code m} distinct edges, with no self-loops or duplicate edges.
     * Weights are uniform random integers in [{@value #MIN_WEIGHT}, {@value #MAX_WEIGHT}].
     *
     * @param n number of vertices, at least 1
     * @param m number of edges, {@code n - 1 <= m <= n(n-1)/2}
     * @return the generated graph
     * @throws IllegalArgumentException if no simple connected graph with n vertices and m edges exists
     */
    public static Graph generateRandomGraph(int n, int m) {
        return generateRandomGraph(n, m, new Random());
    }

    /**
     * Same as {@link #generateRandomGraph(int, int)} but with a caller-supplied random source,
     * so tests can use a fixed seed and get the same graph every time.
     */
    public static Graph generateRandomGraph(int n, int m, Random random) {
        long maxEdges = (long) n * (n - 1L) / 2;
        if (n < 1 || m < n - 1L || m > maxEdges) {
            throw new IllegalArgumentException("Invalid vertex and edge counts: " + n + ", " + m);
        }
        Objects.requireNonNull(random, "random");

        Graph graph = new Graph(n);
        Set<Long> usedEdges = new HashSet<>();
        for (int vertex = 1; vertex < n; vertex++) {
            int parent = random.nextInt(vertex);
            usedEdges.add((long) parent * n + vertex);
            addWeightedEdge(graph, parent, vertex, random);
        }

        int remaining = m - (n - 1);
        if (m <= maxEdges / 2) {
            while (remaining > 0) {
                int u = random.nextInt(n);
                int v = random.nextInt(n);
                if (u != v && usedEdges.add((long) Math.min(u, v) * n + Math.max(u, v))) {
                    addWeightedEdge(graph, u, v, random);
                    remaining--;
                }
            }
        } else {
            // Sample the unused pairs in one pass so dense graphs don't stall on retries.
            long candidates = maxEdges - (n - 1L);
            for (int u = 0; u < n && remaining > 0; u++) {
                for (int v = u + 1; v < n && remaining > 0; v++) {
                    if (!usedEdges.contains((long) u * n + v)) {
                        if (random.nextLong(candidates) < remaining) {
                            addWeightedEdge(graph, u, v, random);
                            remaining--;
                        }
                        candidates--;
                    }
                }
            }
        }
        return graph;
    }

    private static void addWeightedEdge(Graph graph, int u, int v, Random random) {
        int weight = MIN_WEIGHT + random.nextInt(MAX_WEIGHT - MIN_WEIGHT + 1);
        graph.addEdge(u, v, weight);
    }
}
