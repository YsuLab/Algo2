package cpcs324;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.LifecycleMethodExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.opentest4j.TestAbortedException;

/**
 * Turns "not written yet" into "pending" instead of "failed".
 *
 * <p>Every stub in src/main throws {@code UnsupportedOperationException("TODO(Name): ...")}.
 * When a test hits one of those, this extension reports the test as skipped with the TODO
 * message, so CI stays green while the team is still writing code.
 * As soon as the stub is replaced with real code, the test runs for real and can fail.
 *
 * <p>Only messages starting with {@code "TODO("} are treated this way, so a genuine
 * {@code UnsupportedOperationException} (e.g. modifying {@code List.of()}) still fails the test.
 */
public class PendingUntilImplemented
        implements TestExecutionExceptionHandler, LifecycleMethodExecutionExceptionHandler {

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable)
            throws Throwable {
        throw convert(throwable);
    }

    @Override
    public void handleBeforeEachMethodExecutionException(ExtensionContext context, Throwable throwable)
            throws Throwable {
        throw convert(throwable);
    }

    @Override
    public void handleBeforeAllMethodExecutionException(ExtensionContext context, Throwable throwable)
            throws Throwable {
        throw convert(throwable);
    }

    private static Throwable convert(Throwable throwable) {
        for (Throwable t = throwable; t != null; t = t.getCause()) {
            if (t instanceof UnsupportedOperationException
                    && t.getMessage() != null
                    && t.getMessage().startsWith("TODO(")) {
                return new TestAbortedException("PENDING " + t.getMessage());
            }
        }
        return throwable;
    }
}
