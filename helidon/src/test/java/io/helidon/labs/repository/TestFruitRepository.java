package io.helidon.labs.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import io.helidon.common.Weight;
import io.helidon.common.Weighted;
import io.helidon.labs.dto.FruitListRow;
import io.helidon.labs.model.Address;
import io.helidon.labs.model.Fruit;
import io.helidon.labs.model.Store;
import io.helidon.labs.model.StoreFruitPrice;
import io.helidon.service.registry.Service;

@Service.Singleton
@Weight(Weighted.DEFAULT_WEIGHT + 100)
class TestFruitRepository implements FruitRepository {

  private final Map<Long, Fruit> fruitsById = new LinkedHashMap<>();
  private final AtomicLong nextId = new AtomicLong(2);

  TestFruitRepository() {
    Fruit apple = new Fruit(1L, "Apple", "Hearty fruit");
    Store downtown = new Store(
      1L,
      "Downtown Market",
      new Address("123 Main St", "Springfield", "USA"),
      "USD"
    );
    apple.setStorePrices(
      new ArrayList<>(List.of(new StoreFruitPrice(downtown, apple, new BigDecimal("1.29"))))
    );
    fruitsById.put(apple.getId(), apple);
  }

  @Override
  public List<FruitListRow> listSummaryOrderByName() {
    return fruitsById
      .values()
      .stream()
      .sorted(Comparator.comparing(Fruit::getName))
      .flatMap(fruit ->
        fruit
          .getStorePrices()
          .stream()
          .map(price ->
            new FruitListRow(
              fruit.getId(),
              fruit.getName(),
              fruit.getDescription(),
              price.getStore().getId(),
              price.getStore().getName(),
              price.getStore().getCurrency(),
              price.getStore().getAddress().getAddress(),
              price.getStore().getAddress().getCity(),
              price.getStore().getAddress().getCountry(),
              price.getPrice()
            )
          )
      )
      .toList();
  }

  @Override
  public Optional<Fruit> findDetailedByName(String name) {
    return findByName(name).map(TestFruitRepository::copyFruit);
  }

  @Override
  public Optional<Fruit> findByName(String name) {
    return fruitsById
      .values()
      .stream()
      .filter(fruit -> fruit.getName().equals(name))
      .findFirst()
      .map(TestFruitRepository::copyFruit);
  }

  @Override
  public <T extends Fruit> T insert(T entity) {
    entity.setId(nextId.getAndIncrement());
    if (entity.getStorePrices() == null) {
      entity.setStorePrices(new ArrayList<>());
    }
    fruitsById.put(entity.getId(), entity);
    return entity;
  }

  @Override
  public <T extends Fruit> Iterable<T> insertAll(Iterable<T> entities) {
    List<T> inserted = new ArrayList<>();
    entities.forEach(entity -> inserted.add(insert(entity)));
    return inserted;
  }

  @Override
  public <T extends Fruit> T update(T entity) {
    fruitsById.put(entity.getId(), entity);
    return entity;
  }

  @Override
  public <T extends Fruit> Iterable<T> updateAll(Iterable<T> entities) {
    List<T> updated = new ArrayList<>();
    entities.forEach(entity -> updated.add(update(entity)));
    return updated;
  }

  @Override
  public <T extends Fruit> T save(T entity) {
    return entity.getId() == null ? insert(entity) : update(entity);
  }

  @Override
  public <T extends Fruit> Iterable<T> saveAll(Iterable<T> entities) {
    List<T> saved = new ArrayList<>();
    entities.forEach(entity -> saved.add(save(entity)));
    return saved;
  }

  @Override
  public Optional<Fruit> findById(Long id) {
    return Optional.ofNullable(fruitsById.get(id)).map(TestFruitRepository::copyFruit);
  }

  @Override
  public boolean existsById(Long id) {
    return fruitsById.containsKey(id);
  }

  @Override
  public Stream<Fruit> findAll() {
    return fruitsById.values().stream().map(TestFruitRepository::copyFruit);
  }

  @Override
  public long count() {
    return fruitsById.size();
  }

  @Override
  public long deleteById(Long id) {
    return fruitsById.remove(id) == null ? 0 : 1;
  }

  @Override
  public void delete(Fruit entity) {
    if (entity != null && entity.getId() != null) {
      fruitsById.remove(entity.getId());
    }
  }

  @Override
  public void deleteAll(Iterable<? extends Fruit> entities) {
    entities.forEach(this::delete);
  }

  @Override
  public long deleteAll() {
    long count = fruitsById.size();
    fruitsById.clear();
    return count;
  }

  private static Fruit copyFruit(Fruit source) {
    Fruit copy = new Fruit(source.getId(), source.getName(), source.getDescription());
    copy.setStorePrices(
      source
        .getStorePrices()
        .stream()
        .map(price -> {
          Store store = new Store(
            price.getStore().getId(),
            price.getStore().getName(),
            new Address(
              price.getStore().getAddress().getAddress(),
              price.getStore().getAddress().getCity(),
              price.getStore().getAddress().getCountry()
            ),
            price.getStore().getCurrency()
          );
          return new StoreFruitPrice(store, copy, price.getPrice());
        })
        .toList()
    );
    return copy;
  }
}
