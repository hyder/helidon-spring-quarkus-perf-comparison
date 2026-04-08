package io.helidon.labs.telemetry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

class TestLogHandler extends Handler implements AutoCloseable {

  private final List<String> messages = Collections.synchronizedList(new ArrayList<>());
  private final Logger logger;

  private TestLogHandler(Logger logger) {
    this.logger = logger;
    logger.addHandler(this);
  }

  static TestLogHandler create(Logger logger) {
    return new TestLogHandler(logger);
  }

  @Override
  public void publish(LogRecord record) {
    messages.add(record.getMessage());
  }

  @Override
  public void flush() {
  }

  @Override
  public void close() {
    logger.removeHandler(this);
  }

  String awaitContains(Duration timeout, String... expectedSnippets) {
    long deadlineNanos = System.nanoTime() + timeout.toNanos();
    while (System.nanoTime() < deadlineNanos) {
      String snapshot = String.join("\n", List.copyOf(messages));
      boolean matches = true;
      for (String snippet : expectedSnippets) {
        if (!snapshot.contains(snippet)) {
          matches = false;
          break;
        }
      }
      if (matches) {
        return snapshot;
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Interrupted while waiting for log messages", e);
      }
    }
    return String.join("\n", List.copyOf(messages));
  }
}
