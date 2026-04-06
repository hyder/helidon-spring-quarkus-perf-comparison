package io.helidon.labs.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.helidon.labs.dto.AddressDto;
import java.io.IOException;

public class AddressDtoSerializer extends StdSerializer<AddressDto> {

  public AddressDtoSerializer() {
    super(AddressDto.class);
  }

  @Override
  public void serialize(
    AddressDto value,
    JsonGenerator gen,
    SerializerProvider provider
  ) throws IOException {
    write(value, gen);
  }

  static void write(AddressDto value, JsonGenerator gen) throws IOException {
    if (value == null) {
      gen.writeNull();
      return;
    }
    gen.writeStartObject();
    gen.writeStringField("address", value.address());
    gen.writeStringField("city", value.city());
    gen.writeStringField("country", value.country());
    gen.writeEndObject();
  }
}
