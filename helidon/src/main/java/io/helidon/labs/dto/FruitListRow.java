package io.helidon.labs.dto;

import java.math.BigDecimal;

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
