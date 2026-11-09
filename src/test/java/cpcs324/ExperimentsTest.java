package cpcs324;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Requirement 3.2-3.4: timing harness. Owner: Ahmed. */
@ExtendWith(PendingUntilImplemented.class)
class ExperimentsTest {

    @Test
    void configurationsMatchAssignment() {
        assertEquals(4, Experiments.EXPERIMENT_A.length);
        for (int[] config : Experiments.EXPERIMENT_A) {
            assertEquals(3 * config[0], config[1], "Experiment A keeps m/n = 3");
        }
        for (int[] config : Experiments.EXPERIMENT_B) {
            assertEquals(1000, config[0], "Experiment B keeps n = 1000");
        }
    }

    @Test
    void timePrimReturnsFiveNonNegativeRuns() {
        Graph g = RandomGraphGenerator.generateRandomGraph(300, 900, new Random(5));
        double[] times = Experiments.timePrim(g);
        assertEquals(Experiments.RUNS, times.length);
        for (double t : times) {
            assertTrue(t >= 0, "negative time " + t);
        }
    }
}
