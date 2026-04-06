package io.helidon.labs.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.helidon.labs.dto.StoreFruitPriceDto;
import java.io.IOException;

public class StoreFruitPriceDtoSerializer
  extends StdSerializer<StoreFruitPriceDto> {

  public StoreFruitPriceDtoSerializer() {
    super(StoreFruitPriceDto.class);
  }

  @Override
  public void serialize(
    StoreFruitPriceDto value,
    JsonGenerator gen,
    SerializerProvider provider
  ) throws IOException {
    write(value, gen);
  }

  static void write(StoreFruitPriceDto value, JsonGenerator gen)
    throws IOException {
    if (value == null) {
      gen.writeNull();
      return;
    }
    gen.writeStartObject();
    gen.writeFieldName("store");
    StoreDtoSerializer.write(value.store(), gen);
    gen.writeNumberField("price", value.price());
    gen.writeEndObject();
  }
}
