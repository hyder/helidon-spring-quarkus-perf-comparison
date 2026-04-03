package io.helidon.labs.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.helidon.common.Weighted;
import io.helidon.common.config.Config;
import io.helidon.http.media.MediaSupport;
import io.helidon.http.media.jackson.JacksonSupport;
import io.helidon.http.media.spi.MediaSupportProvider;

public class BlackbirdJacksonMediaSupportProvider
  implements MediaSupportProvider, Weighted {

  @Override
  public String configKey() {
    return "jackson";
  }

  @Override
  public MediaSupport create(Config config, String name) {
    return JacksonSupport
      .builder()
      .config(config)
      .name(name)
      .objectMapper(objectMapper())
      .build();
  }

  @Override
  public double weight() {
    return 20.0;
  }

  private static ObjectMapper objectMapper() {
    return JsonMapper
      .builder()
      .addModule(new ParameterNamesModule())
      .addModule(new Jdk8Module())
      .addModule(new JavaTimeModule())
      .addModule(new BlackbirdModule())
      .build();
  }
}
