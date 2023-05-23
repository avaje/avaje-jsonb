package io.avaje.jsonb.generator.models.valid;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.Json.Import;

@Import(
    value = Packet.class,
    jsonSettings = @Json(naming = Json.Naming.LowerUnderscore, typeProperty = "type"),
    subtypes = {
      @Json.SubType(type = ExamplePacket.class, name = "example"),
      @Json.SubType(type = Example2Packet.class, name = "example_2"),
      @Json.SubType(type = Example3Packet.class)
    })
public abstract class Packet {}
