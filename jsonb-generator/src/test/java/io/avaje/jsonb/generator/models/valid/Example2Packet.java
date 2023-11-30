package io.avaje.jsonb.generator.models.valid;

import io.avaje.jsonb.Json;

public class Example2Packet extends Packet {

  private final String ids;
  @Json.Alias("long-ring-long-land")
  private Long longy;

  public Example2Packet(String ids, Long longy) {
    this.ids = ids;
    this.longy = longy;
  }

  public Long getLongy() {
    return longy;
  }

  public void setLongy(Long longy) {
    this.longy = longy;
  }

  public String getIds() {
    return ids;
  }
}
