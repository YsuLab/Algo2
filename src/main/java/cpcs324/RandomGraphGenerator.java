package cpcs324;

import java.util.Random;

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
     * (B1..Bn) and exactly {@code m} distinct edges, with no self-loops or duplicate edges.
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
        throw new UnsupportedOperationException("TODO(Ahmed): RandomGraphGenerator.generateRandomGraph");
    }
}
