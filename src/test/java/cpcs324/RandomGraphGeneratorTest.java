package cpcs324;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Requirement 3.1: generateRandomGraph(n, m). Owner: Ahmed. */
@ExtendWith(PendingUntilImplemented.class)
class RandomGraphGeneratorTest {

    /** Checks every property the assignment asks for. */
    private static void assertValidGraph(Graph g, int n, int m) {
        assertEquals(n, g.getVertexCount(), "vertex count");
        assertEquals(m, g.getEdgeCount(), "edge count");
        assertEquals(m, g.getEdges().size(), "edge list size");
        assertEquals(1, g.countConnectedComponents(), "must be connected");

        Set<Long> seen = new HashSet<>();
        for (Edge e : g.getEdges()) {
            int a = Math.min(e.getFrom(), e.getTo());
            int b = Math.max(e.getFrom(), e.getTo());
            assertNotEquals(a, b, "self-loop at " + g.getLabel(a));
            assertTrue(seen.add((long) a * n + b),
                    "duplicate edge " + g.getLabel(a) + " - " + g.getLabel(b));
            assertTrue(e.getWeight() >= RandomGraphGenerator.MIN_WEIGHT
                    && e.getWeight() <= RandomGraphGenerator.MAX_WEIGHT,
                    "weight out of range: " + e.getWeight());
        }
    }

    @Test
    void assignmentExampleFiveSeven() {
        assertValidGraph(RandomGraphGenerator.generateRandomGraph(5, 7), 5, 7);
    }

    @ParameterizedTest(name = "Experiment config n={0}, m={1}")
    @CsvSource({
        "500, 1500", "1000, 3000", "2000, 6000", "5000, 15000",
        "1000, 2000", "1000, 5000", "1000, 10000", "1000, 20000"
    })
    void everyExperimentConfigurationIsValid(int n, int m) {
        assertValidGraph(RandomGraphGenerator.generateRandomGraph(n, m, new Random(n * 31L + m)), n, m);
    }

    @Test
    void edgeCasesTreeAndCompleteGraph() {
        assertValidGraph(RandomGraphGenerator.generateRandomGraph(10, 9, new Random(1)), 10, 9);
        assertValidGraph(RandomGraphGenerator.generateRandomGraph(10, 45, new Random(2)), 10, 45);
        assertValidGraph(RandomGraphGenerator.generateRandomGraph(1, 0, new Random(3)), 1, 0);
    }

    @Test
    void rejectsImpossibleSizes() {
        assertThrows(IllegalArgumentException.class,
                () -> RandomGraphGenerator.generateRandomGraph(5, 3), "m < n - 1 cannot be connected");
        assertThrows(IllegalArgumentException.class,
                () -> RandomGraphGenerator.generateRandomGraph(5, 11), "m > n(n-1)/2 needs duplicates");
        assertThrows(IllegalArgumentException.class,
                () -> RandomGraphGenerator.generateRandomGraph(0, 0), "n must be positive");
    }

    @Test
    void sameSeedGivesSameTotalWeight() {
        Graph a = RandomGraphGenerator.generateRandomGraph(50, 200, new Random(7));
        Graph b = RandomGraphGenerator.generateRandomGraph(50, 200, new Random(7));
        assertEquals(a.getTotalEdgeWeight(), b.getTotalEdgeWeight());
    }

    @Test
    void largestConfigurationIsFastEnough() {
        // Generation is outside the timing, but it still has to finish in reasonable time.
        assertDoesNotThrow(() -> {
            long start = System.nanoTime();
            RandomGraphGenerator.generateRandomGraph(1000, 20000, new Random(9));
            long ms = (System.nanoTime() - start) / 1_000_000;
            assertTrue(ms < 5_000, "generation took " + ms + " ms");
        });
    }
}
