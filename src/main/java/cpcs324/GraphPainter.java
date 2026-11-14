package cpcs324;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.imageio.ImageIO;

/**
 * Draws the pictures saved with every run: the graph, each Prim step, the final MST,
 * and the two experiment charts. Everything is drawn with plain Java 2D, so there is
 * no extra library, and the PNG files can go straight into the report.
 *
 * <p>Colours always mean the same thing: green = already in the tree, blue = the edge
 * picked in this step, grey = everything else.
 *
 * <p><b>Owner:</b> Yasir.
 */
public final class GraphPainter {

    static {
        // No window is ever opened. This lets the drawing work on machines without a screen.
        System.setProperty("java.awt.headless", "true");
    }

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 760;

    private static final Color INK = new Color(0x22, 0x22, 0x26);
    private static final Color GREY = new Color(0xB4, 0xB4, 0xBA);
    private static final Color GREEN = new Color(0x2E, 0x9E, 0x5B);
    private static final Color BLUE = new Color(0x1F, 0x78, 0xD1);
    private static final Color RED = new Color(0xD6, 0x2F, 0x2F);
    private static final Color GRID = new Color(0xE3, 0xE3, 0xE8);

    /** Hand-placed positions for a six-vertex graph, as fractions of the drawing area. */
    private static final double[][] SIX = {
        {0.08, 0.16}, {0.40, 0.08}, {0.22, 0.82}, {0.66, 0.26}, {0.60, 0.92}, {0.92, 0.62}
    };

    private GraphPainter() {
        // static utility class
    }

    /**
     * Where each vertex is drawn, as fractions (0 to 1) of the drawing area.
     * Six vertices use the campus layout; any other size is placed on a circle.
     *
     * @param vertexCount number of vertices
     * @return one {x, y} pair per vertex
     */
    public static double[][] layout(int vertexCount) {
        if (vertexCount == SIX.length) {
            return SIX;
        }
        double[][] positions = new double[vertexCount][2];
        for (int i = 0; i < vertexCount; i++) {
            double angle = -Math.PI / 2 + 2 * Math.PI * i / vertexCount;
            positions[i][0] = 0.5 + 0.44 * Math.cos(angle);
            positions[i][1] = 0.5 + 0.44 * Math.sin(angle);
        }
        return positions;
    }

    /**
     * Saves one picture of the graph as a PNG file.
     *
     * @param file      where to write the PNG
     * @param graph     the graph to draw
     * @param treeEdges edges already in the tree (green); may be empty
     * @param current   the edge picked in this step (blue), or {@code null}
     * @param visited   vertices already visited (green); may be empty
     * @param title     heading drawn at the top
     * @param caption   line drawn at the bottom, e.g. a trace line; may be empty
     * @throws IOException if the file cannot be written
     */
    public static void saveGraph(Path file, Graph graph, Set<Edge> treeEdges, Edge current,
            Set<Integer> visited, String title, String caption) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = begin(image);
        drawHeading(g, title, caption);

        int n = graph.getVertexCount();
        double[][] layout = layout(n);
        int left = 110;
        int top = 120;
        int areaWidth = WIDTH - 2 * left;
        int areaHeight = HEIGHT - top - 130;
        int[] x = new int[n];
        int[] y = new int[n];
        for (int v = 0; v < n; v++) {
            x[v] = left + (int) Math.round(layout[v][0] * areaWidth);
            y[v] = top + (int) Math.round(layout[v][1] * areaHeight);
        }

