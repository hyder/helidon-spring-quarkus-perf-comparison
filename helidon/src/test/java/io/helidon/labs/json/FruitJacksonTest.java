package io.helidon.labs.json;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.helidon.labs.dto.AddressDto;
import io.helidon.labs.dto.FruitCreateRequest;
import io.helidon.labs.dto.FruitDto;
import io.helidon.labs.dto.StoreDto;
import io.helidon.labs.dto.StoreFruitPriceDto;
import java.util.List;
import org.junit.jupiter.api.Test;

class FruitJacksonTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  void serializesFruitDtoWithExplicitJacksonSerializer() throws Exception {
    FruitDto fruit = new FruitDto(
      1L,
      "Apple",
      "Hearty fruit",
      List.of(
        new StoreFruitPriceDto(
          new StoreDto(
            5L,
            "Downtown Market",
            "USD",
            new AddressDto("123 Main St", "Springfield", "USA")
          ),
          1.29f
        )
      )
    );

    String json = OBJECT_MAPPER.writeValueAsString(fruit);

    assertThat(json, containsString("\"name\":\"Apple\""));
    assertThat(json, containsString("\"storePrices\":[{"));
    assertThat(json, containsString("\"store\":{\"id\":5"));
    assertThat(json, containsString("\"price\":1.29"));
  }

  @Test
  void deserializesFruitCreateRequestWithExplicitJacksonDeserializer()
    throws Exception {
    FruitCreateRequest request = OBJECT_MAPPER.readValue(
      """
      {
        "name": "Banana",
        "description": "Curved yellow fruit",
        "ignored": {
          "extra": true
        }
      }
      """,
      FruitCreateRequest.class
    );

    assertThat(request.name(), is("Banana"));
    assertThat(request.description(), is("Curved yellow fruit"));
  }
}
