package io.avaje.jsonb.generator.models.valid;

import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import io.avaje.jsonb.Json;

@Json
public final class PropertyCreator {

  private final transient String[] arguments;

  private final transient List<Consumer<?>> hooks;

  @Json.Property("uid")
  private final String identifier;

  @Json.Property("stp")
  private final Instant startup;

  @Json.Property("cfg")
  private final Properties configuration;

  @Json.Property("ins")
  private final Set<PropertyCreator> instances;

  @Json.Creator
  public PropertyCreator(
      String identifier,
      Instant startup,
      Properties configuration,
      Set<PropertyCreator> instances) {
    this.arguments = null;
    this.hooks = null;

    this.identifier = identifier;
    this.startup = startup;
    this.configuration = configuration;
    this.instances = null;
  }

  public PropertyCreator(String[] arguments, Instant startup) throws IOException {
    this.arguments = arguments != null ? arguments : new String[0];
    this.hooks = new ArrayList<>();

    this.identifier = Instant.now().toEpochMilli() + "-" + UUID.randomUUID();
    this.startup = startup;

    this.configuration = new Properties();
    this.configuration.putAll(System.getenv());
    this.configuration.putAll(System.getProperties());

    this.instances = new HashSet<>();
  }

  public String[] arguments() {
    return arguments;
  }

  public String identifier() {
    return this.identifier;
  }

  public Instant startup() {
    return this.startup;
  }

  public Properties configuration() {
    return this.configuration;
  }

  public Set<PropertyCreator> instances() {
    return this.instances;
  }

  @Json.Property("upt")
  public Duration uptime() {
    return Duration.ofMillis(1);
  }
}
