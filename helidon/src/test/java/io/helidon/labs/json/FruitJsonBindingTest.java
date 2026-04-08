package io.helidon.labs.json;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import io.helidon.labs.dto.AddressDto;
import io.helidon.labs.dto.FruitCreateRequest;
import io.helidon.labs.dto.FruitDto;
import io.helidon.labs.dto.StoreDto;
import io.helidon.labs.dto.StoreFruitPriceDto;
import io.helidon.json.binding.JsonBinding;
import java.util.List;
import org.junit.jupiter.api.Test;

class FruitJsonBindingTest {

  private static final JsonBinding JSON_BINDING = JsonBinding.create();

  @Test
  void serializesFruitDtoWithHelidonJsonBinding() {
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

    String json = JSON_BINDING.serialize(fruit);

    assertThat(json, containsString("\"name\":\"Apple\""));
    assertThat(json, containsString("\"storePrices\":[{"));
    assertThat(json, containsString("\"store\":{\"id\":5"));
    assertThat(json, containsString("\"price\":1.29"));
  }

  @Test
  void deserializesFruitCreateRequestWithHelidonJsonBinding() {
    FruitCreateRequest request = JSON_BINDING.deserialize(
      """
      {
        "name": "Banana",
        "description": "Curved yellow fruit"
      }
      """,
      FruitCreateRequest.class
    );

    assertThat(request.name(), is("Banana"));
    assertThat(request.description(), is("Curved yellow fruit"));
  }
}
