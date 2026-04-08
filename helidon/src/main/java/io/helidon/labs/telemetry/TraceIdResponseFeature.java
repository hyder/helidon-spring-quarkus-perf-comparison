package io.helidon.labs.telemetry;

import java.util.Optional;

import io.helidon.http.HeaderName;
import io.helidon.http.HeaderNames;
import io.helidon.service.registry.Service;
import io.helidon.tracing.Span;
import io.helidon.tracing.SpanContext;
import io.helidon.webserver.http.Filter;
import io.helidon.webserver.http.FilterChain;
import io.helidon.webserver.http.HttpFeature;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.RoutingRequest;
import io.helidon.webserver.http.RoutingResponse;

@Service.Singleton
public class TraceIdResponseFeature implements HttpFeature {

  static final HeaderName TRACE_ID_HEADER = HeaderNames.create("X-Trace-Id");

  @Override
  public void setup(HttpRouting.Builder routing) {
    routing.addFilter(new TraceIdFilter());
  }

  static final class TraceIdFilter implements Filter {

    @Override
    public void filter(
      FilterChain chain,
      RoutingRequest request,
      RoutingResponse response
    ) {
      String traceId = currentTraceId().orElse(null);
      response.beforeSend(() ->
        currentTraceId()
          .or(() -> Optional.ofNullable(traceId))
          .ifPresent(value -> response.header(TRACE_ID_HEADER, value))
      );
      chain.proceed();
    }

    private static Optional<String> currentTraceId() {
      return Span.current()
        .map(Span::context)
        .map(SpanContext::traceId)
        .filter(traceId -> !traceId.isBlank());
    }
  }
}
