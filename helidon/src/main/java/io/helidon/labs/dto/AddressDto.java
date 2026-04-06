package io.helidon.labs.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.helidon.labs.json.AddressDtoSerializer;

@JsonSerialize(using = AddressDtoSerializer.class)
public record AddressDto(String address, String city, String country) {}
