package io.helidon.labs.dto;

import io.helidon.json.binding.Json;
import java.util.List;

@Json.Entity
public record FruitDto(
  Long id,
  String name,
  String description,
  List<StoreFruitPriceDto> storePrices
) {}
