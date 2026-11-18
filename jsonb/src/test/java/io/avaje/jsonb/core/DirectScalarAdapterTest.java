package io.avaje.jsonb.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.AdapterFactory;
import io.avaje.jsonb.Jsonb;

class DirectScalarAdapterTest {

  private static final List<Class<?>> DIRECT_SCALARS = List.of(
    String.class,
    Boolean.class,
    boolean.class,
    Integer.class,
    int.class,
    Long.class,
    long.class,
    Double.class,
    double.class);

  @Test
  void rejectsDirectScalarAdapters() {
    for (Class<?> type : DIRECT_SCALARS) {
      assertThatThrownBy(() -> Jsonb.builder().add(type, adapter()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(type.getTypeName());
    }
  }

  @Test
  void rejectsDirectScalarSuppliersAndBuilders() {
    assertThatThrownBy(() -> Jsonb.builder().add(String.class, DirectScalarAdapterTest::adapter))
      .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> Jsonb.builder().add(String.class, jsonb -> adapter()))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsFactoryResultForDirectScalar() {
    AdapterFactory factory = (type, jsonb) -> type == String.class ? adapter() : null;

    assertThatThrownBy(() -> Jsonb.builder().add(factory).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining(String.class.getTypeName());
  }

  @Test
  void allowsCustomAdapterForOtherScalarTypes() {
    JsonAdapter<Float> adapter = new JsonAdapter<>() {
      @Override
      public void toJson(JsonWriter writer, Float value) {
        writer.value(value);
      }

      @Override
      public Float fromJson(JsonReader reader) {
        return (float) reader.readDouble();
      }
    };

    Jsonb jsonb = Jsonb.builder().add(Float.class, adapter).build();

    assertThat(jsonb.adapter(Float.class)).isSameAs(adapter);
  }

  private static JsonAdapter<Object> adapter() {
    return new JsonAdapter<>() {
      @Override
      public void toJson(JsonWriter writer, Object value) {
        writer.nullValue();
      }

      @Override
      public Object fromJson(JsonReader reader) {
        reader.skipValue();
        return null;
      }
    };
  }
}
