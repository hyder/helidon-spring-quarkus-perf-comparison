package io.helidon.labs.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.helidon.config.Config;
import io.helidon.config.MapConfigSource;
import io.helidon.labs.dto.FruitListRow;
import io.helidon.labs.model.Fruit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class ConfiguredFruitRepositoryTest {

  @Test
  void defaultsToJpaRepository() {
    CountingFruitDataRepository dataRepository = new CountingFruitDataRepository();
    JpaFruitRepository jpaRepository = new JpaFruitRepository(dataRepository);
    CachingFruitRepository cachingRepository = new CachingFruitRepository(
      jpaRepository,
      Config.empty()
    );

    ConfiguredFruitRepository repository = new ConfiguredFruitRepository(
      jpaRepository,
      cachingRepository,
      Config.empty()
    );

    repository.listSummaryOrderByName();
    repository.listSummaryOrderByName();

    assertThat(dataRepository.listSummaryCalls.get(), equalTo(2));
  }

  @Test
  void usesCaffeineDelegateWhenConfigured() {
    CountingFruitDataRepository dataRepository = new CountingFruitDataRepository();
    JpaFruitRepository jpaRepository = new JpaFruitRepository(dataRepository);
    CachingFruitRepository cachingRepository = new CachingFruitRepository(
      jpaRepository,
      config(Map.of(ConfiguredFruitRepository.DELEGATE_CONFIG_KEY, "caffeine"))
    );

    ConfiguredFruitRepository repository = new ConfiguredFruitRepository(
      jpaRepository,
      cachingRepository,
      config(Map.of(ConfiguredFruitRepository.DELEGATE_CONFIG_KEY, "caffeine"))
    );

    repository.listSummaryOrderByName();
    repository.listSummaryOrderByName();

    assertThat(dataRepository.listSummaryCalls.get(), equalTo(1));
  }

  @Test
  void rejectsUnsupportedDelegate() {
    CountingFruitDataRepository dataRepository = new CountingFruitDataRepository();
    JpaFruitRepository jpaRepository = new JpaFruitRepository(dataRepository);
    CachingFruitRepository cachingRepository = new CachingFruitRepository(
      jpaRepository,
      Config.empty()
    );

    IllegalStateException exception = org.junit.jupiter.api.Assertions.assertThrows(
      IllegalStateException.class,
      () -> new ConfiguredFruitRepository(
        jpaRepository,
        cachingRepository,
        config(Map.of(ConfiguredFruitRepository.DELEGATE_CONFIG_KEY, "bogus"))
      )
    );

    assertThat(
      exception.getMessage(),
      is("Unsupported fruit repository delegate: bogus")
    );
  }

  private static Config config(Map<String, String> values) {
    return Config.create(() -> MapConfigSource.create(values));
  }

  private static final class CountingFruitDataRepository implements FruitDataRepository {

    private final AtomicInteger listSummaryCalls = new AtomicInteger();

    @Override
    public List<FruitListRow> listSummaryOrderByName() {
      listSummaryCalls.incrementAndGet();
      return List.of();
    }

    @Override
    public Optional<Fruit> findDetailedByName(String name) {
      return Optional.empty();
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
