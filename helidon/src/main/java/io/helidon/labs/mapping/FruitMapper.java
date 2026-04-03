package io.helidon.labs.mapping;

import io.helidon.labs.dto.AddressDto;
import io.helidon.labs.dto.FruitCreateRequest;
import io.helidon.labs.dto.FruitDto;
import io.helidon.labs.dto.FruitListRow;
import io.helidon.labs.dto.StoreDto;
import io.helidon.labs.dto.StoreFruitPriceDto;
import io.helidon.labs.model.Address;
import io.helidon.labs.model.Fruit;
import io.helidon.labs.model.Store;
import io.helidon.labs.model.StoreFruitPrice;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FruitMapper {

  private FruitMapper() {}

  public static FruitDto toDto(Fruit fruit) {
    return new FruitDto(
      fruit.getId(),
      fruit.getName(),
      fruit.getDescription(),
      fruit.getStorePrices().stream().map(FruitMapper::toDto).toList()
    );
  }

  public static List<FruitDto> toDtos(List<FruitListRow> rows) {
    Map<Long, FruitAccumulator> fruits = new LinkedHashMap<>();

    for (FruitListRow row : rows) {
      FruitAccumulator fruit = fruits.computeIfAbsent(row.fruitId(), ignored ->
        new FruitAccumulator(
          row.fruitId(),
          row.fruitName(),
          row.fruitDescription()
        )
      );

      if (row.storeId() != null) {
        fruit
          .storePrices()
          .add(
            new StoreFruitPriceDto(
              new StoreDto(
                row.storeId(),
                row.storeName(),
                row.storeCurrency(),
                new AddressDto(
                  row.storeAddress(),
                  row.storeCity(),
                  row.storeCountry()
                )
              ),
              row.price().floatValue()
            )
          );
      }
    }

    return fruits
      .values()
      .stream()
      .map(it ->
        new FruitDto(
          it.id(),
          it.name(),
          it.description(),
          it
            .storePrices()
            .stream()
            .sorted(Comparator.comparing(price -> price.store().name()))
            .toList()
        )
      )
      .toList();
  }

  public static Fruit toEntity(FruitCreateRequest request) {
    Fruit fruit = new Fruit();
    fruit.setName(request.name());
    fruit.setDescription(request.description());
    return fruit;
  }

  private static StoreFruitPriceDto toDto(StoreFruitPrice storeFruitPrice) {
    return new StoreFruitPriceDto(
      toDto(storeFruitPrice.getStore()),
      storeFruitPrice.getPrice().floatValue()
    );
  }

  private static StoreDto toDto(Store store) {
    return new StoreDto(
      store.getId(),
      store.getName(),
      store.getCurrency(),
      toDto(store.getAddress())
    );
  }

  private static AddressDto toDto(Address address) {
    return new AddressDto(
      address.getAddress(),
      address.getCity(),
      address.getCountry()
    );
  }

  private record FruitAccumulator(
    Long id,
    String name,
    String description,
    List<StoreFruitPriceDto> storePrices
  ) {
    private FruitAccumulator(Long id, String name, String description) {
      this(id, name, description, new ArrayList<>());
    }
  }
}
