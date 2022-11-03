package io.avaje.jsonb.generator.models.valid;

import io.avaje.jsonb.Json;

@Json.Import(MixinTarget.class)
@Json.MixIn(MixinTarget.class)
public abstract class MixinClass {

  @Json.Property("among us")
  private String st;

  @Json.Ignore private String av;
}
