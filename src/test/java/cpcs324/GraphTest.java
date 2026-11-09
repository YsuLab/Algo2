package cpcs324;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Requirement 1: input file, adjacency list, validation, statistics. Owner: Badr. */
@ExtendWith(PendingUntilImplemented.class)
class GraphTest {

    @TempDir
    Path tmp;

    private Graph campus() throws IOException {
        return Graph.readGraphFromFile(Main.INPUT_FILE);
    }

    private Graph fromText(String text) throws IOException {
        Path file = tmp.resolve("g.txt");
        Files.writeString(file, text);
        return Graph.readGraphFromFile(file.toString());
    }

    @Test
    void campusStatisticsMatchAssignment() throws IOException {
        Graph g = campus();
        assertEquals(6, g.getVertexCount(), "vertices");
        assertEquals(9, g.getEdgeCount(), "edges");
        assertEquals(1, g.countConnectedComponents(), "components");
        assertEquals(41, g.getTotalEdgeWeight(), "total weight");
        assertTrue(g.validate().isEmpty(), "campus graph must be valid: " + g.validate());
    }

    @Test
    void labelsMapToIndices() {
        Graph g = new Graph(6);
        assertEquals("B1", g.getLabel(0));
        assertEquals("B6", g.getLabel(5));
        assertEquals(2, g.indexOf("B3"));
        assertThrows(IllegalArgumentException.class, () -> g.indexOf("B7"));
    }

    @Test
    void edgesAreUndirectedButCountedOnce() {
        Graph g = new Graph(3);
        g.addEdge("B1", "B2", 7);
        assertEquals(1, g.getEdgeCount());
        assertEquals(1, g.getEdges().size());
        assertEquals(1, g.getNeighbors(0).size(), "B1 sees B2");
        assertEquals(1, g.getNeighbors(1).size(), "B2 sees B1");
        assertEquals(1, g.getNeighbors(0).get(0).other(0));
        assertEquals(0, g.getNeighbors(1).get(0).other(1));
        assertEquals(7, g.getTotalEdgeWeight());
    }

    @Test
    void campusNeighborsOfB3() throws IOException {
        Graph g = campus();
        int b3 = g.indexOf("B3");
        Set<String> neighbors = new HashSet<>();
        for (Edge e : g.getNeighbors(b3)) {
            neighbors.add(g.getLabel(e.other(b3)));
        }
        assertEquals(Set.of("B1", "B2", "B4", "B5"), neighbors);
    }

    @Test
    void detectsNonPositiveWeight() throws IOException {
        Graph g = fromText("3 2\nB1 B2 4\nB2 B3 0\n");
        assertFalse(g.hasOnlyPositiveWeights());
        assertFalse(g.validate().isEmpty(), "weight 0 must be reported");
    }

    @Test
    void detectsDisconnectedGraph() throws IOException {
        Graph g = fromText("4 2\nB1 B2 1\nB3 B4 1\n");
        assertEquals(2, g.countConnectedComponents());
        List<String> problems = g.validate();
        assertFalse(problems.isEmpty(), "disconnected graph must be reported");
    }

    @Test
    void isolatedVertexIsItsOwnComponent() throws IOException {
        Graph g = fromText("3 1\nB1 B2 5\n");
        assertEquals(2, g.countConnectedComponents());
    }

    @Test
    void rejectsMalformedFile() {
        assertThrows(IllegalArgumentException.class, () -> fromText("3 2\nB1 B2 4\n"),
                "header says 2 edges but only 1 line");
        assertThrows(IllegalArgumentException.class, () -> fromText("3 1\nB1 B9 4\n"),
                "unknown vertex B9");
        assertThrows(IllegalArgumentException.class, () -> fromText("3 1\nB1 B2 four\n"),
                "non-integer weight");
    }

    @Test
    void missingFileIsAnIOException() {
        assertThrows(IOException.class,
                () -> Graph.readGraphFromFile(tmp.resolve("nope.txt").toString()));
    }
}
