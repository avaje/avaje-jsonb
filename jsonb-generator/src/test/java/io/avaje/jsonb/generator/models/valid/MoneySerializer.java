package io.avaje.jsonb.generator.models.valid;

import java.math.BigDecimal;

import io.avaje.jsonb.CustomAdapter;
import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.Jsonb;

@CustomAdapter(global = false)
public class MoneySerializer implements JsonAdapter<java.math.BigDecimal> {

  public MoneySerializer(Jsonb jsonb) {}

  @Override
  public void toJson(JsonWriter writer, BigDecimal value) {}

  @Override
  public BigDecimal fromJson(JsonReader reader) {

    return null;
  }
}
