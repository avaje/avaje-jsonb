package org.example.customer.customtype;

import io.avaje.json.*;
import io.avaje.jsonb.*;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class CustomScalarTypeTest {

  /**
   * Using the {@link CustomTypeComponent} which is service loaded.
   */
  @Test
  void toJson_fromJson_usingComponent() {
    Jsonb jsonb = Jsonb.builder()
      // service loading finds and loads CustomTypeComponent
      .build();

    MyWrapper wrapper = new MyWrapper(42, "hello", new MyCustomScalarType("hello".getBytes(StandardCharsets.UTF_8)));

    String asJson = jsonb.toJson(wrapper);
    assertThat(asJson).isEqualTo("{\"id\":42,\"base\":\"hello\",\"custom\":\"*** aGVsbG8=\"}");

    MyWrapper wrapper1 = jsonb.type(MyWrapper.class).fromJson(asJson);

    assertThat(wrapper1).isEqualTo(wrapper);
    assertThat(wrapper1.custom()).isEqualTo(wrapper.custom());
  }

  @Test
  void toJson_fromJson() {
    Jsonb jsonb = Jsonb.builder()
      // explicitly register the CustomTypeAdapter for our value type
      .add(MyCustomScalarType.class, new CustomTypeAdapter().nullSafe())
      .build();

    MyWrapper wrapper = new MyWrapper(42, "hello", new MyCustomScalarType("hello".getBytes(StandardCharsets.UTF_8)));

    String asJson = jsonb.toJson(wrapper);
    assertThat(asJson).isEqualTo("{\"id\":42,\"base\":\"hello\",\"custom\":\"aGVsbG8=\"}");

    MyWrapper wrapper1 = jsonb.type(MyWrapper.class).fromJson(asJson);

    assertThat(wrapper1).isEqualTo(wrapper);
    assertThat(wrapper1.custom()).isEqualTo(wrapper.custom());
  }

  @Test
  void toJson_fromJson_usingSupplier() {
    Jsonb jsonb = Jsonb.builder()
      // register a supplier
      .add(MyCustomScalarType.class, () -> new CustomTypeAdapter().nullSafe())
      .build();

    MyWrapper wrapper = new MyWrapper(42, "hello", new MyCustomScalarType("hello".getBytes(StandardCharsets.UTF_8)));

    String asJson = jsonb.toJson(wrapper);
    assertThat(asJson).isEqualTo("{\"id\":42,\"base\":\"hello\",\"custom\":\"aGVsbG8=\"}");

    MyWrapper wrapper1 = jsonb.type(MyWrapper.class).fromJson(asJson);

    assertThat(wrapper1).isEqualTo(wrapper);
    assertThat(wrapper1.custom()).isEqualTo(wrapper.custom());
  }

  static class CustomTypeAdapter implements JsonAdapter<MyCustomScalarType> {

    @Override
    public void toJson(JsonWriter writer, MyCustomScalarType value) {
      writer.value(value.toString());
    }

    @Override
    public MyCustomScalarType fromJson(JsonReader reader) {
      String encoded = reader.readString();
      return MyCustomScalarType.of(encoded);
    }
  }

}
