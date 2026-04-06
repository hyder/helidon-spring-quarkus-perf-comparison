package io.helidon.labs.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.helidon.labs.json.StoreFruitPriceDtoSerializer;

@JsonSerialize(using = StoreFruitPriceDtoSerializer.class)
public record StoreFruitPriceDto(StoreDto store, float price) {}
