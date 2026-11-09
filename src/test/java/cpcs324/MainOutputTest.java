package cpcs324;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * End-to-end check of the printed output against docs/expected-output.txt
 * (copied from the assignment). Owner: Yasir.
 *
 * <p>Every non-blank expected line must appear in the program output, in the same order.
 * Extra lines are allowed. Lines made only of dashes match any dash line, so the
 * separator length does not matter.
 */
@ExtendWith(PendingUntilImplemented.class)
class MainOutputTest {

    private static boolean isSeparator(String line) {
        return !line.isEmpty() && line.chars().allMatch(c -> c == '-');
    }

    private static List<String> meaningfulLines(String text) {
        List<String> lines = new ArrayList<>();
        for (String line : text.split("\\R")) {
            String trimmed = line.strip();
            if (!trimmed.isEmpty()) {
                lines.add(trimmed);
            }
        }
        return lines;
    }

    @Test
    void campusOutputMatchesAssignment() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int exit;
        try (PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            exit = Main.run(new String[] {"--skip-experiments"}, out);
        }
        String actual = buffer.toString(StandardCharsets.UTF_8);
        assertEquals(0, exit, "exit code\n" + actual);

        List<String> expected = meaningfulLines(Files.readString(Path.of("docs/expected-output.txt")));
        List<String> got = meaningfulLines(actual);

        int i = 0;
        for (String want : expected) {
            boolean found = false;
            while (i < got.size()) {
                String line = got.get(i++);
                if (line.equals(want) || (isSeparator(want) && isSeparator(line))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail("Missing or out of order: \"" + want + "\"\n--- actual output ---\n" + actual);
            }
        }
    }
}
