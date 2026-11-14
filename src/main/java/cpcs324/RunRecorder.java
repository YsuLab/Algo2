package cpcs324;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Saves everything one run produced into its own numbered folder:
 * {@code runs/1}, {@code runs/2}, {@code runs/3}, and so on. Nothing is ever overwritten,
 * so the timings of different runs can be compared later.
 *
 * <p>Files written to each folder:
 * <pre>
 * output.txt            exactly what the program printed
 * graph.png             the input graph
 * step-1.png ...        one picture per Prim step
 * mst.png               the finished minimum spanning tree
 * experiment-a.png      vertices vs. average time (red line)
 * experiment-b.png      edges vs. average time (blue line)
 * experiment-a.csv      the Experiment A table
 * experiment-b.csv      the Experiment B table
 * index.html            interactive page for this run (see {@link ReportPage})
 * </pre>
 * {@code runs/index.html} is rewritten after every run and lists all of them.
 * The experiment files are written only when the experiments ran, and the graph files
 * only when the campus part ran.
 *
 * <p><b>Owner:</b> Yasir.
 */
public final class RunRecorder {

    /**
     * One line of an experiment table.
     *
     * @param n       number of vertices
     * @param m       number of edges
     * @param runs    time of each timed run, in milliseconds
     * @param average average of the timed runs, in milliseconds
     */
    public record ExperimentRow(int n, int m, double[] runs, double average) { }

    private final Path runsRoot;
    private Graph graph;
    private int startVertex;
    private PrimMST prim;

    /**
     * @param runsRoot folder that holds the numbered run folders, normally {@code runs}
     */
    public RunRecorder(Path runsRoot) {
        this.runsRoot = runsRoot;
    }

    /**
     * Remembers the campus result so its pictures can be drawn when the run is saved.
     *
     * @param graph       the validated input graph
     * @param startVertex index Prim started from
     * @param prim        Prim after {@code run(startVertex, true)}
     */
    public void recordCampus(Graph graph, int startVertex, PrimMST prim) {
        this.graph = graph;
        this.startVertex = startVertex;
        this.prim = prim;
    }

    /**
     * Creates the next numbered folder and writes every file of this run into it.
     *
     * @param consoleOutput everything the program printed during the run
     * @return the folder that was created, e.g. {@code runs/3}
     * @throws IOException if the folder or one of the files cannot be written
     */
    public Path save(String consoleOutput) throws IOException {
        Path folder = createNextFolder();
        int runNumber = Integer.parseInt(folder.getFileName().toString());
        Files.writeString(folder.resolve("output.txt"), consoleOutput, StandardCharsets.UTF_8);

        if (graph != null) {
            saveGraphPictures(folder);
        }

        List<ExperimentRow> experimentA = parseExperiment(consoleOutput, "Experiment A");
        List<ExperimentRow> experimentB = parseExperiment(consoleOutput, "Experiment B");
        if (!experimentA.isEmpty()) {
            Files.writeString(folder.resolve("experiment-a.csv"), toCsv(experimentA), StandardCharsets.UTF_8);
            GraphPainter.saveChart(folder.resolve("experiment-a.png"), experimentA, true, true,
                    "Experiment A: vertices vs. average time (m = 3n)");
        }
        if (!experimentB.isEmpty()) {
            Files.writeString(folder.resolve("experiment-b.csv"), toCsv(experimentB), StandardCharsets.UTF_8);
            GraphPainter.saveChart(folder.resolve("experiment-b.png"), experimentB, false, false,
                    "Experiment B: edges vs. average time (n = " + experimentB.get(0).n() + ")");
        }

        String savedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String page = ReportPage.render(runNumber, savedAt, graph, startVertex, prim, experimentA, experimentB);
        Files.writeString(folder.resolve("index.html"), page, StandardCharsets.UTF_8);
        Files.writeString(runsRoot.resolve("index.html"), ReportPage.renderRunList(latestRunNumber(runsRoot)),
                StandardCharsets.UTF_8);
        return folder;
    }