        // Plain edges first, so the coloured ones are drawn on top of them.
        for (int pass = 0; pass < 3; pass++) {
            for (Edge edge : graph.getEdges()) {
                boolean isCurrent = edge == current;
                boolean inTree = treeEdges.contains(edge);
                int level = isCurrent ? 2 : inTree ? 1 : 0;
                if (level != pass) {
                    continue;
                }
                g.setColor(isCurrent ? BLUE : inTree ? GREEN : GREY);
                g.setStroke(new BasicStroke(level == 0 ? 3f : 8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine(x[edge.getFrom()], y[edge.getFrom()], x[edge.getTo()], y[edge.getTo()]);
            }
        }

        // Weight labels sit on the middle of each edge.
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        FontMetrics metrics = g.getFontMetrics();
        for (Edge edge : graph.getEdges()) {
            String text = String.valueOf(edge.getWeight());
            int cx = (x[edge.getFrom()] + x[edge.getTo()]) / 2;
            int cy = (y[edge.getFrom()] + y[edge.getTo()]) / 2;
            int boxWidth = metrics.stringWidth(text) + 20;
            g.setColor(Color.WHITE);
            g.fillRoundRect(cx - boxWidth / 2, cy - 18, boxWidth, 36, 12, 12);
            g.setStroke(new BasicStroke(2f));
            g.setColor(edge == current ? BLUE : treeEdges.contains(edge) ? GREEN : GREY);
            g.drawRoundRect(cx - boxWidth / 2, cy - 18, boxWidth, 36, 12, 12);
            g.setColor(INK);
            g.drawString(text, cx - metrics.stringWidth(text) / 2, cy + metrics.getAscent() / 2 - 3);
        }

        int radius = n > 16 ? 22 : 34;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, n > 16 ? 16 : 24));
        metrics = g.getFontMetrics();
        for (int v = 0; v < n; v++) {
            boolean isVisited = visited.contains(v);
            g.setColor(isVisited ? new Color(0xE3, 0xF4, 0xE9) : Color.WHITE);
            g.fillOval(x[v] - radius, y[v] - radius, 2 * radius, 2 * radius);
            g.setStroke(new BasicStroke(4f));
            g.setColor(isVisited ? GREEN : GREY);
            g.drawOval(x[v] - radius, y[v] - radius, 2 * radius, 2 * radius);
            String label = graph.getLabel(v);
            g.setColor(INK);
            g.drawString(label, x[v] - metrics.stringWidth(label) / 2, y[v] + metrics.getAscent() / 2 - 3);
        }

