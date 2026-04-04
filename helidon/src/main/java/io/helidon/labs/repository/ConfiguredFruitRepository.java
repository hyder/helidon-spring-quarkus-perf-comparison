package io.helidon.labs.repository;

import io.helidon.config.Config;
import io.helidon.labs.dto.FruitListRow;
import io.helidon.labs.model.Fruit;
import io.helidon.service.registry.Service;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service.Singleton
class ConfiguredFruitRepository implements FruitRepository {

  private static final String DEFAULT_REPOSITORY_TYPE = "caffeine";

  private final DbClientFruitRepository dbClientRepository;
  private final CachingFruitRepository cachingRepository;
  private final String repositoryType;

  @Service.Inject
  ConfiguredFruitRepository(
    DbClientFruitRepository dbClientRepository,
    CachingFruitRepository cachingRepository,
    Config config
  ) {
    this.dbClientRepository = dbClientRepository;
    this.cachingRepository = cachingRepository;
    this.repositoryType = config
      .get("fruit-repository.type")
      .asString()
      .orElse(DEFAULT_REPOSITORY_TYPE)
      .toLowerCase(Locale.ROOT);
    validateRepositoryType(repositoryType);
  }

  @Override
  public List<FruitListRow> listSummaryOrderByName() {
    return delegate().listSummaryOrderByName();
  }

  @Override
  public Optional<Fruit> findDetailedByName(String name) {
    return delegate().findDetailedByName(name);
  }

  @Override
  public Optional<Fruit> findByName(String name) {
    return delegate().findByName(name);
  }

  @Override
  public <T extends Fruit> T insert(T entity) {
    return delegate().insert(entity);
  }

  private RepositoryDelegate delegate() {
    return switch (repositoryType) {
      case "db-client" -> new RepositoryDelegate() {
        @Override
        public List<FruitListRow> listSummaryOrderByName() {
          return dbClientRepository.listSummaryOrderByName();
        }

        @Override
        public Optional<Fruit> findDetailedByName(String name) {
          return dbClientRepository.findDetailedByName(name);
        }

        @Override
        public Optional<Fruit> findByName(String name) {
          return dbClientRepository.findByName(name);
        }

        @Override
        public <T extends Fruit> T insert(T entity) {
          return dbClientRepository.insert(entity);
        }
      };
      case "caffeine" -> new RepositoryDelegate() {
        @Override
        public List<FruitListRow> listSummaryOrderByName() {
          return cachingRepository.listSummaryOrderByName();
        }

        @Override
        public Optional<Fruit> findDetailedByName(String name) {
          return cachingRepository.findDetailedByName(name);
        }

        @Override
        public Optional<Fruit> findByName(String name) {
          return cachingRepository.findByName(name);
        }

        @Override
        public <T extends Fruit> T insert(T entity) {
          return cachingRepository.insert(entity);
        }
      };
      default -> throw new IllegalStateException("Unsupported repository type: " + repositoryType);
    };
  }

  private static void validateRepositoryType(String repositoryType) {
    if (!"db-client".equals(repositoryType) && !"caffeine".equals(repositoryType)) {
      throw new IllegalStateException(
        "Unsupported fruit repository type: "
          + repositoryType
          + ". Supported values: db-client, caffeine."
      );
    }
  }

  private interface RepositoryDelegate {
    List<FruitListRow> listSummaryOrderByName();

    Optional<Fruit> findDetailedByName(String name);

    Optional<Fruit> findByName(String name);

    <T extends Fruit> T insert(T entity);
  }
}
