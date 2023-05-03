package io.avaje.jsonb.generator.models.valid;

import java.util.Set;

public class ExamplePacket extends Packet {

  private final Set<String> ids;
  private final Long longy;

  public ExamplePacket(Set<String> ids, Long longy) {
    this.ids = ids;
    this.longy = longy;
  }

  public Long getLongy() {
    return longy;
  }

  public Set<String> getIds() {
    return this.ids;
  }
}
