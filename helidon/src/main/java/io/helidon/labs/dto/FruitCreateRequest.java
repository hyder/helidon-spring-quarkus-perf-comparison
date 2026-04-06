package io.helidon.labs.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.helidon.labs.json.FruitCreateRequestDeserializer;

@JsonDeserialize(using = FruitCreateRequestDeserializer.class)
public record FruitCreateRequest(String name, String description) {}
