package io.helidon.labs.telemetry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingMetricExporter;
import java.time.Duration;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

@ServerTest
class DeclarativeMetricsTest {

  private final Http1Client client;

  DeclarativeMetricsTest(Http1Client client) {
    this.client = client;
  }

  @Test
  void fruitEndpointsPublishOpenTelemetryHttpMetrics() {
    try (
      TestLogHandler handler = TestLogHandler.create(
        Logger.getLogger(OtlpJsonLoggingMetricExporter.class.getName())
      )
    ) {
      var listResponse = client.get("/fruits").request(String.class);
      var fruitResponse = client.get("/fruits/Apple").request(String.class);

      assertThat(listResponse.status(), is(Status.OK_200));
      assertThat(fruitResponse.status(), is(Status.OK_200));

      String exported = handler.awaitContains(
        Duration.ofSeconds(5),
        "\"name\":\"http.server.request.duration\"",
        "\"value\":{\"stringValue\":\"/fruits/\"}",
        "\"value\":{\"stringValue\":\"/fruits/{name}\"}"
      );

      assertThat(exported, containsString("\"name\":\"http.server.request.duration\""));
      assertThat(exported, containsString("\"value\":{\"stringValue\":\"/fruits/\"}"));
      assertThat(exported, containsString("\"value\":{\"stringValue\":\"/fruits/{name}\"}"));
    }
  }
}
