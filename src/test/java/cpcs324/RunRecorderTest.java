package cpcs324;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Saved runs: numbered folders, pictures, tables and the page. Owner: Yasir. */
class RunRecorderTest {

    /** Two table rows in the exact layout Experiments.runAll prints. */
    private static final String TABLES = String.join("\n",
            "Experiment A (ms)",
            "     n      m      Run 1      Run 2      Run 3      Run 4      Run 5    Average",
            "   500   1500      0.888      0.720      0.720      0.637      0.621      0.717",
            "  1000   3000      1.239      1.241      1.175      1.180      1.127      1.192",
            "",
            "Experiment B (ms)",
            "     n      m      Run 1      Run 2      Run 3      Run 4      Run 5    Average",
            "  1000   2000      0.455      0.399      0.393      0.395      0.402      0.409",
            "  1000   5000      0.976      0.975      0.959      0.875      0.863      0.930",
            "");

    @TempDir
    Path tmp;

    private static int runCampus(Path runsRoot, String... flags) {
        PrintStream out = new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);
        return Main.run(flags, out, runsRoot);
    }

    @Test
    void everyRunGetsTheNextNumberedFolder() throws IOException {
        Path runs = tmp.resolve("runs");
        assertEquals(0, runCampus(runs, "--skip-experiments"));
        assertEquals(0, runCampus(runs, "--skip-experiments"));
        assertTrue(Files.isDirectory(runs.resolve("1")), "first run");
        assertTrue(Files.isDirectory(runs.resolve("2")), "second run");
        assertEquals(2, RunRecorder.latestRunNumber(runs));
        assertTrue(Files.isRegularFile(runs.resolve("index.html")), "list of runs");
    }

    @Test
    void campusRunSavesOutputPicturesAndPage() throws IOException {
        Path runs = tmp.resolve("runs");
        assertEquals(0, runCampus(runs, "--skip-experiments"));
        Path folder = runs.resolve("1");

        String output = Files.readString(folder.resolve("output.txt"));
        assertTrue(output.contains("Total MST Cost: 13"), output);

        for (String name : List.of("graph.png", "step-1.png", "step-5.png", "mst.png")) {
            Path picture = folder.resolve(name);
            assertTrue(Files.isRegularFile(picture) && Files.size(picture) > 1000, name);
        }
        assertFalse(Files.exists(folder.resolve("step-6.png")), "the campus MST has 5 edges");
        assertFalse(Files.exists(folder.resolve("experiment-a.png")), "experiments were skipped");

        String page = Files.readString(folder.resolve("index.html"));
        assertTrue(page.contains("Step 5: B5 - B6 | Weight: 3"), "page carries the real trace");
    }

    @Test
    void noSaveFlagCreatesNothing() {
        Path runs = tmp.resolve("runs");
        assertEquals(0, runCampus(runs, "--skip-experiments", "--no-save"));
        assertFalse(Files.exists(runs));
    }

    @Test
    void experimentTablesAreReadBackFromThePrintedOutput() {
        List<RunRecorder.ExperimentRow> a = RunRecorder.parseExperiment(TABLES, "Experiment A");
        List<RunRecorder.ExperimentRow> b = RunRecorder.parseExperiment(TABLES, "Experiment B");
        assertEquals(2, a.size());
        assertEquals(2, b.size());
        assertEquals(1000, a.get(1).n());
        assertEquals(3000, a.get(1).m());
        assertEquals(5, a.get(1).runs().length);
        assertEquals(1.192, a.get(1).average(), 1e-9);
        assertEquals(5000, b.get(1).m());
        assertTrue(RunRecorder.parseExperiment("Graph Statistics\n", "Experiment A").isEmpty());
    }

    @Test
    void experimentRunSavesChartsAndCsv() throws IOException {
        Path folder = new RunRecorder(tmp.resolve("runs")).save(TABLES);
        for (String name : List.of("experiment-a.png", "experiment-b.png")) {
            assertTrue(Files.size(folder.resolve(name)) > 1000, name);
        }
        List<String> csv = Files.readAllLines(folder.resolve("experiment-a.csv"));
        assertEquals("n,m,run1_ms,run2_ms,run3_ms,run4_ms,run5_ms,average_ms", csv.get(0));
        assertEquals("1000,3000,1.239,1.241,1.175,1.180,1.127,1.192", csv.get(2));
        assertFalse(Files.exists(folder.resolve("graph.png")), "no campus part in this run");
    }
}
