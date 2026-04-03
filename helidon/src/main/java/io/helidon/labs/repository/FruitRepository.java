package io.helidon.labs.repository;

import io.helidon.labs.dto.FruitListRow;
import io.helidon.labs.model.Fruit;
import java.util.List;
import java.util.Optional;

public interface FruitRepository {
  List<FruitListRow> listSummaryOrderByName();

  Optional<Fruit> findDetailedByName(String name);

  Optional<Fruit> findByName(String name);

  <T extends Fruit> T insert(T entity);
}