        g.dispose();
        ImageIO.write(image, "png", file.toFile());
    }

    /**
     * Saves a line chart of average running time as a PNG file.
     * The dashed grey line is the expected shape for heap-based Prim,
     * {@code c * m * log2(n)}, scaled to fit the measurements.
     *
     * @param file   where to write the PNG
     * @param rows   one measured configuration per row
     * @param useN   {@code true} to put n on the x axis (Experiment A), {@code false} for m (Experiment B)
     * @param red    {@code true} for a red line (Experiment A), {@code false} for blue (Experiment B)
     * @param title  heading drawn at the top
     * @throws IOException if the file cannot be written
     */
    public static void saveChart(Path file, List<RunRecorder.ExperimentRow> rows, boolean useN,
            boolean red, String title) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = begin(image);
        drawHeading(g, title, "");

        int left = 150;
        int right = WIDTH - 70;
        int top = 130;
        int bottom = HEIGHT - 130;

        double scale = fitScale(rows);
        double xMax = 0;
        double yMax = 0;
        for (RunRecorder.ExperimentRow row : rows) {
            xMax = Math.max(xMax, useN ? row.n() : row.m());
            yMax = Math.max(yMax, Math.max(row.average(), scale * shape(row.n(), row.m())));
        }
        yMax = niceCeiling(yMax * 1.1);

        Font small = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
        g.setFont(small);
        FontMetrics metrics = g.getFontMetrics();

        // Horizontal grid lines with the time values.
        for (int i = 0; i <= 4; i++) {
            double value = yMax * i / 4;
            int py = bottom - (int) Math.round((bottom - top) * i / 4.0);
            g.setStroke(new BasicStroke(i == 0 ? 2f : 1f));
            g.setColor(i == 0 ? INK : GRID);
            g.drawLine(left, py, right, py);
            String text = trim(value);
            g.setColor(INK);
            g.drawString(text, left - 14 - metrics.stringWidth(text), py + 7);
        }
        g.setStroke(new BasicStroke(2f));
        g.setColor(INK);
        g.drawLine(left, top, left, bottom);

        int count = rows.size();
        int[] px = new int[count];
        int[] py = new int[count];
        int[] fitY = new int[count];
        for (int i = 0; i < count; i++) {
            RunRecorder.ExperimentRow row = rows.get(i);
            double xValue = useN ? row.n() : row.m();
            px[i] = left + (int) Math.round((right - left - 30) * xValue / xMax);
            py[i] = bottom - (int) Math.round((bottom - top) * row.average() / yMax);
            fitY[i] = bottom - (int) Math.round((bottom - top) * scale * shape(row.n(), row.m()) / yMax);
        }

        // Expected shape, dashed.
        g.setColor(new Color(0x80, 0x80, 0x88));
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f, new float[] {12f, 9f}, 0f));
        g.drawPolyline(px, fitY, count);

        // Measured averages.
        Color line = red ? RED : BLUE;
        g.setColor(line);
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawPolyline(px, py, count);
        for (int i = 0; i < count; i++) {
            RunRecorder.ExperimentRow row = rows.get(i);
            g.setColor(line);
            g.fillOval(px[i] - 9, py[i] - 9, 18, 18);
            g.setColor(INK);
            String value = String.format(Locale.ROOT, "%.3f", row.average());
            int valueX = Math.max(left + 8, Math.min(px[i] - metrics.stringWidth(value) / 2, right - metrics.stringWidth(value)));
            g.drawString(value, valueX, py[i] - 18 < top + 10 ? py[i] + 34 : py[i] - 18);
            String tick = String.valueOf(useN ? row.n() : row.m());
            g.drawLine(px[i], bottom, px[i], bottom + 8);
            g.drawString(tick, px[i] - metrics.stringWidth(tick) / 2, bottom + 34);
        }

        g.setColor(INK);
        String xTitle = useN ? "Number of vertices (n)" : "Number of edges (m)";
        g.drawString(xTitle, (left + right) / 2 - metrics.stringWidth(xTitle) / 2, bottom + 74);
        String yTitle = "Average time (ms)";
        g.rotate(-Math.PI / 2, 44, (top + bottom) / 2.0);
        g.drawString(yTitle, 44 - metrics.stringWidth(yTitle) / 2, (top + bottom) / 2);
        g.rotate(Math.PI / 2, 44, (top + bottom) / 2.0);

        // Legend, top left inside the plot.
        g.setColor(line);
        g.setStroke(new BasicStroke(5f));
        g.drawLine(left + 24, top + 16, left + 74, top + 16);
        g.setColor(INK);
        g.drawString("measured average of the timed runs", left + 86, top + 23);
        g.setColor(new Color(0x80, 0x80, 0x88));
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f, new float[] {12f, 9f}, 0f));
        g.drawLine(left + 24, top + 46, left + 74, top + 46);
        g.setColor(INK);
        g.drawString("expected shape: c * m * log2(n)", left + 86, top + 53);

        g.dispose();
        ImageIO.write(image, "png", file.toFile());
    }

    /** The quantity heap-based Prim's running time should grow with. */
    static double shape(int n, int m) {
        return m * (Math.log(Math.max(2, n)) / Math.log(2));
    }

    /** Least-squares constant c that best fits {@code average = c * shape}. */
    static double fitScale(List<RunRecorder.ExperimentRow> rows) {
        double top = 0;
        double bottom = 0;
        for (RunRecorder.ExperimentRow row : rows) {
            double s = shape(row.n(), row.m());
            top += row.average() * s;
            bottom += s * s;
        }
        return bottom == 0 ? 0 : top / bottom;
    }

    private static Graphics2D begin(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        return g;
    }

    private static void drawHeading(Graphics2D g, String title, String caption) {
        g.setColor(INK);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        g.drawString(title, 50, 64);
        if (!caption.isEmpty()) {
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 22));
            g.drawString(caption, 50, HEIGHT - 44);
        }
    }

    /** Rounds up to 1, 2, 2.5, 5 or 10 times a power of ten, so the axis ends on a round number. */
    private static double niceCeiling(double value) {
        if (value <= 0) {
            return 1;
        }
        double power = Math.pow(10, Math.floor(Math.log10(value)));
        double fraction = value / power;
        double nice = fraction <= 1 ? 1 : fraction <= 2 ? 2 : fraction <= 2.5 ? 2.5 : fraction <= 5 ? 5 : 10;
        return nice * power;
    }

    private static String trim(double value) {
        String text = String.format(Locale.ROOT, "%.3f", value);
        return text.contains(".") ? text.replaceAll("0+$", "").replaceAll("\\.$", "") : text;
    }
}
