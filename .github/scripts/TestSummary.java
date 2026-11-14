import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Turns Surefire XML reports into a Markdown progress table for the GitHub job summary.
 *
 * <p>Usage (single-file source launch, no compile step):
 * <pre>java .github/scripts/TestSummary.java target/surefire-reports >> "$GITHUB_STEP_SUMMARY"</pre>
 *
 * <p>"Pending" = a test that reached a TODO stub (see PendingUntilImplemented).
 */
public class TestSummary {

    /** Test class -> owner. Keep in sync with the ownership table in README.md. */
    private static final Map<String, String> OWNERS = Map.of(
            "GraphTest", "Badr",
            "PrimMSTTest", "Yasir",
            "MainOutputTest", "Yasir",
            "RunRecorderTest", "Yasir",
            "RandomGraphGeneratorTest", "Ahmed",
            "ExperimentsTest", "Ahmed");

    private record Row(String owner, String suite, int passed, int pending, int failed) { }

    public static void main(String[] args) throws Exception {
        // The job summary is UTF-8 Markdown; don't depend on the runner's locale.
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        Path dir = Path.of(args.length > 0 ? args[0] : "target/surefire-reports");
        List<Row> rows = new ArrayList<>();
        Map<String, Set<String>> todosByOwner = new LinkedHashMap<>();

        if (!Files.isDirectory(dir)) {
            System.out.println("No test reports found in `" + dir + "` (the build probably failed to compile).");
            return;
        }

        try (DirectoryStream<Path> files = Files.newDirectoryStream(dir, "TEST-*.xml")) {
            for (Path file : files) {
                Element suite = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder().parse(file.toFile()).getDocumentElement();
                String fullName = suite.getAttribute("name");
                String simple = fullName.substring(fullName.lastIndexOf('.') + 1);
                String owner = OWNERS.getOrDefault(simple, "Team");

                int tests = Integer.parseInt(suite.getAttribute("tests"));
                int failed = Integer.parseInt(suite.getAttribute("failures"))
                        + Integer.parseInt(suite.getAttribute("errors"));
                int pending = Integer.parseInt(suite.getAttribute("skipped"));
                rows.add(new Row(owner, simple, tests - failed - pending, pending, failed));

                NodeList skipped = suite.getElementsByTagName("skipped");
                for (int i = 0; i < skipped.getLength(); i++) {
                    String message = ((Element) skipped.item(i)).getAttribute("message");
                    int at = message.indexOf("TODO(");
                    if (at >= 0) {
                        todosByOwner.computeIfAbsent(owner, k -> new LinkedHashSet<>())
                                .add(message.substring(at));
                    }
                }
            }
        }

        rows.sort((a, b) -> (a.owner() + a.suite()).compareTo(b.owner() + b.suite()));
        int passed = rows.stream().mapToInt(Row::passed).sum();
        int pending = rows.stream().mapToInt(Row::pending).sum();
        int failed = rows.stream().mapToInt(Row::failed).sum();
        int total = passed + pending + failed;

        System.out.println("## Spec progress: " + passed + " / " + total + " tests passing");
        System.out.println();
        System.out.println("| Owner | Test class | ✅ Passing | ⏳ Pending | ❌ Failing |");
        System.out.println("|---|---|---:|---:|---:|");
        for (Row r : rows) {
            System.out.printf("| %s | `%s` | %d | %d | %d |%n",
                    r.owner(), r.suite(), r.passed(), r.pending(), r.failed());
        }
        System.out.println();
        if (failed > 0) {
            System.out.println("❌ **" + failed + " failing.** Open the test report artifact or the log above.");
            System.out.println();
        }
        if (!todosByOwner.isEmpty()) {
            System.out.println("<details><summary>Stubs still blocking tests</summary>");
            System.out.println();
            todosByOwner.forEach((owner, todos) -> {
                System.out.println("**" + owner + "**");
                todos.forEach(t -> System.out.println("- `" + t + "`"));
                System.out.println();
            });
            System.out.println("</details>");
        }
    }
}
