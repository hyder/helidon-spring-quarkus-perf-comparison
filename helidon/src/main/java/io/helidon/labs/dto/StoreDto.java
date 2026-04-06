package io.helidon.labs.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.helidon.labs.json.StoreDtoSerializer;

@JsonSerialize(using = StoreDtoSerializer.class)
public record StoreDto(
  Long id,
  String name,
  String currency,
  AddressDto address
) {}
