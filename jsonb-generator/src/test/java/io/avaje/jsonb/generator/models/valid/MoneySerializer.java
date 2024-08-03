package io.avaje.jsonb.generator.models.valid;

import java.math.BigDecimal;

import io.avaje.jsonb.CustomAdapter;
import io.avaje.jsonb.JsonAdapter;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.Jsonb;

@CustomAdapter(global = false)
public class MoneySerializer implements JsonAdapter<BigDecimal> {

  public MoneySerializer(Jsonb jsonb) {}

  @Override
  public void toJson(JsonWriter writer, BigDecimal value) {}

  @Override
  public BigDecimal fromJson(JsonReader reader) {

    return null;
  }
}
