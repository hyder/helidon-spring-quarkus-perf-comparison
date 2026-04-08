package io.helidon.labs.dto;

import io.helidon.json.binding.Json;
import java.math.BigDecimal;

@Json.Entity
public record FruitListRow(
    Long fruitId,
    String fruitName,
    String fruitDescription,
    Long storeId,
    String storeName,
    String storeCurrency,
    String storeAddress,
    String storeCity,
    String storeCountry,
    BigDecimal price
) {}
