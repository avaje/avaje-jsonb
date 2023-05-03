package io.avaje.jsonb.generator.models.valid;

import io.avaje.jsonb.Json.JsonAlias;

public class Example2Packet extends Packet {
  @JsonAlias("packer")
  private final String ids;
  @JsonAlias("long-ring-long-land")
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
