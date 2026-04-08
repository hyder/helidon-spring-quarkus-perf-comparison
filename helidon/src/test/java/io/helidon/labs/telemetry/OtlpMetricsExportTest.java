package io.helidon.labs.telemetry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingMetricExporter;
import java.time.Duration;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

@ServerTest
class OtlpMetricsExportTest {

  private final Http1Client client;

  OtlpMetricsExportTest(Http1Client client) {
    this.client = client;
  }

  @Test
  void fruitMetricsAreExportedViaOtlp() {
    try (
      TestLogHandler handler = TestLogHandler.create(
        Logger.getLogger(OtlpJsonLoggingMetricExporter.class.getName())
      )
    ) {
      var listResponse = client.get("/fruits").request(String.class);
      var fruitResponse = client.get("/fruits/Apple").request(String.class);
      var missingFruitResponse = client.get("/fruits/Missing").request(String.class);

      assertThat(listResponse.status(), org.hamcrest.Matchers.is(Status.OK_200));
      assertThat(fruitResponse.status(), org.hamcrest.Matchers.is(Status.OK_200));
      assertThat(
        missingFruitResponse.status(),
        org.hamcrest.Matchers.is(Status.NOT_FOUND_404)
      );

      String exported = handler.awaitContains(
        Duration.ofSeconds(5),
        "\"name\":\"fruit.requests\"",
        "\"name\":\"fruit.request.duration\"",
        "\"value\":{\"stringValue\":\"all\"}",
        "\"value\":{\"stringValue\":\"fruit\"}",
        "\"key\":\"response.code\",\"value\":{\"intValue\":\"200\"}",
        "\"key\":\"response.code\",\"value\":{\"intValue\":\"404\"}"
      );

      assertThat(exported, containsString("\"name\":\"fruit.requests\""));
      assertThat(exported, containsString("\"name\":\"fruit.request.duration\""));
      assertThat(exported, containsString("\"value\":{\"stringValue\":\"all\"}"));
      assertThat(exported, containsString("\"value\":{\"stringValue\":\"fruit\"}"));
      assertThat(
        exported,
        containsString("\"key\":\"response.code\",\"value\":{\"intValue\":\"200\"}")
      );
      assertThat(
        exported,
        containsString("\"key\":\"response.code\",\"value\":{\"intValue\":\"404\"}")
      );
    }
  }
}
