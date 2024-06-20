package org.example.customer.creator;

import io.avaje.jsonb.Json;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Json
public final class PropertyCreator {

  @Json.Property("uid")
  private final String identifier;

  @Json.Property("stp")
  private final Instant startup;

  @Json.Creator
  public PropertyCreator(
    String identifier,
    Instant startup) {
    this.identifier = identifier;
    this.startup = startup;
  }

  public PropertyCreator(Instant startup) {
    this.identifier = Instant.now().toEpochMilli() + "-" + UUID.randomUUID();
    this.startup = startup;
  }

  public String identifier() {
    return this.identifier;
  }

  public Instant startup() {
    return this.startup;
  }

  @Json.Property("upt")
  public Duration uptime() {
    return Duration.ofMillis(1);
  }
}
