package io.helidon.labs.dto;

import java.math.BigDecimal;

public record StoreFruitPriceDto(StoreDto store, BigDecimal price) {
}
