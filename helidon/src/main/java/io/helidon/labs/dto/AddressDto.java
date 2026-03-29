package io.helidon.labs.dto;

import io.helidon.json.binding.Json;

@Json.Entity
public record AddressDto(String address, String city, String country) {}
