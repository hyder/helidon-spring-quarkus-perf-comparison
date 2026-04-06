package io.helidon.labs.resource;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.BadRequestException;
import io.helidon.http.Http;
import io.helidon.http.NotFoundException;
import io.helidon.labs.dto.FruitCreateRequest;
import io.helidon.labs.dto.FruitDto;
import io.helidon.labs.mapping.FruitMapper;
import io.helidon.labs.model.Fruit;
import io.helidon.labs.repository.FruitRepository;
import io.helidon.service.registry.Service;
import io.helidon.transaction.Tx;
import io.helidon.webserver.http.RestServer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import java.util.List;

@SuppressWarnings("deprecation")
@Http.Path("/fruits")
@Service.Singleton
@RestServer.Endpoint
public class FruitResource {

  private static final AttributeKey<String> OPERATION_ATTRIBUTE = AttributeKey.stringKey(
    "fruit.operation"
  );
  private static final AttributeKey<String> FRUIT_NAME_ATTRIBUTE = AttributeKey.stringKey(
    "fruit.name"
  );

  private final FruitRepository fruitRepository;
  private final LongCounter requestCounter;
  private final Tracer tracer;
  private final Logger logger;

  @Service.Inject
  public FruitResource(
    FruitRepository fruitRepository,
    OpenTelemetry openTelemetry
  ) {
    this.fruitRepository = fruitRepository;
    Meter meter = openTelemetry.getMeter("io.helidon.labs.fruit-resource");
    this.requestCounter = meter
      .counterBuilder("fruit.requests")
      .setDescription("Fruit API requests served")
      .setUnit("{request}")
      .build();
    this.tracer = openTelemetry.getTracer("io.helidon.labs.fruit-resource");
    this.logger = openTelemetry.getLogsBridge().get("io.helidon.labs.fruit-resource");
  }

  public FruitResource(FruitRepository fruitRepository) {
    this(fruitRepository, OpenTelemetry.noop());
  }

  @Http.GET
  @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
  List<FruitDto> all() {
    Span span = startSpan("FruitResource.all", "all");
    try (Scope ignored = span.makeCurrent()) {
      emitLog(Severity.INFO, "Listing fruits", Attributes.empty());
      countRequest("all");
      List<FruitDto> fruits = FruitMapper.toDtos(
        fruitRepository.listSummaryOrderByName()
      );
      span.setAttribute("fruit.result_count", fruits.size());
      return fruits;
    } finally {
      span.end();
    }
  }

  @Http.GET
  @Http.Path("/{name}")
  @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
  FruitDto fruit(@Http.PathParam("name") String name) {
    Span span = startSpan("FruitResource.fruit", "fruit");
    span.setAttribute(FRUIT_NAME_ATTRIBUTE, name);
    try (Scope ignored = span.makeCurrent()) {
      emitLog(
        Severity.INFO,
        "Fetching fruit",
        Attributes.of(FRUIT_NAME_ATTRIBUTE, name)
      );
      countRequest("fruit");
      return fruitRepository
        .findDetailedByName(name)
        .map(FruitMapper::toDto)
        .orElseThrow(() -> {
          emitLog(
            Severity.WARN,
            "Fruit not found",
            Attributes.of(FRUIT_NAME_ATTRIBUTE, name)
          );
          span.setAttribute("fruit.found", false);
          return new NotFoundException("Fruit not found: " + name);
        });
    } finally {
      span.end();
    }
  }

  @Http.POST
  @Http.Consumes(MediaTypes.APPLICATION_JSON_VALUE)
  @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
  @Tx.Required
  FruitDto insert(@Http.Entity FruitCreateRequest request) {
    Span span = startSpan("FruitResource.insert", "insert");
    try (Scope ignored = span.makeCurrent()) {
      countRequest("insert");
      FruitCreateRequest normalizedRequest = normalize(request);
      span.setAttribute(FRUIT_NAME_ATTRIBUTE, normalizedRequest.name());
      emitLog(
        Severity.INFO,
        "Creating fruit",
        Attributes.of(FRUIT_NAME_ATTRIBUTE, normalizedRequest.name())
      );
      fruitRepository
        .findByName(normalizedRequest.name())
        .ifPresent(existing -> {
          emitLog(
            Severity.WARN,
            "Fruit already exists",
            Attributes.of(FRUIT_NAME_ATTRIBUTE, normalizedRequest.name())
          );
          throw new BadRequestException(
            "Fruit already exists: " + normalizedRequest.name()
          );
        });
      Fruit fruit = fruitRepository.insert(
        FruitMapper.toEntity(normalizedRequest)
      );
      return FruitMapper.toDto(fruit);
    } finally {
      span.end();
    }
  }

  private void countRequest(String operation) {
    requestCounter.add(
      1,
      Attributes.of(OPERATION_ATTRIBUTE, operation)
    );
  }

  private Span startSpan(String spanName, String operation) {
    return tracer
      .spanBuilder(spanName)
      .setSpanKind(SpanKind.INTERNAL)
      .setAttribute(OPERATION_ATTRIBUTE, operation)
      .startSpan();
  }

  private void emitLog(Severity severity, String body, Attributes attributes) {
    logger
      .logRecordBuilder()
      .setSeverity(severity)
      .setBody(body)
      .setContext(io.opentelemetry.context.Context.current())
      .setAllAttributes(attributes)
      .emit();
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
}
