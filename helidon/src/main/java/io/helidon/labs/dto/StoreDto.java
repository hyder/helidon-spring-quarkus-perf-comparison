package io.helidon.labs.dto;

import io.helidon.json.binding.Json;

@Json.Entity
public record StoreDto(
  Long id,
  String name,
  String currency,
  AddressDto address
) {}
