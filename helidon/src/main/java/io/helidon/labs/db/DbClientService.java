package io.helidon.labs.db;

import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.service.registry.Service;

@Service.Singleton
public class DbClientService {

  private final DbClient dbClient;

  @Service.Inject
  public DbClientService(Config config) {
    this.dbClient = DbClient.create(config.get("db"));
  }

  public DbClient dbClient() {
    return dbClient;
  }

  @Service.PreDestroy
  void close() {
    dbClient.close();
  }
}
