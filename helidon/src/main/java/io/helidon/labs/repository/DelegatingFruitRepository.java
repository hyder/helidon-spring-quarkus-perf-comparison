package io.helidon.labs.repository;

import io.helidon.labs.dto.FruitListRow;
import io.helidon.labs.model.Fruit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

abstract class DelegatingFruitRepository implements FruitRepository {

  protected abstract FruitRepository delegate();

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
  public <T extends Fruit> T save(T entity) {
    return delegate().save(entity);
  }

  @Override
  public <T extends Fruit> Iterable<T> saveAll(Iterable<T> entities) {
    return delegate().saveAll(entities);
  }

  @Override
  public Optional<Fruit> findById(Long id) {
    return delegate().findById(id);
  }

  @Override
  public boolean existsById(Long id) {
    return delegate().existsById(id);
  }

  @Override
  public Stream<Fruit> findAll() {
    return delegate().findAll();
  }

  @Override
  public long count() {
    return delegate().count();
  }

  @Override
  public long deleteById(Long id) {
    return delegate().deleteById(id);
  }

  @Override
  public void delete(Fruit entity) {
    delegate().delete(entity);
  }

  @Override
  public void deleteAll(Iterable<? extends Fruit> entities) {
    delegate().deleteAll(entities);
  }

  @Override
  public long deleteAll() {
    return delegate().deleteAll();
  }

  @Override
  public <T extends Fruit> T insert(T entity) {
    return delegate().insert(entity);
  }

  @Override
  public <T extends Fruit> Iterable<T> insertAll(Iterable<T> entities) {
    return delegate().insertAll(entities);
  }

  @Override
  public <T extends Fruit> T update(T entity) {
    return delegate().update(entity);
  }

  @Override
  public <T extends Fruit> Iterable<T> updateAll(Iterable<T> entities) {
    return delegate().updateAll(entities);
  }
}
