package io.helidon.labs.telemetry;

import io.helidon.http.Header;
import io.helidon.http.HttpException;
import io.helidon.service.registry.Service;
import io.helidon.webserver.http.Filter;
import io.helidon.webserver.http.FilterChain;
import io.helidon.webserver.http.HttpFeature;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.RoutingRequest;
import io.helidon.webserver.http.RoutingResponse;

@Service.Singleton
public class HttpExceptionResponseFeature implements HttpFeature {

  @Override
  public void setup(HttpRouting.Builder routing) {
    routing.addFilter(new HttpExceptionResponseFilter());
  }

  static final class HttpExceptionResponseFilter implements Filter {

    @Override
    public void filter(
      FilterChain chain,
      RoutingRequest request,
      RoutingResponse response
    ) {
      try {
        chain.proceed();
      } catch (HttpException httpException) {
        if (response.isSent()) {
          throw httpException;
        }

        response.status(httpException.status());
        for (Header header : httpException.headers()) {
          response.header(header);
        }
        response.send(httpException.getMessage());
      }
    }
  }
}
