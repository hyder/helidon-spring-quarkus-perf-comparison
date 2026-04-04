package io.helidon.labs.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.helidon.config.Config;
import io.helidon.config.MapConfigSource;
import io.helidon.labs.dto.FruitListRow;
import io.helidon.labs.model.Address;
import io.helidon.labs.model.Fruit;
import io.helidon.labs.model.Store;
import io.helidon.labs.model.StoreFruitPrice;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class CachingFruitRepositoryTest {

  @Test
  void cachesReadQueriesAndInvalidatesOnInsert() {
    CountingFruitDataRepository dataRepository = new CountingFruitDataRepository();
    JpaFruitRepository jpaRepository = new JpaFruitRepository(dataRepository);
    CachingFruitRepository repository = new CachingFruitRepository(
      jpaRepository,
      Config.create(() -> MapConfigSource.create(Map.of(
        CachingFruitRepository.CACHE_MAXIMUM_SIZE,
        "32"
      )))
    );

    repository.listSummaryOrderByName();
    repository.listSummaryOrderByName();
    repository.findDetailedByName("Apple");
    repository.findDetailedByName("Apple");

    assertThat(dataRepository.listSummaryCalls.get(), equalTo(1));
    assertThat(dataRepository.findDetailedCalls.get(), equalTo(1));

    repository.insert(new Fruit(null, "Banana", "Curved yellow fruit"));
    repository.listSummaryOrderByName();
    repository.findDetailedByName("Apple");

    assertThat(dataRepository.listSummaryCalls.get(), equalTo(2));
    assertThat(dataRepository.findDetailedCalls.get(), equalTo(2));
  }

  @Test
  void returnsDetachedCopiesForDetailedQueries() {
    CountingFruitDataRepository dataRepository = new CountingFruitDataRepository();
    JpaFruitRepository jpaRepository = new JpaFruitRepository(dataRepository);
    CachingFruitRepository repository = new CachingFruitRepository(
      jpaRepository,
      Config.empty()
    );

    Fruit first = repository.findDetailedByName("Apple").orElseThrow();
    Fruit second = repository.findDetailedByName("Apple").orElseThrow();

    assertThat(first == second, is(false));
    assertThat(first.getStorePrices().getFirst() == second.getStorePrices().getFirst(), is(false));

    first.setDescription("Changed");

    Fruit third = repository.findDetailedByName("Apple").orElseThrow();

    assertThat(third.getDescription(), is("Hearty fruit"));
    assertThat(third.getStorePrices().getFirst().getStore().getAddress(), notNullValue());
  }

  private static final class CountingFruitDataRepository implements FruitDataRepository {

    private final AtomicInteger listSummaryCalls = new AtomicInteger();
    private final AtomicInteger findDetailedCalls = new AtomicInteger();
    private final AtomicInteger nextId = new AtomicInteger(2);

    @Override
    public List<FruitListRow> listSummaryOrderByName() {
      listSummaryCalls.incrementAndGet();
      return List.of(
        new FruitListRow(
          1L,
          "Apple",
          "Hearty fruit",
          1L,
          "Downtown Market",
          "USD",
          "123 Main St",
          "Springfield",
          "USA",
          new BigDecimal("1.29")
        )
      );
    }

    @Override
    public Optional<Fruit> findDetailedByName(String name) {
      findDetailedCalls.incrementAndGet();
      if (!"Apple".equals(name)) {
        return Optional.empty();
      }

      Fruit fruit = new Fruit(1L, "Apple", "Hearty fruit");
      Store store = new Store(
        1L,
        "Downtown Market",
        new Address("123 Main St", "Springfield", "USA"),
        "USD"
      );
      fruit.setStorePrices(
        new ArrayList<>(
          List.of(new StoreFruitPrice(store, fruit, new BigDecimal("1.29")))
        )
      );
      return Optional.of(fruit);
    }

    @Override
    public Optional<Fruit> findByName(String name) {
      return Optional.empty();
    }

    @Override
    public <T extends Fruit> T save(T entity) {
      return entity;
    }

    @Override
    public <T extends Fruit> Iterable<T> saveAll(Iterable<T> entities) {
      return entities;
    }

    @Override
    public Optional<Fruit> findById(Long id) {
      return Optional.empty();
    }

    @Override
    public boolean existsById(Long id) {
      return false;
    }

    @Override
    public Stream<Fruit> findAll() {
      return Stream.empty();
    }

    @Override
    public long count() {
      return 0;
    }

    @Override
    public long deleteById(Long id) {
      return 0;
    }

    @Override
    public void delete(Fruit entity) {
    }

    @Override
    public void deleteAll(Iterable<? extends Fruit> entities) {
    }

    @Override
    public long deleteAll() {
      return 0;
    }

    @Override
    public <T extends Fruit> T insert(T entity) {
      entity.setId((long) nextId.getAndIncrement());
      return entity;
    }

    @Override
    public <T extends Fruit> Iterable<T> insertAll(Iterable<T> entities) {
      return entities;
    }

    @Override
    public <T extends Fruit> T update(T entity) {
      return entity;
    }

    @Override
    public <T extends Fruit> Iterable<T> updateAll(Iterable<T> entities) {
      return entities;
    }
  }
}
