package io.helidon.labs.repository;

import io.helidon.common.Weight;
import io.helidon.data.DataException;
import io.helidon.data.jakarta.persistence.JpaRepositoryExecutor;
import io.helidon.labs.dto.FruitListRow;
import io.helidon.labs.model.Fruit;
import io.helidon.transaction.Tx;
import io.helidon.transaction.TxException;
import io.helidon.service.registry.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service.Singleton
@Weight(200)
class OptimizedFruitRepository implements FruitRepository {

  private static final String QUERY_HINT_READ_ONLY = "org.hibernate.readOnly";

  private final JpaRepositoryExecutor executor;

  @Service.Inject
  OptimizedFruitRepository(JpaRepositoryExecutor executor) {
    this.executor = executor;
  }

  @Override
  public <T extends Fruit> T save(T entity) {
    return executor.call(em -> em.merge(entity));
  }

  @Override
  public <T extends Fruit> Iterable<T> saveAll(Iterable<T> entities) {
    List<T> mergedEntities = new ArrayList<>();
    executor.run(em -> entities.forEach(entity -> mergedEntities.add(em.merge(entity))));
    return mergedEntities;
  }

  @Override
  public Optional<Fruit> findById(Long id) {
    if (isTransactionActive()) {
      return Optional.ofNullable(executor.call(em -> em.find(Fruit.class, id)));
    }

    try (EntityManager entityManager = executor.factory().createEntityManager()) {
      return Optional.ofNullable(entityManager.find(Fruit.class, id));
    }
  }

  @Override
  public boolean existsById(Long id) {
    return findById(id).isPresent();
  }

  @Override
  public Stream<Fruit> findAll() {
    if (isTransactionActive()) {
      return executor.call(em -> em.createQuery("SELECT f FROM Fruit f", Fruit.class)
        .getResultList()
        .stream());
    }

    try (EntityManager entityManager = executor.factory().createEntityManager()) {
      return readOnlyQuery(entityManager, "SELECT f FROM Fruit f", Fruit.class)
        .getResultList()
        .stream();
    }
  }

  @Override
  public long count() {
    if (isTransactionActive()) {
      return executor.call(em -> em.createQuery("SELECT COUNT(f) FROM Fruit f", Long.class)
        .getSingleResult());
    }

    try (EntityManager entityManager = executor.factory().createEntityManager()) {
      return readOnlyQuery(entityManager, "SELECT COUNT(f) FROM Fruit f", Long.class)
        .getSingleResult();
    }
  }

  @Override
  public long deleteById(Long id) {
    return (long) executor.call(em -> em.createQuery("DELETE FROM Fruit f WHERE f.id = :id")
      .setParameter("id", id)
      .executeUpdate());
  }

  @Override
  public void delete(Fruit entity) {
    executor.run(em -> em.remove(em.contains(entity) ? entity : em.merge(entity)));
  }

  @Override
  public void deleteAll(Iterable<? extends Fruit> entities) {
    executor.run(em -> {
      for (Fruit entity : entities) {
        em.remove(em.contains(entity) ? entity : em.merge(entity));
      }
    });
  }

  @Override
  public long deleteAll() {
    return (long) executor.call(em -> em.createQuery("DELETE FROM Fruit f").executeUpdate());
  }

  @Override
  public <T extends Fruit> T insert(T entity) {
    executor.run(em -> em.persist(entity));
    return entity;
  }

  @Override
  public <T extends Fruit> Iterable<T> insertAll(Iterable<T> entities) {
    executor.run(em -> entities.forEach(em::persist));
    return entities;
  }

  @Override
  public <T extends Fruit> T update(T entity) {
    return executor.call(em -> {
      Object id = executor.persistenceUnitUtil().getIdentifier(entity);
      if (id == null) {
        throw new DataException("Entity does not have ID value set.");
      }

      Fruit sourceEntity = em.find(Fruit.class, id);
      if (sourceEntity == null) {
        throw new DataException(String.format("Entity with id = \"%s\" does not exist.", id));
      }
      return em.merge(entity);
    });
  }

