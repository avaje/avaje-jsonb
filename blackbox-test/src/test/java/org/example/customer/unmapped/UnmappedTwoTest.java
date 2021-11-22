package org.example.customer.unmapped;


import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UnmappedTwoTest {

  Jsonb jsonb = Jsonb.newBuilder().build();
  JsonType<UnmappedTwo> jsonType = jsonb.type(UnmappedTwo.class);

  @Test
  void toJson_unmappedField_includedInJson()  {

    Map<String, Object> otherStuff = new LinkedHashMap<>();
    otherStuff.put("xone", 57);
    otherStuff.put("xtwo", Map.of("nm", "fred"));
    otherStuff.put("xthree", List.of("a", "b", "c"));

    UnmappedTwo bean = new UnmappedTwo(42, "foo", otherStuff);

    String asJson = jsonType.toJson(bean);

    // unmapped not included via explicit @Json.Ignore
    assertThat(asJson).isEqualTo("{\"id\":42,\"name\":\"foo\"}");
  }


  @Test
  void fromJson_unmapped()  {

    String jsonContent = "{\"id\":42,\"name\":\"foo\",\"xone\":57,\"xtwo\":{\"nm\":\"fred\"},\"xthree\":[\"a\",\"b\",\"c\"]}";

    UnmappedTwo unmappedOne = jsonType.fromJson(jsonContent);
    assertThat(unmappedOne.unmapped()).containsKeys("xone", "xtwo", "xthree");
  }
}
