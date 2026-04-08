package io.helidon.labs.telemetry;

import java.util.Map;
import java.util.function.Supplier;

import io.helidon.common.Weight;
import io.helidon.common.Weighted;
import io.helidon.service.registry.Service;
import io.helidon.tracing.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

@Service.Singleton
@Weight(Weighted.DEFAULT_WEIGHT + 100)
class TestTracerFactory implements Supplier<Tracer> {

  private final TestSpanExporter exporter = new TestSpanExporter();
  private final OpenTelemetrySdk openTelemetry = OpenTelemetrySdk
    .builder()
    .setTracerProvider(
      SdkTracerProvider
        .builder()
        .addSpanProcessor(SimpleSpanProcessor.create(exporter))
        .build()
    )
    .build();
  private final Tracer tracer =
    io.helidon.tracing.providers.opentelemetry.HelidonOpenTelemetry.create(
      openTelemetry,
      openTelemetry.getTracer("io.helidon.labs.test"),
      Map.of()
    );

  @Override
  public Tracer get() {
    return tracer;
  }

  TestSpanExporter exporter() {
    return exporter;
  }

  @Service.PreDestroy
  void shutdown() {
    openTelemetry.close();
  }
}
