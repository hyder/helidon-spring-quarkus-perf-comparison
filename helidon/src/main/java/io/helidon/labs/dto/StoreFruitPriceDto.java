package io.helidon.labs.dto;

import io.helidon.json.binding.Json;

@Json.Entity
public record StoreFruitPriceDto(StoreDto store, float price) {}
