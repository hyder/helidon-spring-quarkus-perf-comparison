package io.helidon.labs.repository;

import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbResultDml;
import io.helidon.dbclient.DbRow;
import io.helidon.dbclient.DbTransaction;
import io.helidon.labs.db.DbClientService;
import io.helidon.labs.dto.FruitListRow;
import io.helidon.labs.model.Address;
import io.helidon.labs.model.Fruit;
import io.helidon.labs.model.Store;
import io.helidon.labs.model.StoreFruitPrice;
import io.helidon.service.registry.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service.Singleton
class DbClientFruitRepository {

  private static final String SUMMARY_SQL = """
    SELECT f.id AS fruit_id,
           f.name AS fruit_name,
           f.description AS fruit_description,
           s.id AS store_id,
           s.name AS store_name,
           s.currency AS store_currency,
           s.address AS store_address,
           s.city AS store_city,
           s.country AS store_country,
           sp.price AS price
      FROM fruits f
      LEFT JOIN store_fruit_prices sp ON sp.fruit_id = f.id
      LEFT JOIN stores s ON s.id = sp.store_id
     ORDER BY f.name
    """;

  private static final String DETAIL_BY_NAME_SQL = """
    SELECT f.id AS fruit_id,
           f.name AS fruit_name,
           f.description AS fruit_description,
           s.id AS store_id,
           s.name AS store_name,
           s.currency AS store_currency,
           s.address AS store_address,
           s.city AS store_city,
           s.country AS store_country,
           sp.price AS price
      FROM fruits f
      LEFT JOIN store_fruit_prices sp ON sp.fruit_id = f.id
      LEFT JOIN stores s ON s.id = sp.store_id
     WHERE f.name = :name
    """;

  private static final String FIND_BY_NAME_SQL = """
    SELECT id, name, description
      FROM fruits
     WHERE name = :name
    """;

  private static final String INSERT_SQL = """
    INSERT INTO fruits(name, description)
    VALUES (?, ?)
  """;

  private final DbClient dbClient;

  @Service.Inject
  DbClientFruitRepository(DbClientService dbClientService) {
    this.dbClient = dbClientService.dbClient();
  }

  public List<FruitListRow> listSummaryOrderByName() {
    return querySummaryRows();
  }

  public Optional<Fruit> findDetailedByName(String name) {
    return queryFruitDetailByName(name);
  }

  public Optional<Fruit> findByName(String name) {
    return queryFruitByName(name);
  }

  public <T extends Fruit> T insert(T entity) {
    DbTransaction transaction = dbClient.transaction();
    DbResultDml result = transaction
      .createInsert(INSERT_SQL)
      .addParam(entity.getName())
      .addParam(entity.getDescription())
      .returnColumns(List.of("id"))
      .insert();
    try {
      Long id = result
        .generatedKeys()
        .findFirst()
        .map(row -> row.column("id").get(Long.class))
        .orElseThrow(() ->
          new IllegalStateException("Fruit insert did not return a generated id.")
        );
      entity.setId(id);
      if (entity.getStorePrices() == null) {
        entity.setStorePrices(new ArrayList<>());
      }
      transaction.commit();
      return entity;
    } catch (RuntimeException e) {
      transaction.rollback();
      throw e;
    } finally {
      closeResult(result);
    }
  }

  private static FruitListRow toFruitListRow(DbRow row) {
    return new FruitListRow(
      row.column("fruit_id").get(Long.class),
      row.column("fruit_name").get(String.class),
      row.column("fruit_description").get(String.class),
      nullable(row, "store_id", Long.class),
      nullable(row, "store_name", String.class),
      nullable(row, "store_currency", String.class),
      nullable(row, "store_address", String.class),
      nullable(row, "store_city", String.class),
      nullable(row, "store_country", String.class),
      nullable(row, "price", BigDecimal.class)
    );
  }

  private List<FruitListRow> querySummaryRows() {
    try (Stream<DbRow> rows = dbClient.execute().createQuery(SUMMARY_SQL).execute()) {
      return rows.map(DbClientFruitRepository::toFruitListRow).toList();
    }
  }

  private Optional<Fruit> queryFruitDetailByName(String name) {
    try (
      Stream<DbRow> rows = dbClient
        .execute()
        .createQuery(DETAIL_BY_NAME_SQL)
        .addParam("name", name)
        .execute()
    ) {
      List<DbRow> foundRows = rows.toList();
      if (foundRows.isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(toFruit(foundRows));
    }
  }

  private Optional<Fruit> queryFruitByName(String name) {
    return dbClient
      .execute()
      .createGet(FIND_BY_NAME_SQL)
      .addParam("name", name)
      .execute()
      .map(DbClientFruitRepository::toFruit);
  }

  private static Fruit toFruit(DbRow row) {
    return new Fruit(
      row.column("id").get(Long.class),
      row.column("name").get(String.class),
      row.column("description").get(String.class)
    );
  }

  private static Fruit toFruit(List<DbRow> rows) {
    DbRow first = rows.getFirst();
    Fruit fruit = new Fruit(
      first.column("fruit_id").get(Long.class),
      first.column("fruit_name").get(String.class),
      first.column("fruit_description").get(String.class)
    );
    List<StoreFruitPrice> storePrices = new ArrayList<>();
    for (DbRow row : rows) {
      Long storeId = nullable(row, "store_id", Long.class);
      if (storeId == null) {
        continue;
      }
      Store store = new Store(
        storeId,
        row.column("store_name").get(String.class),
        new Address(
          row.column("store_address").get(String.class),
          row.column("store_city").get(String.class),
          row.column("store_country").get(String.class)
        ),
        row.column("store_currency").get(String.class)
      );
      storePrices.add(
        new StoreFruitPrice(
          store,
          fruit,
          row.column("price").get(BigDecimal.class)
        )
      );
    }
    storePrices.sort((left, right) ->
      left.getStore().getName().compareTo(right.getStore().getName())
    );
    fruit.setStorePrices(storePrices);
    return fruit;
  }

  private static <T> T nullable(DbRow row, String columnName, Class<T> type) {
    Object value = row.column(columnName).get();
    return value == null ? null : type.cast(value);
  }

  private static void closeResult(DbResultDml result) {
    try {
      result.close();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to close DB insert result.", e);
    }
  }
}
