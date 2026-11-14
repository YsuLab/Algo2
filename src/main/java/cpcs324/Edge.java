package cpcs324;

/**
 * An undirected, weighted edge between two vertices (a possible cable between two buildings).
 *
 * <p>Vertices are identified by their index in the {@link Graph} (0 = B1, 1 = B2, ...).
 * The same {@code Edge} object is stored in the adjacency lists of both endpoints,
 * so {@link #other(int)} is how you walk from one end to the other.
 *
 * <p><b>Owner:</b> Badr. Everyone depends on this class, so ship it first.
 */
public final class Edge {

    private final int from;
    private final int to;
    private final int weight;

    /**
     * Creates an edge between {@code u} and {@code v}.
     *
     * @param u      index of the first endpoint (as written in the input file)
     * @param v      index of the second endpoint
     * @param weight installation cost of this cable
     */
    public Edge(int u, int v, int weight) {
        this.from = u;
        this.to = v;
        this.weight = weight;
    }

    /** @return the first endpoint, in the order the edge was created */
    public int getFrom() {
        return from;
    }

    /** @return the second endpoint, in the order the edge was created */
    public int getTo() {
        return to;
    }

    /** @return the installation cost of this edge */
    public int getWeight() {
        return weight;
    }

    /**
     * Returns the endpoint that is not {@code vertex}.
     *
     * @param vertex one endpoint of this edge
     * @return the other endpoint
     * @throws IllegalArgumentException if {@code vertex} is not an endpoint of this edge
     */
    public int other(int vertex) {
        if (vertex == from) {
            return to;
        }
        if (vertex == to) {
            return from;
        }
        throw new IllegalArgumentException("Vertex " + vertex
                + " is not an endpoint of edge " + from + " - " + to);
    }
}
