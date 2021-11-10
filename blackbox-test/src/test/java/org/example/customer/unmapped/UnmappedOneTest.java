package org.example.customer.unmapped;


import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UnmappedOneTest {

  Jsonb jsonb = Jsonb.newBuilder().build();
  JsonType<UnmappedOne> jsonType = jsonb.type(UnmappedOne.class);

  @Test
  void toJson_unmappedField_includedInJson() throws IOException {

    Map<String, Object> otherStuff = new LinkedHashMap<>();
    otherStuff.put("xone", 57);
    otherStuff.put("xtwo", Map.of("nm", "fred"));
    otherStuff.put("xthree", List.of("a", "b", "c"));

    UnmappedOne bean = new UnmappedOne().id(42).name("foo").unmapped(otherStuff);

    String asJson = jsonType.toJson(bean);

    assertThat(asJson).isEqualTo("{\"id\":42,\"name\":\"foo\",\"xone\":57,\"xtwo\":{\"nm\":\"fred\"},\"xthree\":[\"a\",\"b\",\"c\"]}");
  }


  @Test
  void fromJson_unmapped() throws IOException {

    String jsonContent = "{\"id\":42,\"name\":\"foo\",\"xone\":57,\"xtwo\":{\"nm\":\"fred\"},\"xthree\":[\"a\",\"b\",\"c\"]}";

    UnmappedOne unmappedOne = jsonType.fromJson(jsonContent);
    assertThat(unmappedOne.unmapped()).containsKeys("xone", "xtwo", "xthree");
  }
}
