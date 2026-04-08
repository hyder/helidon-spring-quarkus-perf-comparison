package io.helidon.labs.telemetry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.List;
import org.junit.jupiter.api.Test;

@ServerTest
class DeclarativeTracingTest {

  private final Http1Client client;
  private final TestTracerFactory tracerFactory;

  DeclarativeTracingTest(Http1Client client, TestTracerFactory tracerFactory) {
    this.client = client;
    this.tracerFactory = tracerFactory;
  }

  @Test
  void fruitEndpointCreatesDeclarativeChildSpan() {
    var response = client.get("/fruits/Apple").request(String.class);

    assertThat(response.status(), is(Status.OK_200));

    List<SpanData> spans = tracerFactory.exporter().awaitSpanData(3);
    tracerFactory.exporter().clear();

    SpanData httpRequest = spanNamed(spans, "HTTP Request");
    SpanData contentWrite = spanNamed(spans, "content-write");
    SpanData endpoint = spanNamed(spans, "FruitResource.fruit");

    assertThat(httpRequest, notNullValue());
    assertThat(contentWrite, notNullValue());
    assertThat(endpoint, notNullValue());
    assertThat(
      response.headers().first(TraceIdResponseFeature.TRACE_ID_HEADER).orElse(null),
      is(httpRequest.getTraceId())
    );
    assertThat(contentWrite.getTraceId(), is(httpRequest.getTraceId()));
    assertThat(contentWrite.getParentSpanId(), is(httpRequest.getSpanId()));
    assertThat(endpoint.getTraceId(), is(httpRequest.getTraceId()));
    assertThat(endpoint.getParentSpanId(), is(httpRequest.getSpanId()));
  }

  @Test
  void listEndpointStillReturnsPayloadWhileTracingSpanIsEmitted() {
    var response = client.get("/fruits").request(String.class);

    assertThat(response.status(), is(Status.OK_200));

    List<SpanData> spans = tracerFactory.exporter().awaitSpanData(3);
    tracerFactory.exporter().clear();

    assertThat(spans, hasSize(3));
    assertThat(spanNamed(spans, "FruitResource.all"), notNullValue());
  }

  private static SpanData spanNamed(List<SpanData> spans, String name) {
    return spans
      .stream()
      .filter(span -> span.getName().equals(name))
      .findFirst()
      .orElse(null);
  }
}
