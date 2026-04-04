package io.helidon.labs.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.helidon.config.Config;
import io.helidon.labs.dto.FruitListRow;
import io.helidon.labs.model.Address;
import io.helidon.labs.model.Fruit;
import io.helidon.labs.model.Store;
import io.helidon.labs.model.StoreFruitPrice;
import io.helidon.service.registry.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service.Singleton
class CachingFruitRepository extends DelegatingFruitRepository {

  static final String CACHE_MAXIMUM_SIZE = "app.fruit.repository.cache.maximum-size";

  private static final String SUMMARY_CACHE_KEY = "all";

  private final JpaFruitRepository delegate;
  private final Cache<String, List<FruitListRow>> summaryCache;
  private final Cache<String, Optional<Fruit>> detailedFruitByNameCache;

  @Service.Inject
  CachingFruitRepository(JpaFruitRepository delegate, Config config) {
    this.delegate = delegate;
    long maximumSize = config.get(CACHE_MAXIMUM_SIZE).asLong().orElse(1_024L);
    this.summaryCache = Caffeine.newBuilder().maximumSize(maximumSize).build();
    this.detailedFruitByNameCache = Caffeine.newBuilder()
      .maximumSize(maximumSize)
      .build();
  }

  @Override
  protected FruitRepository delegate() {
    return delegate;
  }

  @Override
  public List<FruitListRow> listSummaryOrderByName() {
    return summaryCache.get(
      SUMMARY_CACHE_KEY,
      ignored -> List.copyOf(delegate.listSummaryOrderByName())
    );
  }

  @Override
  public Optional<Fruit> findDetailedByName(String name) {
    return detailedFruitByNameCache.get(
      name,
      key -> delegate.findDetailedByName(key).map(CachingFruitRepository::copyFruit)
    ).map(CachingFruitRepository::copyFruit);
  }

  @Override
  public <T extends Fruit> T save(T entity) {
    T saved = delegate.save(entity);
    invalidateCaches();
    return saved;
  }

  @Override
  public <T extends Fruit> Iterable<T> saveAll(Iterable<T> entities) {
    Iterable<T> saved = delegate.saveAll(entities);
    invalidateCaches();
    return saved;
  }

  @Override
  public long deleteById(Long id) {
    long deleted = delegate.deleteById(id);
    if (deleted > 0) {
      invalidateCaches();
    }
    return deleted;
  }

  @Override
  public void delete(Fruit entity) {
    delegate.delete(entity);
    invalidateCaches();
  }

  @Override
  public void deleteAll(Iterable<? extends Fruit> entities) {
    delegate.deleteAll(entities);
    invalidateCaches();
  }

  @Override
  public long deleteAll() {
    long deleted = delegate.deleteAll();
    if (deleted > 0) {
      invalidateCaches();
    }
    return deleted;
  }

  @Override
  public <T extends Fruit> T insert(T entity) {
    T inserted = delegate.insert(entity);
    invalidateCaches();
    return inserted;
  }

  @Override
  public <T extends Fruit> Iterable<T> insertAll(Iterable<T> entities) {
    Iterable<T> inserted = delegate.insertAll(entities);
    invalidateCaches();
    return inserted;
  }

  @Override
  public <T extends Fruit> T update(T entity) {
    T updated = delegate.update(entity);
    invalidateCaches();
    return updated;
  }

  @Override
  public <T extends Fruit> Iterable<T> updateAll(Iterable<T> entities) {
    Iterable<T> updated = delegate.updateAll(entities);
    invalidateCaches();
    return updated;
  }

  private void invalidateCaches() {
    summaryCache.invalidateAll();
    detailedFruitByNameCache.invalidateAll();
  }

  private static Fruit copyFruit(Fruit source) {
    Fruit copy = new Fruit(source.getId(), source.getName(), source.getDescription());
    List<StoreFruitPrice> copiedStorePrices = source.getStorePrices()
      .stream()
      .map(price -> copyStoreFruitPrice(price, copy))
      .toList();
    copy.setStorePrices(new ArrayList<>(copiedStorePrices));
    return copy;
  }

  private static StoreFruitPrice copyStoreFruitPrice(StoreFruitPrice source, Fruit fruitCopy) {
    return new StoreFruitPrice(copyStore(source.getStore()), fruitCopy, source.getPrice());
  }

  private static Store copyStore(Store source) {
    return new Store(
      source.getId(),
      source.getName(),
      copyAddress(source.getAddress()),
      source.getCurrency()
    );
  }

  private static Address copyAddress(Address source) {
    return source == null
      ? null
      : new Address(source.getAddress(), source.getCity(), source.getCountry());
  }
}
