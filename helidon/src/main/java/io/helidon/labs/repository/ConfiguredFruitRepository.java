package io.helidon.labs.repository;

import io.helidon.config.Config;
import io.helidon.service.registry.Service;
import java.util.Locale;

@Service.Singleton
class ConfiguredFruitRepository extends DelegatingFruitRepository {

  static final String DELEGATE_CONFIG_KEY = "app.fruit.repository.delegate";

  private final FruitRepository delegate;

  @Service.Inject
  ConfiguredFruitRepository(
    JpaFruitRepository jpaRepository,
    CachingFruitRepository cachingRepository,
    Config config
  ) {
    this.delegate = switch (config.get(DELEGATE_CONFIG_KEY).asString().orElse("jpa")
      .toLowerCase(Locale.ROOT)) {
      case "plain", "jpa" -> jpaRepository;
      case "cache", "cached", "caffeine" -> cachingRepository;
      default -> throw new IllegalStateException(
        "Unsupported fruit repository delegate: " +
        config.get(DELEGATE_CONFIG_KEY).asString().orElse("")
      );
    };
  }

  @Override
  protected FruitRepository delegate() {
    return delegate;
  }
}
