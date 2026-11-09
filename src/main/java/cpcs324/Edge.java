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

    // TODO(Badr): fields for the two endpoints and the weight (make them private and final).

    /**
     * Creates an edge between {@code u} and {@code v}.
     *
     * @param u      index of the first endpoint (as written in the input file)
     * @param v      index of the second endpoint
     * @param weight installation cost of this cable
     */
    public Edge(int u, int v, int weight) {
        throw new UnsupportedOperationException("TODO(Badr): Edge constructor");
    }

    /** @return the first endpoint, in the order the edge was created */
    public int getFrom() {
        throw new UnsupportedOperationException("TODO(Badr): Edge.getFrom");
    }

    /** @return the second endpoint, in the order the edge was created */
    public int getTo() {
        throw new UnsupportedOperationException("TODO(Badr): Edge.getTo");
    }

    /** @return the installation cost of this edge */
    public int getWeight() {
        throw new UnsupportedOperationException("TODO(Badr): Edge.getWeight");
    }

    /**
     * Returns the endpoint that is not {@code vertex}.
     *
     * @param vertex one endpoint of this edge
     * @return the other endpoint
     * @throws IllegalArgumentException if {@code vertex} is not an endpoint of this edge
     */
    public int other(int vertex) {
        throw new UnsupportedOperationException("TODO(Badr): Edge.other");
    }
}
