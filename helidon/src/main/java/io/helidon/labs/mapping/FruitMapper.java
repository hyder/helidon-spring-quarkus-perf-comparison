package io.helidon.labs.mapping;

import java.util.Comparator;
import java.util.List;

import io.helidon.labs.model.Address;
import io.helidon.labs.model.Fruit;
import io.helidon.labs.model.Store;
import io.helidon.labs.model.StoreFruitPrice;
import io.helidon.labs.dto.AddressDto;
import io.helidon.labs.dto.FruitCreateRequest;
import io.helidon.labs.dto.FruitDto;
import io.helidon.labs.dto.StoreDto;
import io.helidon.labs.dto.StoreFruitPriceDto;

public final class FruitMapper {

    private FruitMapper() {
    }

    public static FruitDto toDto(Fruit fruit) {
        return new FruitDto(
                fruit.getId(),
                fruit.getName(),
                fruit.getDescription(),
                fruit.getStorePrices().stream()
                        .sorted(Comparator.comparing(price -> price.getStore().getName()))
                        .map(FruitMapper::toDto)
                        .toList());
    }

    public static Fruit toEntity(FruitCreateRequest request) {
        Fruit fruit = new Fruit();
        fruit.setName(request.name());
        fruit.setDescription(request.description());
        return fruit;
    }

    private static StoreFruitPriceDto toDto(StoreFruitPrice storeFruitPrice) {
        return new StoreFruitPriceDto(toDto(storeFruitPrice.getStore()), storeFruitPrice.getPrice());
    }

    private static StoreDto toDto(Store store) {
        return new StoreDto(store.getId(), store.getName(), store.getCurrency(), toDto(store.getAddress()));
    }

    private static AddressDto toDto(Address address) {
        return new AddressDto(address.getAddress(), address.getCity(), address.getCountry());
    }
}
