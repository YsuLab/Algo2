package cpcs324;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * Entry point. Run from the repository root so {@code campus_graph.txt} is found.
 *
 * <pre>
 * java -jar target/algo2.jar                      full run: campus graph + experiments
 * java -jar target/algo2.jar --skip-experiments   campus graph only (used by the tests)
 * java -jar target/algo2.jar --experiments-only   experiments only
 * java -jar target/algo2.jar --serve              also open the visualizer at http://localhost:8080
 * java -jar target/algo2.jar --no-save            do not create a runs/N folder
 * </pre>
 *
 * <p>Every run saves its printed output, pictures and timing tables in a new numbered
 * folder: {@code runs/1}, {@code runs/2}, and so on (see {@link RunRecorder}).
 *
 * <p><b>Owner:</b> Yasir (integration with Badr's and Ahmed's parts).
 */
public final class Main {

    /** Input file required by the assignment. */
    public static final String INPUT_FILE = "campus_graph.txt";

    /** Every run is saved in a numbered folder under this one: runs/1, runs/2, ... */
    public static final String RUNS_FOLDER = "runs";

    /** Prim starts from this building, as the assignment requires. */
    private static final String START_LABEL = "B1";

    private Main() {
    }

    public static void main(String[] args) {
        int exitCode = run(args, System.out, Path.of(RUNS_FOLDER));
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    /**
     * Runs the program and prints to {@code out}. Kept separate from {@link #main(String[])}
     * so tests can capture the output without starting a new JVM.
     *
     * <p>Output must contain these blocks, as in the assignment's expected output:
     * "Graph Statistics", "Graph Validation: Successful", "Prim's Algorithm (Starting from B1)"
     * with the step trace, and "MST Summary" with the n - 1 verification line.
     * See {@code docs/expected-output.txt}.
     *
     * @param args command-line flags (see class comment)
     * @param out  where to print
     * @return process exit code: 0 on success, 1 if the input is missing or invalid,
     *         2 if a command-line flag is not recognized
     */
    static int run(String[] args, PrintStream out) {
        return run(args, out, null);
    }

    /**
     * Same as {@link #run(String[], PrintStream)}, and also saves the run.
     *
     * @param args     command-line flags (see class comment)
     * @param out      where to print
     * @param runsRoot folder for the numbered run folders, or {@code null} to save nothing
     *                 (the tests use {@code null} so they leave no files behind)
     * @return process exit code, as in {@link #run(String[], PrintStream)}
     */
    static int run(String[] args, PrintStream out, Path runsRoot) {
        boolean runCampus = true;
        boolean runExperiments = true;
        boolean save = runsRoot != null;
        boolean serve = false;
        for (String arg : args) {
            if (arg.equals("--skip-experiments")) {
                runExperiments = false;
            } else if (arg.equals("--experiments-only")) {
                runCampus = false;
            } else if (arg.equals("--no-save")) {
                save = false;
            } else if (arg.equals("--serve")) {
                serve = true;
            } else {
                out.println("Unknown option: " + arg);
                out.println("Usage: java -jar algo2.jar [--skip-experiments | --experiments-only]"
                        + " [--no-save] [--serve]");
                return 2;
            }
        }
        if (!runCampus && !runExperiments) {
            out.println("--skip-experiments and --experiments-only cannot be used together");
            return 2;
        }

        // Everything printed from here on is also kept in memory, so it can be saved as output.txt.
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream both = new PrintStream(new Tee(out, captured), true, StandardCharsets.UTF_8);
        RunRecorder recorder = save ? new RunRecorder(runsRoot) : null;

        int exitCode = 0;
        if (runCampus) {
            exitCode = runCampus(both, recorder);
        }
        // Invalid input: do not go on to the experiments.
        if (exitCode == 0 && runExperiments) {
            if (runCampus) {
                both.println();
            }
            Experiments.runAll(both);
        }
        both.flush();

        if (recorder != null) {
            saveRun(recorder, captured.toString(StandardCharsets.UTF_8), out);
        }
        if (serve && runsRoot != null) {
            try {
                out.println("Visualizer: " + ReportServer.start(runsRoot) + "  (press Ctrl+C to stop)");
            } catch (IOException ex) {
                out.println("Warning: could not start the visualizer server (" + ex.getMessage() + ")");
            }
        }
        return exitCode;
    }

    /** Saving is a bonus: if it fails, say so and carry on, because the results were already printed. */
    private static void saveRun(RunRecorder recorder, String consoleOutput, PrintStream out) {
        try {
            Path folder = recorder.save(consoleOutput);
            out.println();
            out.println("Run saved to " + folder + " (open " + folder.resolve("index.html") + " in a browser)");
        } catch (IOException | RuntimeException | LinkageError | InternalError ex) {
            out.println();
            out.println("Warning: could not save this run (" + ex + ")");
        }
    }

    /** Writes every byte to two streams: the screen and the in-memory copy. */
    private static final class Tee extends OutputStream {
        private final OutputStream first;
        private final OutputStream second;

        Tee(OutputStream first, OutputStream second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public void write(int b) throws IOException {
            first.write(b);
            second.write(b);
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            first.write(bytes, offset, length);
            second.write(bytes, offset, length);
        }

        @Override
        public void flush() throws IOException {
            first.flush();
            second.flush();
        }
    }

    /** Requirements 1 and 2: read and validate the campus graph, then run Prim from B1. */
    private static int runCampus(PrintStream out, RunRecorder recorder) {
        Graph graph;
        try {
            graph = Graph.readGraphFromFile(INPUT_FILE);
        } catch (IOException ex) {
            out.println("Error: cannot read " + INPUT_FILE + " (" + ex.getMessage() + ")");
            out.println("Run the program from the folder that contains " + INPUT_FILE + ".");
            return 1;
        } catch (IllegalArgumentException ex) {
            out.println("Error: " + INPUT_FILE + " is malformed. " + ex.getMessage());
            return 1;
        }

        int vertexCount = graph.getVertexCount();
        out.println("Graph Statistics");
        out.println("----------------------------");
        out.println("Number of Vertices: " + vertexCount);
        out.println("Number of Edges: " + graph.getEdgeCount());
        out.println("Number of Connected Components: " + graph.countConnectedComponents());
        out.println("Total Edge Weight: " + graph.getTotalEdgeWeight());
        out.println();

        List<String> problems = graph.validate();
        if (!problems.isEmpty()) {
            out.println("Graph Validation: Failed");
            for (String problem : problems) {
                out.println("- " + problem);
            }
            return 1;
        }
        out.println("Graph Validation: Successful");
        out.println();

        int start = graph.indexOf(START_LABEL);
        PrimMST prim = new PrimMST(graph);
        prim.run(start, true);
        if (recorder != null) {
            recorder.recordCampus(graph, start, prim);
        }

        out.println("Prim's Algorithm (Starting from " + START_LABEL + ")");
        out.println("------------------------------------------");
        for (String step : prim.getTrace()) {
            out.println(step);
        }
        out.println();

        int mstEdgeCount = prim.getMstEdges().size();
        boolean isSpanningTree = mstEdgeCount == vertexCount - 1;
        out.println("MST Summary");
        out.println("------------------------------------------");
        out.println("Number of MST Edges: " + mstEdgeCount);
        out.println("Total MST Cost: " + prim.getTotalCost());
        out.println("MST Edge Count Verification: "
                + (isSpanningTree ? "Passed (" : "Failed (")
                + mstEdgeCount + (isSpanningTree ? " = " : " != ") + vertexCount + " - 1)");
        return isSpanningTree ? 0 : 1;
    }
}
