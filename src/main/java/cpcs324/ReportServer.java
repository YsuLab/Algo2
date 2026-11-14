package cpcs324;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A very small web server for the saved runs, using only the HTTP server that ships
 * with the JDK. Started with {@code --serve}; it shows the newest run in the browser.
 *
 * <p>It answers only requests from this computer (it listens on the loopback address),
 * and it only ever reads files inside the runs folder.
 *
 * <p><b>Owner:</b> Yasir.
 */
public final class ReportServer {

    /** Port tried first. If it is busy, the system picks a free one. */
    public static final int DEFAULT_PORT = 8080;

    private ReportServer() {
        // static utility class
    }

    /**
     * Starts the server and returns straight away. The server keeps the program alive
     * until it is stopped with Ctrl+C.
     *
     * @param runsRoot folder that holds the numbered run folders
     * @return the address to open in a browser, e.g. {@code http://localhost:8080/}
     * @throws IOException if the server cannot start
     */
    public static String start(Path runsRoot) throws IOException {
        Path root = runsRoot.toAbsolutePath().normalize();
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), DEFAULT_PORT), 0);
        } catch (IOException portBusy) {
            server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        }
        server.createContext("/", exchange -> handle(exchange, root));
        server.start();
        return "http://localhost:" + server.getAddress().getPort() + "/";
    }

    private static void handle(HttpExchange exchange, Path root) throws IOException {
        try (exchange) {
            if (!exchange.getRequestMethod().equals("GET")) {
                send(exchange, 405, "text/plain; charset=utf-8", "Only GET is supported".getBytes(StandardCharsets.UTF_8));
                return;
            }
            String path = exchange.getRequestURI().getPath();

            // The front page always jumps to the newest run.
            if (path.equals("/")) {
                int latest = RunRecorder.latestRunNumber(root);
                if (latest == 0) {
                    send(exchange, 404, "text/plain; charset=utf-8",
                            "No runs saved yet. Run the program once first.".getBytes(StandardCharsets.UTF_8));
                    return;
                }
                exchange.getResponseHeaders().set("Location", "/" + latest + "/index.html");
                exchange.sendResponseHeaders(302, -1);
                return;
            }

            // Resolve inside the runs folder and refuse anything that escapes it (e.g. "..").
            Path file = root.resolve(path.substring(1)).normalize();
            if (!file.startsWith(root)) {
                send(exchange, 403, "text/plain; charset=utf-8", "Forbidden".getBytes(StandardCharsets.UTF_8));
                return;
            }
            if (Files.isDirectory(file)) {
                file = file.resolve("index.html");
            }
            if (!Files.isRegularFile(file)) {
                send(exchange, 404, "text/plain; charset=utf-8", "Not found".getBytes(StandardCharsets.UTF_8));
                return;
            }
            send(exchange, 200, contentType(file.getFileName().toString()), Files.readAllBytes(file));
        }
    }

    private static void send(HttpExchange exchange, int status, String type, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    private static String contentType(String name) {
        if (name.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (name.endsWith(".png")) {
            return "image/png";
        }
        if (name.endsWith(".csv")) {
            return "text/csv; charset=utf-8";
        }
        if (name.endsWith(".txt")) {
            return "text/plain; charset=utf-8";
        }
        return "application/octet-stream";
    }
}
