package io.helidon.labs.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.helidon.labs.json.FruitDtoSerializer;
import java.util.List;

@JsonSerialize(using = FruitDtoSerializer.class)
public record FruitDto(
  Long id,
  String name,
  String description,
  List<StoreFruitPriceDto> storePrices
) {}