  @Override
  public <T extends Fruit> Iterable<T> updateAll(Iterable<T> entities) {
    List<T> mergedEntities = new ArrayList<>();
    executor.run(em -> entities.forEach(entity -> {
      Object id = executor.persistenceUnitUtil().getIdentifier(entity);
      if (id == null) {
        throw new DataException("Entity does not have ID value set.");
      }

      Fruit sourceEntity = em.find(Fruit.class, id);
      if (sourceEntity == null) {
        throw new DataException(String.format("Entity with id = \"%s\" does not exist.", id));
      }
      mergedEntities.add(em.merge(entity));
    }));
    return mergedEntities;
  }

  @Override
  public Optional<Fruit> findByName(String name) {
    if (isTransactionActive()) {
      return JpaRepositoryExecutor.optionalFromQuery(executor.call(em -> em.createQuery(
          "SELECT f FROM Fruit f WHERE f.name = :name",
          Fruit.class
        )
        .setParameter("name", name)
        .getResultList()));
    }

    try (EntityManager entityManager = executor.factory().createEntityManager()) {
      return JpaRepositoryExecutor.optionalFromQuery(readOnlyQuery(
          entityManager,
          "SELECT f FROM Fruit f WHERE f.name = :name",
          Fruit.class
        )
        .setParameter("name", name)
        .getResultList());
    }
  }

  @Override
  public List<FruitListRow> listSummaryOrderByName() {
    if (isTransactionActive()) {
      return executor.call(em -> em.createQuery(
          "SELECT new io.helidon.labs.dto.FruitListRow(" +
            "f.id, f.name, f.description, s.id, s.name, s.currency, " +
            "s.address.address, s.address.city, s.address.country, sp.price" +
            ") FROM Fruit f LEFT JOIN f.storePrices sp LEFT JOIN sp.store s " +
            "ORDER BY f.name, s.name",
          FruitListRow.class
        )
        .getResultList());
    }

    try (EntityManager entityManager = executor.factory().createEntityManager()) {
      return readOnlyQuery(
          entityManager,
          "SELECT new io.helidon.labs.dto.FruitListRow(" +
            "f.id, f.name, f.description, s.id, s.name, s.currency, " +
            "s.address.address, s.address.city, s.address.country, sp.price" +
            ") FROM Fruit f LEFT JOIN f.storePrices sp LEFT JOIN sp.store s " +
            "ORDER BY f.name, s.name",
          FruitListRow.class
        )
        .getResultList();
    }
  }

  @Override
  public Optional<Fruit> findDetailedByName(String name) {
    if (isTransactionActive()) {
      return JpaRepositoryExecutor.optionalFromQuery(executor.call(em -> em.createQuery(
          "SELECT DISTINCT f FROM Fruit f LEFT JOIN FETCH f.storePrices sp LEFT JOIN FETCH sp.store " +
            "WHERE f.name = :name ORDER BY s.name",
          Fruit.class
        )
        .setParameter("name", name)
        .getResultList()));
    }

    try (EntityManager entityManager = executor.factory().createEntityManager()) {
      return JpaRepositoryExecutor.optionalFromQuery(readOnlyQuery(
          entityManager,
          "SELECT DISTINCT f FROM Fruit f LEFT JOIN FETCH f.storePrices sp LEFT JOIN FETCH sp.store " +
            "WHERE f.name = :name ORDER BY s.name",
          Fruit.class
        )
        .setParameter("name", name)
        .getResultList());
    }
  }

  private static <T> TypedQuery<T> readOnlyQuery(
    EntityManager entityManager,
    String query,
    Class<T> type
  ) {
    return entityManager
      .createQuery(query, type)
      .setHint(QUERY_HINT_READ_ONLY, true);
  }

  private static boolean isTransactionActive() {
    try {
      return Tx.transaction(Tx.Type.MANDATORY, () -> Boolean.TRUE);
    } catch (TxException ignored) {
      return false;
    }
  }
}
