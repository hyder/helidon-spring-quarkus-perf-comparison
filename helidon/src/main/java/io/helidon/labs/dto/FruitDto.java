package io.helidon.labs.dto;

import java.util.List;

public record FruitDto(
  Long id,
  String name,
  String description,
  List<StoreFruitPriceDto> storePrices
) {}
