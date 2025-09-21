package io.avaje.jsonb.generator;

import com.fasterxml.jackson.annotation.JsonValue;
import io.avaje.jsonb.Json;
import io.avaje.prism.GeneratePrism;

@GeneratePrism(
    value = Json.Value.class,
    name = "AvajeValuePrism",
    superInterfaces = ValuePrism.class)
@GeneratePrism(
    value = JsonValue.class,
    name = "JacksonValuePrism",
    superInterfaces = ValuePrism.class)
public interface ValuePrism {
  String AVAJE_JSON_VALUE = "io.avaje.jsonb.Json.Value";
  String JACKSON_JSON_VALUE = "com.fasterxml.jackson.annotation.JsonValue";
}
