package io.avaje.jsonb.generator.models.valid;

import io.avaje.jsonb.Json;

@Json
@Json.SubType(type = MoveAccuracy.Unavoidable.class, name = "UNAVOIDABLE")
@Json.SubType(type = MoveAccuracy.Avoidable.class, name = "AVOIDABLE")
public interface MoveAccuracy {
  public static class Unavoidable implements MoveAccuracy {}

  public static class Avoidable implements MoveAccuracy {
    private int percentToHit;

    public int getPercentToHit() {
      return percentToHit;
    }

    public void setPercentToHit(int percentToHit) {
      this.percentToHit = percentToHit;
    }
  }
}
