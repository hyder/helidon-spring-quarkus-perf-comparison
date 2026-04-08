package io.helidon.labs.telemetry;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class TestSpanExporter implements SpanExporter {

  private final List<SpanData> spanData = new CopyOnWriteArrayList<>();

  @Override
  public CompletableResultCode export(Collection<SpanData> spans) {
    spanData.addAll(spans);
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    spanData.clear();
    return CompletableResultCode.ofSuccess();
  }

  List<SpanData> awaitSpanData(int expectedCount) {
    long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
    while (System.nanoTime() < deadline) {
      List<SpanData> snapshot = new ArrayList<>(spanData);
      if (snapshot.size() >= expectedCount) {
        return snapshot;
      }
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Interrupted while waiting for spans", e);
      }
    }
    return new ArrayList<>(spanData);
  }

  void clear() {
    spanData.clear();
  }
}
