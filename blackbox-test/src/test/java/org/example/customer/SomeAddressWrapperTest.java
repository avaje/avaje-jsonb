package org.example.customer;

import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SomeAddressWrapperTest {

  Jsonb jsonb = Jsonb.newBuilder().build();

  @Test
  void test_when_null() {

    var type = jsonb.type(SomeAddressWrapper.class);
    String asJson = type.toJson(new SomeAddressWrapper(43L, null, Collections.emptyList()));
    assertThat(asJson).isEqualTo("{\"id\":43}");

    var myList = List.of( new SomeAddressWrapper(43L, null, Collections.emptyList()),  new SomeAddressWrapper(44L, null, Collections.emptyList()));

    String asJsonList = type.list().toJson(myList);
    assertThat(asJsonList).isEqualTo("[{\"id\":43},{\"id\":44}]");
  }

  @Test
  void includeNull() {

    StringWriter sw = new StringWriter();
    JsonWriter writer = jsonb.writer(sw);
    writer.serializeNulls(true);
    writer.serializeEmpty(true);

    var type = jsonb.type(SomeAddressWrapper.class);
    type.toJson(writer, new SomeAddressWrapper(43L, null, Collections.emptyList()));
    writer.close();
    assertThat(sw.toString()).isEqualTo("{\"id\":43,\"address\":null,\"tags\":[]}");
  }


  @Test
  void includeNull_viaJsonB() {

    Jsonb jsonb = Jsonb.newBuilder().serializeNulls(true).build();

    var type = jsonb.type(SomeAddressWrapper.class);
    String asJson = type.toJson(new SomeAddressWrapper(43L, null, Collections.emptyList()));
    assertThat(asJson).isEqualTo("{\"id\":43,\"address\":null}");
  }


  @Test
  void includeEmpty_viaJsonB() {

    Jsonb jsonb = Jsonb.newBuilder().serializeEmpty(true).build();

    var type = jsonb.type(SomeAddressWrapper.class);
    String asJson = type.toJson(new SomeAddressWrapper(43L, null, Collections.emptyList()));
    assertThat(asJson).isEqualTo("{\"id\":43,\"tags\":[]}");
  }


  @Test
  void includeNullAndEmpty_viaJsonB() {

    Jsonb jsonb = Jsonb.newBuilder().serializeNulls(true).serializeEmpty(true).build();

    var type = jsonb.type(SomeAddressWrapper.class);
    String asJson = type.toJson(new SomeAddressWrapper(43L, null, Collections.emptyList()));
    assertThat(asJson).isEqualTo("{\"id\":43,\"address\":null,\"tags\":[]}");
  }
}
