package io.helidon.labs.repository;

import io.helidon.data.Data;
import io.helidon.labs.dto.FruitListRow;
import io.helidon.labs.model.Fruit;
import java.util.List;
import java.util.Optional;

@Data.Repository
public interface FruitDataRepository extends Data.CrudRepository<Fruit, Long> {
  @Data.Query("""
    SELECT new io.helidon.labs.dto.FruitListRow(
      f.id, f.name, f.description,
      s.id, s.name, s.currency,
      s.address.address, s.address.city, s.address.country,
      sp.price
    )
    FROM Fruit f
    LEFT JOIN f.storePrices sp
    LEFT JOIN sp.store s
    ORDER BY f.name, s.name
    """)
  List<FruitListRow> listSummaryOrderByName();

  @Data.Query("""
    SELECT DISTINCT f
    FROM Fruit f
    LEFT JOIN FETCH f.storePrices sp
    LEFT JOIN FETCH sp.store s
    WHERE f.name = :name
    ORDER BY s.name
    """)
  Optional<Fruit> findDetailedByName(String name);

  Optional<Fruit> findByName(String name);
}
