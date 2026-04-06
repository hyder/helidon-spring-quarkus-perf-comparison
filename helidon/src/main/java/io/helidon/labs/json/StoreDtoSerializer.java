package io.helidon.labs.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.helidon.labs.dto.StoreDto;
import java.io.IOException;

public class StoreDtoSerializer extends StdSerializer<StoreDto> {

  public StoreDtoSerializer() {
    super(StoreDto.class);
  }

  @Override
  public void serialize(
    StoreDto value,
    JsonGenerator gen,
    SerializerProvider provider
  ) throws IOException {
    write(value, gen);
  }

  static void write(StoreDto value, JsonGenerator gen) throws IOException {
    gen.writeStartObject();
    writeNumberField(gen, "id", value.id());
    gen.writeStringField("name", value.name());
    gen.writeStringField("currency", value.currency());
    gen.writeFieldName("address");
    AddressDtoSerializer.write(value.address(), gen);
    gen.writeEndObject();
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
}
