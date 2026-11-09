package cpcs324;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Requirement 2: Prim's algorithm, trace, summary. Owner: Yasir. */
@ExtendWith(PendingUntilImplemented.class)
class PrimMSTTest {

    /** Step trace copied from the assignment's expected output. */
    static final List<String> EXPECTED_TRACE = List.of(
            "Step 1: B1 - B3 | Weight: 2 | Visited: {B1, B3}",
            "Step 2: B3 - B2 | Weight: 1 | Visited: {B1, B3, B2}",
            "Step 3: B2 - B4 | Weight: 5 | Visited: {B1, B3, B2, B4}",
            "Step 4: B4 - B5 | Weight: 2 | Visited: {B1, B3, B2, B4, B5}",
            "Step 5: B5 - B6 | Weight: 3 | Visited: {B1, B3, B2, B4, B5, B6}");

    private static PrimMST campusMst() throws IOException {
        Graph g = Graph.readGraphFromFile(Main.INPUT_FILE);
        PrimMST prim = new PrimMST(g);
        prim.run(g.indexOf("B1"), true);
        return prim;
    }

    @Test
    void campusCostAndEdgeCount() throws IOException {
        PrimMST prim = campusMst();
        assertEquals(13, prim.getTotalCost());
        assertEquals(5, prim.getMstEdges().size(), "n - 1 edges");
    }

    @Test
    void campusTraceMatchesAssignmentExactly() throws IOException {
        assertEquals(EXPECTED_TRACE, campusMst().getTrace());
    }

    @Test
    void noTraceWhenTracingIsOff() throws IOException {
        Graph g = Graph.readGraphFromFile(Main.INPUT_FILE);
        PrimMST prim = new PrimMST(g);
        prim.run(0, false);
        assertTrue(prim.getTrace().isEmpty());
        assertEquals(13, prim.getTotalCost());
    }

    @Test
    void repeatedRunsStartFromCleanState() throws IOException {
        Graph g = Graph.readGraphFromFile(Main.INPUT_FILE);
        PrimMST prim = new PrimMST(g);
        for (int i = 0; i < 5; i++) {
            prim.run(0, false);
            assertEquals(13, prim.getTotalCost(), "run " + (i + 1));
            assertEquals(5, prim.getMstEdges().size(), "run " + (i + 1));
        }
    }

    @Test
    void costDoesNotDependOnStartVertex() throws IOException {
        Graph g = Graph.readGraphFromFile(Main.INPUT_FILE);
        for (int start = 0; start < g.getVertexCount(); start++) {
            PrimMST prim = new PrimMST(g);
            prim.run(start, false);
            assertEquals(13, prim.getTotalCost(), "start at " + g.getLabel(start));
        }
    }

    @Test
    void handBuiltSquareWithDiagonal() {
        // B1-B2 1, B2-B3 2, B3-B4 3, B4-B1 4, B1-B3 5  ->  MST = 1 + 2 + 3 = 6
        Graph g = new Graph(4);
        g.addEdge("B1", "B2", 1);
        g.addEdge("B2", "B3", 2);
        g.addEdge("B3", "B4", 3);
        g.addEdge("B4", "B1", 4);
        g.addEdge("B1", "B3", 5);
        PrimMST prim = new PrimMST(g);
        prim.run(0, false);
        assertEquals(6, prim.getTotalCost());
        assertEquals(3, prim.getMstEdges().size());
    }

    @Test
    void worksOnGeneratedGraphs() {
        // Integration with Ahmed's generator: n - 1 edges on a bigger random graph.
        Graph g = RandomGraphGenerator.generateRandomGraph(1000, 5000, new Random(324));
        PrimMST prim = new PrimMST(g);
        prim.run(0, false);
        assertEquals(999, prim.getMstEdges().size());
    }
}
