package io.helidon.labs.resource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.helidon.http.BadRequestException;
import io.helidon.http.NotFoundException;
import io.helidon.labs.dto.FruitCreateRequest;
import io.helidon.labs.dto.FruitDto;
import io.helidon.labs.dto.FruitListRow;
import io.helidon.labs.model.Address;
import io.helidon.labs.model.Fruit;
import io.helidon.labs.model.Store;
import io.helidon.labs.model.StoreFruitPrice;
import io.helidon.labs.repository.FruitRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class FruitResourceTest {

  @Test
  void allReturnsMappedFruitDetails() {
    Fruit apple = fruit(1L, "Apple", "Hearty fruit");
    Store downtown = store(1L, "Downtown Market");
    Store harbor = store(2L, "Harbor Grocer");
    apple.setStorePrices(
      new ArrayList<>(
        List.of(price(downtown, apple, "1.29"), price(harbor, apple, "1.39"))
      )
    );

    FruitResource resource = new FruitResource(
      new InMemoryFruitRepository(List.of(apple))
    );

    List<FruitDto> fruits = resource.all();

    assertThat(fruits, hasSize(1));
    assertThat(fruits.getFirst().name(), is("Apple"));
    assertThat(fruits.getFirst().storePrices(), hasSize(2));
    assertThat(fruits.getFirst().storePrices().getFirst().price(), is(1.29f));
    assertThat(
      fruits.getFirst().storePrices().getFirst().store().name(),
      is("Downtown Market")
    );
  }

  @Test
  void fruitThrowsNotFoundWhenMissing() {
    FruitResource resource = new FruitResource(
      new InMemoryFruitRepository(List.of())
    );

    assertThrows(NotFoundException.class, () -> resource.fruit("Missing"));
  }

  @Test
  void fruitReturnsStorePricesInRepositoryOrder() {
    Fruit apple = fruit(1L, "Apple", "Hearty fruit");
    Store harbor = store(2L, "Harbor Grocer");
    Store downtown = store(1L, "Downtown Market");
    apple.setStorePrices(
      new ArrayList<>(
        List.of(price(harbor, apple, "1.39"), price(downtown, apple, "1.29"))
      )
    );

    FruitResource resource = new FruitResource(
      new InMemoryFruitRepository(List.of(apple))
    );

    FruitDto fruit = resource.fruit("Apple");

    assertThat(fruit.storePrices(), hasSize(2));
    assertThat(
      fruit.storePrices().getFirst().store().name(),
      is("Downtown Market")
    );
    assertThat(fruit.storePrices().get(1).store().name(), is("Harbor Grocer"));
  }

  @Test
  void insertNormalizesInputAndRejectsDuplicates() {
    InMemoryFruitRepository repository = new InMemoryFruitRepository(
      List.of(fruit(1L, "Apple", "Hearty fruit"))
    );
    FruitResource resource = new FruitResource(repository);

    FruitDto created = resource.insert(
      new FruitCreateRequest("  Banana  ", "  Curved yellow fruit  ")
    );

    assertThat(created.name(), is("Banana"));
    assertThat(created.description(), is("Curved yellow fruit"));
    assertThat(created.id(), equalTo(2L));

    assertThrows(BadRequestException.class, () ->
      resource.insert(new FruitCreateRequest("Apple", "Duplicate"))
    );
  }

  private static Fruit fruit(Long id, String name, String description) {
    Fruit fruit = new Fruit(id, name, description);
    fruit.setStorePrices(new ArrayList<>());
    return fruit;
  }

  private static Store store(Long id, String name) {
    return new Store(
      id,
      name,
      new Address("123 Main St", "Springfield", "USA"),
      "USD"
    );
  }

  private static StoreFruitPrice price(
    Store store,
    Fruit fruit,
    String amount
  ) {
    return new StoreFruitPrice(store, fruit, new BigDecimal(amount));
  }

  private static final class InMemoryFruitRepository
    implements FruitRepository
  {

    private final Map<Long, Fruit> fruitsById = new LinkedHashMap<>();
    private final AtomicLong nextId;

    private InMemoryFruitRepository(List<Fruit> fruits) {
      fruits.forEach(fruit -> fruitsById.put(fruit.getId(), fruit));
      long maxId = fruits
        .stream()
        .map(Fruit::getId)
        .max(Comparator.naturalOrder())
        .orElse(0L);
      this.nextId = new AtomicLong(maxId + 1);
    }

    @Override
    public List<FruitListRow> listSummaryOrderByName() {
      return fruitsById
        .values()
        .stream()
        .sorted(Comparator.comparing(Fruit::getName))
        .flatMap(fruit -> {
          if (fruit.getStorePrices().isEmpty()) {
            return Stream.of(
              new FruitListRow(
                fruit.getId(),
                fruit.getName(),
                fruit.getDescription(),
                null,
                null,
                null,
                null,
                null,
                null,
                null
              )
            );
          }

          return fruit
            .getStorePrices()
            .stream()
            .sorted(Comparator.comparing(price -> price.getStore().getName()))
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
            );
        })
        .toList();
    }

    @Override
    public Optional<Fruit> findDetailedByName(String name) {
      return findByName(name).map(fruit -> {
        fruit.setStorePrices(
          fruit
            .getStorePrices()
            .stream()
            .sorted(Comparator.comparing(price -> price.getStore().getName()))
            .toList()
        );
        return fruit;
      });
    }

    @Override
    public Optional<Fruit> findByName(String name) {
      return fruitsById
        .values()
        .stream()
        .filter(fruit -> fruit.getName().equals(name))
        .findFirst();
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
      return Optional.ofNullable(fruitsById.get(id));
    }

    @Override
    public boolean existsById(Long id) {
      return fruitsById.containsKey(id);
    }

    @Override
    public Stream<Fruit> findAll() {
      return fruitsById.values().stream();
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
      if (entity.getId() != null) {
        fruitsById.remove(entity.getId());
      }
    }

    @Override
    public void deleteAll(Iterable<? extends Fruit> entities) {
      entities.forEach(this::delete);
    }

    @Override
    public long deleteAll() {
      long size = fruitsById.size();
      fruitsById.clear();
      return size;
    }
  }
}
