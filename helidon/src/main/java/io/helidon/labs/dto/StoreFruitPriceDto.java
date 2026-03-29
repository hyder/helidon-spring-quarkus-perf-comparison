package io.helidon.labs.dto;

import io.helidon.json.binding.Json;
import java.math.BigDecimal;

@Json.Entity
public record StoreFruitPriceDto(StoreDto store, BigDecimal price) {}
