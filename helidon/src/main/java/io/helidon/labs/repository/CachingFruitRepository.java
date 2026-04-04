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
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service.Singleton
class CachingFruitRepository {

  private static final String SUMMARY_CACHE_KEY = "all";
  private static final Duration DEFAULT_TTL = Duration.ofSeconds(30);
  private static final long DEFAULT_SUMMARY_MAXIMUM_SIZE = 1;
  private static final long DEFAULT_ENTITY_MAXIMUM_SIZE = 1_024;

  private final DbClientFruitRepository delegate;
  private final Cache<String, List<FruitListRow>> summaryCache;
  private final Cache<String, Optional<Fruit>> fruitByNameCache;
  private final Cache<String, Optional<Fruit>> fruitDetailCache;

  @Service.Inject
  CachingFruitRepository(DbClientFruitRepository delegate, Config config) {
    this.delegate = delegate;
    Config cacheConfig = config.get("cache.fruit-repository");
    Duration ttl = cacheConfig.get("ttl").as(Duration.class).orElse(DEFAULT_TTL);
    long summaryMaximumSize = cacheConfig
      .get("summary-maximum-size")
      .asLong()
      .orElse(DEFAULT_SUMMARY_MAXIMUM_SIZE);
    long entityMaximumSize = cacheConfig
      .get("entity-maximum-size")
      .asLong()
      .orElse(DEFAULT_ENTITY_MAXIMUM_SIZE);
    this.summaryCache = Caffeine
      .newBuilder()
      .maximumSize(summaryMaximumSize)
      .expireAfterWrite(ttl)
      .build();
    this.fruitByNameCache = Caffeine
      .newBuilder()
      .maximumSize(entityMaximumSize)
      .expireAfterWrite(ttl)
      .build();
    this.fruitDetailCache = Caffeine
      .newBuilder()
      .maximumSize(entityMaximumSize)
      .expireAfterWrite(ttl)
      .build();
  }

  public List<FruitListRow> listSummaryOrderByName() {
    return summaryCache.get(SUMMARY_CACHE_KEY, ignored -> delegate.listSummaryOrderByName());
  }

  public Optional<Fruit> findDetailedByName(String name) {
    return fruitDetailCache
      .get(name, delegate::findDetailedByName)
      .map(CachingFruitRepository::copyFruit);
  }

  public Optional<Fruit> findByName(String name) {
    return fruitByNameCache
      .get(name, delegate::findByName)
      .map(CachingFruitRepository::copyFruit);
  }

  public <T extends Fruit> T insert(T entity) {
    T inserted = delegate.insert(entity);
    invalidateFruitCaches(inserted.getName());
    return inserted;
  }

  private void invalidateFruitCaches(String fruitName) {
    summaryCache.invalidate(SUMMARY_CACHE_KEY);
    fruitByNameCache.invalidate(fruitName);
    fruitDetailCache.invalidate(fruitName);
  }

  private static Fruit copyFruit(Fruit source) {
    Fruit copy = new Fruit(source.getId(), source.getName(), source.getDescription());
    List<StoreFruitPrice> copiedPrices = source
      .getStorePrices()
      .stream()
      .map(price -> copyStoreFruitPrice(price, copy))
      .toList();
    copy.setStorePrices(copiedPrices);
    return copy;
  }

  private static StoreFruitPrice copyStoreFruitPrice(StoreFruitPrice source, Fruit fruit) {
    return new StoreFruitPrice(copyStore(source.getStore()), fruit, source.getPrice());
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
    return new Address(
      source.getAddress(),
      source.getCity(),
      source.getCountry()
    );
  }
}
