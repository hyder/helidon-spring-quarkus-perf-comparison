package io.helidon.labs.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.helidon.labs.dto.FruitDto;
import java.io.IOException;
import java.util.List;

public class FruitDtoSerializer extends StdSerializer<FruitDto> {

  public FruitDtoSerializer() {
    super(FruitDto.class);
  }

  @Override
  public void serialize(
    FruitDto value,
    JsonGenerator gen,
    SerializerProvider provider
  ) throws IOException {
    gen.writeStartObject();
    writeNumberField(gen, "id", value.id());
    gen.writeStringField("name", value.name());
    writeNullableStringField(gen, "description", value.description());
    gen.writeArrayFieldStart("storePrices");
    writeStorePrices(value.storePrices(), gen);
    gen.writeEndArray();
    gen.writeEndObject();
  }

  private static void writeStorePrices(
    List<io.helidon.labs.dto.StoreFruitPriceDto> storePrices,
    JsonGenerator gen
  ) throws IOException {
    if (storePrices == null) {
      return;
    }
    for (io.helidon.labs.dto.StoreFruitPriceDto storePrice : storePrices) {
      StoreFruitPriceDtoSerializer.write(storePrice, gen);
    }
  }

  private static void writeNumberField(
    JsonGenerator gen,
    String fieldName,
    Long value
  ) throws IOException {
    if (value == null) {
      gen.writeNullField(fieldName);
      return;
    }
    gen.writeNumberField(fieldName, value);
  }

  private static void writeNullableStringField(
    JsonGenerator gen,
    String fieldName,
    String value
  ) throws IOException {
    if (value == null) {
      gen.writeNullField(fieldName);
      return;
    }
    gen.writeStringField(fieldName, value);
  }
}
