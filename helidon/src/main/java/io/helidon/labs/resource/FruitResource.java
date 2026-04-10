package io.helidon.labs.resource;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.BadRequestException;
import io.helidon.http.Http;
import io.helidon.http.HttpException;
import io.helidon.http.NotFoundException;
import io.helidon.metrics.api.Metrics;
import io.helidon.metrics.api.Meter;
import io.helidon.labs.dto.FruitCreateRequest;
import io.helidon.labs.dto.FruitDto;
import io.helidon.labs.mapping.FruitMapper;
import io.helidon.labs.model.Fruit;
import io.helidon.labs.repository.FruitRepository;
import io.helidon.service.registry.Service;
import io.helidon.tracing.Span;
import io.helidon.tracing.Tracing;
import io.helidon.transaction.Tx;
import io.helidon.webserver.http.RestServer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.context.Context;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("deprecation")
@Http.Path("/fruits")
@Service.Singleton
@RestServer.Endpoint
public class FruitResource {

  private static final Logger LOGGER = Logger.getLogger(FruitResource.class.getName());
  private static final String FRUIT_NAME_ATTRIBUTE = "fruit.name";
  private static final String RESOURCE_ATTRIBUTE = "resource";
  private static final String OPERATION_ATTRIBUTE = "operation";
  private static final String RESPONSE_CODE_ATTRIBUTE = "response.code";

  private final FruitRepository fruitRepository;
  private final io.opentelemetry.api.logs.Logger otelLogger;
  // private final LongCounter fruitRequests;
  // private final DoubleHistogram fruitRequestDuration;

  @Service.Inject
  public FruitResource(FruitRepository fruitRepository, OpenTelemetry openTelemetry) {
    this.fruitRepository = fruitRepository;
    this.otelLogger = openTelemetry.getLogsBridge().get(FruitResource.class.getName());
    // var meter = openTelemetry.meterBuilder("io.helidon.labs.fruits").build();
    // this.fruitRequests = meter
    //   .counterBuilder("fruit.requests")
    //   .setDescription("Fruit resource requests")
    //   .build();
    // this.fruitRequestDuration = meter
    //   .histogramBuilder("fruit.request.duration")
    //   .setDescription("Fruit resource request duration")
    //   .setUnit("s")
    //   .build();
  }

  @Http.GET
  @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
  @Tracing.Traced("FruitResource.all")
  @Metrics.Counted(absoluteName=true, value="fruit.requests", unit=Meter.BaseUnits.SECONDS)
  @Metrics.Timed(absoluteName=true, value="fruit.request.duration")
  List<FruitDto> all() {
    long start = System.nanoTime();
    int statusCode = 200;
    emitLog(Level.FINE, "Listing fruits");
    try {
      return FruitMapper.toDtos(
        fruitRepository.listSummaryOrderByName()
      );
    } catch (RuntimeException e) {
      statusCode = statusCode(e);
      throw e;
    } finally {
      //recordOtelMetrics("all", start, statusCode);
    }
  }

  @Http.GET
  @Http.Path("/{name}")
  @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
  @Tracing.Traced("FruitResource.fruit")
  @Metrics.Counted(absoluteName=true, value="fruit.requests")
  @Metrics.Timed(absoluteName=true, value="fruit.request.duration")
  FruitDto fruit(@Http.PathParam("name") String name) {
    long start = System.nanoTime();
    int statusCode = 200;
    emitLog(Level.FINE, "Fetching fruit");
    try {
      return fruitRepository
        .findDetailedByName(name)
        .map(FruitMapper::toDto)
        .orElseThrow(() -> {
          emitLog(Level.WARNING, "Fruit not found");
          return new NotFoundException("Fruit not found: " + name);
        });
    } catch (RuntimeException e) {
      statusCode = statusCode(e);
      throw e;
    } finally {
      //recordOtelMetrics("fruit", start, statusCode);
    }
  }

  @Http.POST
  @Http.Consumes(MediaTypes.APPLICATION_JSON_VALUE)
  @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
  @Tx.Required
  @Tracing.Traced("FruitResource.insert")
  @Metrics.Counted(absoluteName=true, value="fruit.requests")
  @Metrics.Timed(absoluteName=true, value="fruit.request.duration")
  FruitDto insert(@Http.Entity FruitCreateRequest request) {
    long start = System.nanoTime();
    int statusCode = 200;
    try {
      FruitCreateRequest normalizedRequest = normalize(request);
      Span.current().ifPresent(span ->
        span.tag(FRUIT_NAME_ATTRIBUTE, normalizedRequest.name())
      );
      emitLog(Level.FINE, "Creating fruit");
      fruitRepository
        .findByName(normalizedRequest.name())
        .ifPresent(existing -> {
          emitLog(Level.WARNING, "Fruit already exists");
          throw new BadRequestException(
            "Fruit already exists: " + normalizedRequest.name()
          );
        });
      Fruit fruit = fruitRepository.insert(
        FruitMapper.toEntity(normalizedRequest)
      );
      return FruitMapper.toDto(fruit);
    } catch (RuntimeException e) {
      statusCode = statusCode(e);
      throw e;
    } finally {
      //recordOtelMetrics("insert", start, statusCode);
    }
  }

  private void emitLog(Level level, String body) {
    LOGGER.log(level, body);
    otelLogger
      .logRecordBuilder()
      .setContext(Context.current())
      .setSeverity(severity(level))
      .setSeverityText(level.getName())
      .setBody(body)
      .setAttribute("logger.name", LOGGER.getName())
      .setAttribute("code.namespace", FruitResource.class.getName())
      .setAttribute("code.function", "emitLog")
      .emit();
  }

  // private void recordOtelMetrics(String operation, long startNanos, int statusCode) {
  //   Attributes attributes = Attributes.of(
  //     AttributeKey.stringKey(OPERATION_ATTRIBUTE), operation,
  //     AttributeKey.stringKey(RESOURCE_ATTRIBUTE), "fruits",
  //     AttributeKey.longKey(RESPONSE_CODE_ATTRIBUTE), (long) statusCode
  //   );
  //   fruitRequests.add(1, attributes);
  //   fruitRequestDuration.record(
  //     (System.nanoTime() - startNanos) / 1_000_000_000.0,
  //     attributes
  //   );
  // }

  private static int statusCode(RuntimeException e) {
    if (e instanceof HttpException httpException) {
      return httpException.status().code();
    }
    return 500;
  }

  private static FruitCreateRequest normalize(FruitCreateRequest request) {
    if (request == null) {
      throw new BadRequestException("Fruit payload is required.");
    }
    String name = request.name() == null ? null : request.name().trim();
    if (name == null || name.isEmpty()) {
      throw new BadRequestException("Fruit name is required.");
    }
    String description =
      request.description() == null ? null : request.description().trim();
    return new FruitCreateRequest(name, description);
  }

  private static Severity severity(Level level) {
    int value = level.intValue();
    if (value >= Level.SEVERE.intValue()) {
      return Severity.ERROR;
    }
    if (value >= Level.WARNING.intValue()) {
      return Severity.WARN;
    }
    if (value >= Level.INFO.intValue()) {
      return Severity.INFO;
    }
    if (value >= Level.FINE.intValue()) {
      return Severity.DEBUG;
    }
    return Severity.TRACE;
  }
}
