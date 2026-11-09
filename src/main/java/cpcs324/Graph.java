package cpcs324;

import java.io.IOException;
import java.util.List;

/**
 * A connected, undirected, weighted graph stored as adjacency lists.
 *
 * <p>Vertices are numbered {@code 0 .. n-1} internally and labeled {@code B1 .. Bn}
 * for printing (index 0 is "B1"). Each undirected edge appears in the adjacency list of
 * <em>both</em> endpoints, but is counted once in {@link #getEdgeCount()},
 * {@link #getEdges()} and {@link #getTotalEdgeWeight()}.
 *
 * <p>This class is shared by everyone:
 * <ul>
 *   <li>{@link PrimMST} reads it through {@link #getNeighbors(int)} (Yasir).</li>
 *   <li>{@link RandomGraphGenerator} builds it through {@link #addEdge(int, int, int)} (Ahmed).</li>
 * </ul>
 * Agree before changing any public signature here.
 *
 * <p><b>Owner:</b> Badr. Suggested order: constructor, addEdge and getNeighbors first
 * (they unblock Prim and the generator), then the file reader, then validation.
 */
public class Graph {

    // TODO(Badr): fields: vertex count, the adjacency lists, and the list of all edges (each once).

    /**
     * Creates a graph with {@code vertexCount} vertices labeled B1..Bn and no edges.
     *
     * @param vertexCount number of vertices, at least 1
     */
    public Graph(int vertexCount) {
        throw new UnsupportedOperationException("TODO(Badr): Graph constructor");
    }

    /**
     * Reads a graph in the assignment format.
     *
     * <pre>
     * 6 9          first line: n (vertices) and m (edges)
     * B1 B2 4      each next line: two vertex labels and an integer weight
     * </pre>
     *
     * The reader does <em>not</em> reject non-positive weights or disconnected graphs.
     * That is the job of {@link #validate()}, so the program can report what is wrong.
     *
     * @param filename path to the input file, e.g. {@code "campus_graph.txt"}
     * @return the graph described by the file
     * @throws IOException              if the file cannot be read
     * @throws IllegalArgumentException if the file is malformed (bad header, wrong number of
     *                                  edge lines, unknown vertex label, non-integer weight)
     */
    public static Graph readGraphFromFile(String filename) throws IOException {
        throw new UnsupportedOperationException("TODO(Badr): Graph.readGraphFromFile");
    }

    /**
     * Adds an undirected edge {@code u - v}.
     * Assumes no self-loops or duplicates (the assignment guarantees this for the input file;
     * the generator must guarantee it itself).
     *
     * @param u      index of one endpoint, {@code 0 <= u < n}
     * @param v      index of the other endpoint
     * @param weight edge weight
     */
    public void addEdge(int u, int v, int weight) {
        throw new UnsupportedOperationException("TODO(Badr): Graph.addEdge(int,int,int)");
    }

    /**
     * Adds an undirected edge using labels, e.g. {@code addEdge("B1", "B2", 4)}.
     *
     * @throws IllegalArgumentException if a label is not B1..Bn
     */
    public void addEdge(String u, String v, int weight) {
        throw new UnsupportedOperationException("TODO(Badr): Graph.addEdge(String,String,int)");
    }

    /** @return number of vertices n */
    public int getVertexCount() {
        throw new UnsupportedOperationException("TODO(Badr): Graph.getVertexCount");
    }

    /** @return number of undirected edges m (each edge counted once) */
    public int getEdgeCount() {
        throw new UnsupportedOperationException("TODO(Badr): Graph.getEdgeCount");
    }

    /**
     * @param vertex index {@code 0 <= vertex < n}
     * @return the label, e.g. {@code getLabel(0)} is {@code "B1"}
     */
    public String getLabel(int vertex) {
        throw new UnsupportedOperationException("TODO(Badr): Graph.getLabel");
    }

    /**
     * @param label a vertex label such as {@code "B3"}
     * @return its index, e.g. {@code indexOf("B3")} is {@code 2}
     * @throws IllegalArgumentException if the label does not name a vertex of this graph
     */
    public int indexOf(String label) {
        throw new UnsupportedOperationException("TODO(Badr): Graph.indexOf");
    }

    /**
     * @param vertex index {@code 0 <= vertex < n}
     * @return the edges touching {@code vertex}; use {@link Edge#other(int)} to get the neighbor.
     *         Callers must not modify the returned list.
     */
    public List<Edge> getNeighbors(int vertex) {
        throw new UnsupportedOperationException("TODO(Badr): Graph.getNeighbors");
    }

    /** @return every undirected edge exactly once. Callers must not modify the list. */
    public List<Edge> getEdges() {
        throw new UnsupportedOperationException("TODO(Badr): Graph.getEdges");
    }

    /** @return sum of all edge weights, each edge counted once (41 for the campus graph) */
    public long getTotalEdgeWeight() {
        throw new UnsupportedOperationException("TODO(Badr): Graph.getTotalEdgeWeight");
    }

    /** @return {@code true} if every edge weight is strictly positive */
    public boolean hasOnlyPositiveWeights() {
        throw new UnsupportedOperationException("TODO(Badr): Graph.hasOnlyPositiveWeights");
    }

    /**
     * Counts connected components with your own BFS or DFS.
     *
     * @return number of connected components (1 for a valid campus network)
     */
    public int countConnectedComponents() {
        throw new UnsupportedOperationException("TODO(Badr): Graph.countConnectedComponents");
    }

    /**
     * Checks everything the assignment requires before running Prim.
     *
     * @return an empty list if the graph is valid; otherwise one human-readable message per
     *         problem, e.g. {@code "Edge B1 - B2 has non-positive weight 0"} or
     *         {@code "Graph is not connected (2 components)"}
     */
    public List<String> validate() {
        throw new UnsupportedOperationException("TODO(Badr): Graph.validate");
    }
}
