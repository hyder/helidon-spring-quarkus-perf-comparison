package io.helidon.labs.db;

import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbRow;
import io.helidon.dbclient.DbTransaction;
import io.helidon.service.registry.Service;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service.Singleton
public class DatabaseInitializer {

  private static final String INIT_SCRIPT = "init.sql";
  private final DbClient dbClient;

  @Service.Inject
  public DatabaseInitializer(DbClientService dbClientService) {
    this.dbClient = dbClientService.dbClient();
  }

  @Service.PostConstruct
  void initialize() {
    if (hasFruitData()) {
      return;
    }

    DbTransaction transaction = dbClient.transaction();
    try {
      for (String statement : loadStatements(INIT_SCRIPT)) {
        transaction.createDmlStatement(statement).execute();
      }
      transaction.commit();
    } catch (RuntimeException e) {
      transaction.rollback();
      throw e;
    }
  }

  private boolean hasFruitData() {
    try (
      Stream<DbRow> rows = dbClient
        .execute()
        .createQuery("SELECT COUNT(*) AS fruit_count FROM fruits")
        .execute()
    ) {
      return rows
        .findFirst()
        .map(row -> row.column("fruit_count").get(Number.class).longValue() > 0)
        .orElse(false);
    } catch (RuntimeException e) {
      return false;
    }
  }

  private static List<String> loadStatements(String resourceName) {
    try (InputStream stream = DatabaseInitializer.class
      .getClassLoader()
      .getResourceAsStream(resourceName)) {
      if (stream == null) {
        throw new IllegalStateException("Missing SQL resource: " + resourceName);
      }
      String sql = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
      return splitStatements(sql);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read SQL resource: " + resourceName, e);
    }
  }

  private static List<String> splitStatements(String sql) {
    List<String> statements = new ArrayList<>();
    StringBuilder current = new StringBuilder();

    for (String line : sql.split("\\R")) {
      String trimmed = line.trim();
      if (trimmed.isEmpty() || trimmed.startsWith("--")) {
        continue;
      }
      current.append(line).append('\n');
      if (trimmed.endsWith(";")) {
        statements.add(current.toString().trim());
        current.setLength(0);
      }
    }

    if (!current.isEmpty()) {
      statements.add(current.toString().trim());
    }

    return statements;
  }
}
