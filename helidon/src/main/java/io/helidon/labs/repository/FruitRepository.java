package io.helidon.labs.repository;

import java.util.List;
import java.util.Optional;

import io.helidon.data.Data;
import io.helidon.labs.model.Fruit;

@Data.Repository
public interface FruitRepository extends Data.CrudRepository<Fruit, Long> {

    @Data.Query("SELECT DISTINCT f FROM Fruit f LEFT JOIN FETCH f.storePrices sp LEFT JOIN FETCH sp.store ORDER BY f.name")
    List<Fruit> listDetailedOrderByName();

    @Data.Query("SELECT DISTINCT f FROM Fruit f LEFT JOIN FETCH f.storePrices sp LEFT JOIN FETCH sp.store WHERE f.name = :name")
    Optional<Fruit> findDetailedByName(String name);

    Optional<Fruit> findByName(String name);
}
