package io.helidon.labs.dto;

public record StoreDto(Long id, String name, String currency, AddressDto address) {
}
