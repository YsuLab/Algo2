package cpcs324;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
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

    private final List<List<Edge>> adjacency;
    private final List<Edge> edges = new ArrayList<>();
    private final List<Edge> edgeView = Collections.unmodifiableList(edges);

    /**
     * Creates a graph with {@code vertexCount} vertices labeled B1..Bn and no edges.
     *
     * @param vertexCount number of vertices, at least 1
     */
    public Graph(int vertexCount) {
        if (vertexCount < 1) {
            throw new IllegalArgumentException("Vertex count must be at least 1: " + vertexCount);
        }
        adjacency = new ArrayList<>(vertexCount);
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            adjacency.add(new ArrayList<>());
        }
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
        try (BufferedReader reader = Files.newBufferedReader(Path.of(filename))) {
            String[] header = readFields(reader.readLine(), 2, 1);
            int vertices = parseInteger(header[0], "vertex count", 1);
            int expectedEdges = parseInteger(header[1], "edge count", 1);
            if (vertices < 1 || expectedEdges < 0) {
                throw new IllegalArgumentException(
                        "Line 1: vertex count must be at least 1 and edge count must be non-negative");
            }
            Graph graph = new Graph(vertices);
            for (int i = 0; i < expectedEdges; i++) {
                long lineNumber = (long) i + 2;
                String line = reader.readLine();
                if (line == null) {
                    throw new IllegalArgumentException("Line " + lineNumber + ": expected "
                            + expectedEdges + " edge lines but found " + i);
                }
                String[] fields = readFields(line, 3, lineNumber);
                int weight = parseInteger(fields[2], "edge weight", lineNumber);
                try {
                    graph.addEdge(fields[0], fields[1], weight);
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("Line " + lineNumber + ": " + ex.getMessage(), ex);
                }
            }
            // A final newline is normal; any additional record, even blank, is malformed.
            if (reader.readLine() != null) {
                throw new IllegalArgumentException("Line " + ((long) expectedEdges + 2)
                        + ": unexpected extra line; header declares " + expectedEdges + " edges");
            }
            return graph;
        }
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
        checkVertex(u);
        checkVertex(v);
        Edge edge = new Edge(u, v, weight);
        adjacency.get(u).add(edge);
        adjacency.get(v).add(edge);
        edges.add(edge);
    }

    /**
     * Adds an undirected edge using labels, e.g. {@code addEdge("B1", "B2", 4)}.
     *
     * @throws IllegalArgumentException if a label is not B1..Bn
     */
    public void addEdge(String u, String v, int weight) {
        addEdge(indexOf(u), indexOf(v), weight);
    }

    /** @return number of vertices n */
    public int getVertexCount() {
        return adjacency.size();
    }

    /** @return number of undirected edges m (each edge counted once) */
    public int getEdgeCount() {
        return edges.size();
    }

    /**
     * @param vertex index {@code 0 <= vertex < n}
     * @return the label, e.g. {@code getLabel(0)} is {@code "B1"}
     */
    public String getLabel(int vertex) {
        checkVertex(vertex);
        return "B" + (vertex + 1);
    }

    /**
     * @param label a vertex label such as {@code "B3"}
     * @return its index, e.g. {@code indexOf("B3")} is {@code 2}
     * @throws IllegalArgumentException if the label does not name a vertex of this graph
     */
    public int indexOf(String label) {
        if (label == null || !label.matches("B[1-9][0-9]*")) {
            throw new IllegalArgumentException("Unknown vertex label: " + label
                    + " (expected B1..B" + getVertexCount() + ")");
        }
        int number;
        try {
            number = Integer.parseInt(label.substring(1));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Unknown vertex label: " + label, ex);
        }
        if (number > getVertexCount()) {
            throw new IllegalArgumentException("Unknown vertex label: " + label
                    + " (expected B1..B" + getVertexCount() + ")");
        }
        return number - 1;
    }

    /**
     * @param vertex index {@code 0 <= vertex < n}
     * @return the edges touching {@code vertex}; use {@link Edge#other(int)} to get the neighbor.
     *         Callers must not modify the returned list.
     */
    public List<Edge> getNeighbors(int vertex) {
        checkVertex(vertex);
        return Collections.unmodifiableList(adjacency.get(vertex));
    }

    /** @return every undirected edge exactly once. Callers must not modify the list. */
    public List<Edge> getEdges() {
        return edgeView;
    }

    /** @return sum of all edge weights, each edge counted once (41 for the campus graph) */
    public long getTotalEdgeWeight() {
        long total = 0;
        for (Edge edge : edges) {
            total += edge.getWeight();
        }
        return total;
    }

    /** @return {@code true} if every edge weight is strictly positive */
    public boolean hasOnlyPositiveWeights() {
        for (Edge edge : edges) {
            if (edge.getWeight() <= 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Counts connected components with your own BFS or DFS.
     *
     * @return number of connected components (1 for a valid campus network)
     */
    public int countConnectedComponents() {
        boolean[] visited = new boolean[getVertexCount()];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        int components = 0;
        for (int start = 0; start < getVertexCount(); start++) {
            if (visited[start]) {
                continue;
            }
            components++;
            visited[start] = true;
            queue.addLast(start);
            while (!queue.isEmpty()) {
                int vertex = queue.removeFirst();
                for (Edge edge : adjacency.get(vertex)) {
                    int neighbor = edge.other(vertex);
                    if (!visited[neighbor]) {
                        visited[neighbor] = true;
                        queue.addLast(neighbor);
                    }
                }
            }
        }
        return components;
    }

    /**
     * Checks everything the assignment requires before running Prim.
     *
     * @return an empty list if the graph is valid; otherwise one human-readable message per
     *         problem, e.g. {@code "Edge B1 - B2 has non-positive weight 0"} or
     *         {@code "Graph is not connected (2 components)"}
     */
    public List<String> validate() {
        List<String> problems = new ArrayList<>();
        for (Edge edge : edges) {
            if (edge.getWeight() <= 0) {
                problems.add("Edge " + getLabel(edge.getFrom()) + " - " + getLabel(edge.getTo())
                        + " has non-positive weight " + edge.getWeight());
            }
        }
        int components = countConnectedComponents();
        if (components != 1) {
            problems.add("Graph is not connected (" + components + " components)");
        }
        return List.copyOf(problems);
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= getVertexCount()) {
            throw new IllegalArgumentException("Vertex index " + vertex
                    + " is outside 0.." + (getVertexCount() - 1));
        }
    }

    private static String[] readFields(String line, int expected, long lineNumber) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Line " + lineNumber + ": expected " + expected + " fields");
        }
        String[] fields = line.strip().split("\\s+");
        if (fields.length != expected) {
            throw new IllegalArgumentException("Line " + lineNumber + ": expected " + expected
                    + " fields but found " + fields.length);
        }
        return fields;
    }

    private static int parseInteger(String value, String description, long lineNumber) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Line " + lineNumber + ": invalid integer "
                    + description + " '" + value + "'", ex);
        }
    }
}
