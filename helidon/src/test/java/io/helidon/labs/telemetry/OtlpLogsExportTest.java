package io.helidon.labs.telemetry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingLogRecordExporter;
import java.time.Duration;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

@ServerTest
class OtlpLogsExportTest {

  private final Http1Client client;
  private final OpenTelemetry openTelemetry;

  OtlpLogsExportTest(Http1Client client, OpenTelemetry openTelemetry) {
    this.client = client;
    this.openTelemetry = openTelemetry;
  }

  @Test
  void directOpenTelemetryLogsAreExportedViaOtlp() {
    try (
      TestLogHandler handler = TestLogHandler.create(
        Logger.getLogger(OtlpJsonLoggingLogRecordExporter.class.getName())
      )
    ) {
      openTelemetry
        .getLogsBridge()
        .get("io.helidon.labs.test")
        .logRecordBuilder()
        .setSeverity(io.opentelemetry.api.logs.Severity.WARN)
        .setSeverityText("WARNING")
        .setBody("direct otel warning")
        .emit();

      String exported = handler.awaitContains(
        Duration.ofSeconds(5),
        "\"body\":{\"stringValue\":\"direct otel warning\"}",
        "\"severityText\":\"WARNING\""
      );

      assertThat(
        exported,
        containsString("\"body\":{\"stringValue\":\"direct otel warning\"}")
      );
      assertThat(exported, containsString("\"severityText\":\"WARNING\""));
    }
  }

  @Test
  void warningLogsAreExportedViaOtlp() {
    try (
      TestLogHandler handler = TestLogHandler.create(
        Logger.getLogger(OtlpJsonLoggingLogRecordExporter.class.getName())
      )
    ) {
      var response = client.get("/fruits/Missing").request(String.class);

      assertThat(response.status(), is(Status.NOT_FOUND_404));

      String exported = handler.awaitContains(
        Duration.ofSeconds(5),
        "\"body\":{\"stringValue\":\"Fruit not found\"}",
        "\"severityText\":\"WARNING\"",
        "\"logger.name\",\"value\":{\"stringValue\":\"io.helidon.labs.resource.FruitResource\"}"
      );

      assertThat(exported, containsString("\"body\":{\"stringValue\":\"Fruit not found\"}"));
      assertThat(exported, containsString("\"severityText\":\"WARNING\""));
      assertThat(
        exported,
        containsString(
          "\"logger.name\",\"value\":{\"stringValue\":\"io.helidon.labs.resource.FruitResource\"}"
        )
      );
    }
  }
}
