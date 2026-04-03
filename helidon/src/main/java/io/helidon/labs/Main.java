package io.helidon.labs;

import io.helidon.labs.db.DatabaseInitializer;
import io.helidon.logging.common.LogConfig;
import io.helidon.service.registry.Service;
import io.helidon.service.registry.ServiceRegistryManager;
import io.helidon.service.registry.Services;
import io.helidon.webserver.WebServer;

/**
 * The application main class.
 */
@Service.GenerateBinding
public class Main {

  /**
   * Cannot be instantiated
   */
  private Main() {}

  /**
   * Application main entry point.
   * @param args command line arguments.
   */
  public static void main(String[] args) {
    // load logging configuration
    LogConfig.configureRuntime();

    ServiceRegistryManager.start(ApplicationBinding.create());
    Services.get(DatabaseInitializer.class);

    WebServer webServer = Services.get(WebServer.class);
    System.out.println(
      "Server started on: http://localhost:" + webServer.port()
    );
  }
}
