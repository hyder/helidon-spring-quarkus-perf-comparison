package io.helidon.labs.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.helidon.labs.dto.FruitCreateRequest;
import java.io.IOException;

public class FruitCreateRequestDeserializer
  extends StdDeserializer<FruitCreateRequest> {

  public FruitCreateRequestDeserializer() {
    super(FruitCreateRequest.class);
  }

  @Override
  public FruitCreateRequest deserialize(
    JsonParser parser,
    DeserializationContext context
  ) throws IOException {
    JsonToken token = parser.currentToken();
    if (token == null) {
      token = parser.nextToken();
    }
    if (token != JsonToken.START_OBJECT) {
      return context.reportInputMismatch(
        FruitCreateRequest.class,
        "Expected a JSON object for FruitCreateRequest."
      );
    }

    String name = null;
    String description = null;
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      String fieldName = parser.currentName();
      parser.nextToken();
      switch (fieldName) {
        case "name" -> name = readNullableString(parser, context, fieldName);
        case "description" ->
          description = readNullableString(parser, context, fieldName);
        default -> parser.skipChildren();
      }
    }
    return new FruitCreateRequest(name, description);
  }

  private static String readNullableString(
    JsonParser parser,
    DeserializationContext context,
    String fieldName
  ) throws IOException {
    JsonToken token = parser.currentToken();
    if (token == JsonToken.VALUE_NULL) {
      return null;
    }
    if (!token.isScalarValue()) {
      return context.reportInputMismatch(
        String.class,
        "Expected field '%s' to contain a scalar value.",
        fieldName
      );
    }
    return parser.getValueAsString();
  }
}