    /** graph.png, one step-k.png per selected edge, and mst.png. */
    private void saveGraphPictures(Path folder) throws IOException {
        GraphPainter.saveGraph(folder.resolve("graph.png"), graph, Set.of(), null, Set.of(),
                "Campus network: " + graph.getVertexCount() + " buildings, " + graph.getEdgeCount()
                        + " possible links", "Total weight of all links: " + graph.getTotalEdgeWeight());

        Set<Edge> tree = new HashSet<>();
        Set<Integer> visited = new HashSet<>();
        visited.add(startVertex);
        List<Edge> mstEdges = prim.getMstEdges();
        List<String> trace = prim.getTrace();
        for (int i = 0; i < mstEdges.size(); i++) {
            Edge edge = mstEdges.get(i);
            visited.add(edge.getFrom());
            visited.add(edge.getTo());
            String caption = i < trace.size() ? trace.get(i) : "";
            GraphPainter.saveGraph(folder.resolve("step-" + (i + 1) + ".png"), graph, tree, edge, visited,
                    "Prim's algorithm: step " + (i + 1) + " of " + mstEdges.size(), caption);
            tree.add(edge);
        }

        GraphPainter.saveGraph(folder.resolve("mst.png"), graph, tree, null, visited,
                "Minimum spanning tree", "Number of MST Edges: " + mstEdges.size()
                        + " | Total MST Cost: " + prim.getTotalCost());
    }

    /** Finds the highest numbered folder and creates the one after it. */
    private Path createNextFolder() throws IOException {
        Files.createDirectories(runsRoot);
        int next = latestRunNumber(runsRoot) + 1;
        while (true) {
            Path folder = runsRoot.resolve(String.valueOf(next));
            try {
                return Files.createDirectory(folder);
            } catch (FileAlreadyExistsException ex) {
                next++; // another run took this number a moment ago
            }
        }
    }

    /**
     * @param runsRoot folder that holds the numbered run folders
     * @return the highest run number found, or 0 if there are no runs yet
     * @throws IOException if the folder cannot be listed
     */
    public static int latestRunNumber(Path runsRoot) throws IOException {
        if (!Files.isDirectory(runsRoot)) {
            return 0;
        }
        int latest = 0;
        try (Stream<Path> children = Files.list(runsRoot)) {
            for (Path child : (Iterable<Path>) children::iterator) {
                String name = child.getFileName().toString();
                if (Files.isDirectory(child) && name.matches("[1-9][0-9]{0,8}")) {
                    latest = Math.max(latest, Integer.parseInt(name));
                }
            }
        }
        return latest;
    }

    /**
     * Reads one experiment table back out of the printed output, so the saved files always
     * show the same numbers the user saw on screen.
     *
     * @param consoleOutput everything the program printed
     * @param title         the table to look for, {@code "Experiment A"} or {@code "Experiment B"}
     * @return the rows of that table; empty if the table is not in the output
     */
    public static List<ExperimentRow> parseExperiment(String consoleOutput, String title) {
        List<ExperimentRow> rows = new ArrayList<>();
        boolean inTable = false;
        for (String line : consoleOutput.split("\\R")) {
            String text = line.strip();
            if (text.startsWith("Experiment ")) {
                inTable = text.startsWith(title);
                continue;
            }
            if (!inTable || text.isEmpty()) {
                continue;
            }
            String[] fields = text.split("\\s+");
            if (fields.length < 4) {
                continue;
            }
            try {
                int n = Integer.parseInt(fields[0]);
                int m = Integer.parseInt(fields[1]);
                double[] runs = new double[fields.length - 3];
                for (int i = 0; i < runs.length; i++) {
                    runs[i] = Double.parseDouble(fields[i + 2]);
                }
                rows.add(new ExperimentRow(n, m, runs, Double.parseDouble(fields[fields.length - 1])));
            } catch (NumberFormatException ex) {
                // the header line ("n m Run 1 ...") is not a data row: skip it
            }
        }
        return rows;
    }

    private static String toCsv(List<ExperimentRow> rows) {
        StringBuilder csv = new StringBuilder("n,m");
        for (int i = 1; i <= rows.get(0).runs().length; i++) {
            csv.append(",run").append(i).append("_ms");
        }
        csv.append(",average_ms\n");
        for (ExperimentRow row : rows) {
            csv.append(row.n()).append(',').append(row.m());
            for (double run : row.runs()) {
                csv.append(',').append(String.format(Locale.ROOT, "%.3f", run));
            }
            csv.append(',').append(String.format(Locale.ROOT, "%.3f", row.average())).append('\n');
        }
        return csv.toString();
    }
}
