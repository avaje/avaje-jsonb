package io.avaje.json.mapper;


import io.avaje.json.JsonReader;
import io.avaje.json.stream.BufferedJsonWriter;
import io.avaje.json.stream.JsonStream;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonMapperTest {

  static final JsonMapper mapper = JsonMapper.builder().build();

  @Test
  void mapToJsonFromJson() {

    Map<String, Long> map = new LinkedHashMap<>();
    map.put("one", 45L);
    map.put("two", 93L);

    String asJson = mapper.toJson(map);
    assertThat(asJson).isEqualTo("{\"one\":45,\"two\":93}");

    Map<String, Object> mapFromJson = mapper.fromJsonObject(asJson);

    assertThat(mapFromJson).containsKeys("one", "two");
    assertThat(mapFromJson.toString()).isEqualTo("{one=45, two=93}");

    Map<String, Object> mapFromJson2 = mapper.map().fromJson(asJson);
    assertThat(mapFromJson2).isEqualTo(mapFromJson);

    JsonStream jsonStream = JsonStream.builder().build();
    try (JsonReader reader = jsonStream.reader(asJson)) {
      Map<String, Object> mapFromJson3 = mapper.fromJsonObject(reader);
      assertThat(mapFromJson3).isEqualTo(mapFromJson);
    }
  }

  @Test
  void toJsonWriter_scalar() {
    JsonStream jsonStream = JsonStream.builder().build();
    BufferedJsonWriter writer0 = jsonStream.bufferedWriter();
    mapper.toJson("hi", writer0);
    assertThat(writer0.result()).isEqualTo("\"hi\"");
  }

  @Test
  void toJsonWriter_map() {
    JsonStream jsonStream = JsonStream.builder().build();
    BufferedJsonWriter writer0 = jsonStream.bufferedWriter();
    mapper.toJson(Map.of("key", 0), writer0);
    assertThat(writer0.result()).isEqualTo("{\"key\":0}");
  }

  @Test
  void toJsonWriter_list() {
    JsonStream jsonStream = JsonStream.builder().build();
    BufferedJsonWriter writer0 = jsonStream.bufferedWriter();
    mapper.toJson(List.of("a", 0), writer0);
    assertThat(writer0.result()).isEqualTo("[\"a\",0]");
  }

  @Test
  void nullDirectly() {
    var mapFromJson = mapper.fromJson("null");
    assertThat(mapFromJson).isNull();
  }

  @Test
  void objectJsonReader() {
    try (var reader = JsonStream.builder().build().reader("\"hi\"")) {
      var fromJson = mapper.fromJson(reader);
      assertThat(fromJson).isEqualTo("hi");
    }
  }

  @Test
  void mapWithNull() {
    Map<String, Object> mapFromJson = mapper.fromJsonObject("{\"one\":1,\"two\":null,\"three\":3}");

    assertThat(mapFromJson).containsKeys("one", "two", "three");
    assertThat(mapFromJson.toString()).isEqualTo("{one=1, two=null, three=3}");

    assertThat(mapper.toJson(mapFromJson)).isEqualTo("{\"one\":1,\"three\":3}");
  }

  @Test
  void listWithNull() {
    List<Object> listFromJson = mapper.fromJsonArray("[1,null,3]");

    assertThat(listFromJson).hasSize(3);
    assertThat(listFromJson.get(1)).isNull();

    assertThat(mapper.toJson(listFromJson)).isEqualTo("[1,3]");
  }

  @Test
  void listWithReader() {
    try (JsonReader reader = JsonStream.builder().build().reader("[1,2]")) {
      List<Object> listFromJson = mapper.fromJsonArray(reader);

      assertThat(listFromJson).hasSize(2);
      assertThat(mapper.toJson(listFromJson)).isEqualTo("[1,2]");
    }
  }

  @Test
  void arrayToJsonFromJson() {

    Map<String, Long> map0 = new LinkedHashMap<>();
    map0.put("one", 45L);
    map0.put("two", 93L);

    Map<String, Long> map1 = new LinkedHashMap<>();
    map1.put("one", 27L);

    List<Map<String, Long>> list = List.of(map0, map1);

    String asJson = mapper.toJson(list);
    assertThat(asJson).isEqualTo("[{\"one\":45,\"two\":93},{\"one\":27}]");

    List<Object> listFromJson = mapper.fromJsonArray(asJson);

    assertThat(listFromJson).hasSize(2);
    assertThat(listFromJson.toString()).isEqualTo("[{one=45, two=93}, {one=27}]");

    List<Object> list2 = mapper.list().fromJson(asJson);
    assertThat(list2).isEqualTo(listFromJson);
  }

  @Test
  void extract_example() {
    String json = "{\"name\":\"Rob\",\"score\":4.5,\"whenActive\":\"2025-10-20\",\"address\":{\"street\":\"Pall Mall\"}}";
    Map<String, Object> mapFromJson = mapper.fromJsonObject(json);

    JsonExtract extract = mapper.extract(mapFromJson);

    String name = extract.extract("name");
    double score = extract.extract("score", -1D);
    String street = extract.extract("address.street");
    LocalDate activeDate = extract.extractOrEmpty("whenActive")
      .map(LocalDate::parse)
      .orElseThrow();

    assertThat(name).isEqualTo("Rob");
    assertThat(score).isEqualTo(4.5D);
    assertThat(street).isEqualTo("Pall Mall");
    assertThat(activeDate).isEqualTo(LocalDate.parse("2025-10-20"));
  }

  @Test
  void extract() {
    String json = "{\"one\":1,\"two\":4.5,\"three\":3,\"four\":\"2025-10-20\",\"five\":true}";
    Map<String, Object> mapFromJson = mapper.fromJsonObject(json);

    JsonExtract extract = mapper.extract(mapFromJson);
    assertThat(extract.extract("one", 0)).isEqualTo(1);
    assertThat(extract.extract("two", 0D)).isEqualTo(4.5D);
    assertThat(extract.extract("three", 0L)).isEqualTo(3L);
    assertThat(extract.extract("four")).isEqualTo("2025-10-20");
    assertThat(extract.extract("four", "NA")).isEqualTo("2025-10-20");
    assertThat(extract.extract("five", false)).isTrue();

    LocalDate fourAsLocalDate = extract.extractOrEmpty("four")
      .map(LocalDate::parse)
      .orElseThrow();

    assertThat(fourAsLocalDate)
      .isEqualTo(LocalDate.parse("2025-10-20"));

  }

  @Test
  void JsonExtractOf() {
    String json = "{\"one\":1}";
    Map<String, Object> mapFromJson = mapper.fromJsonObject(json);

    JsonExtract extract = JsonExtract.of(mapFromJson);
    assertThat(extract.extract("one", 0)).isEqualTo(1);
  }

  @Test
  void extract_whenMissing() {
    String json = "{}";
    Map<String, Object> mapFromJson = mapper.fromJsonObject(json);

    JsonExtract extract = mapper.extract(mapFromJson);
    assertThat(extract.extract("one", 0)).isEqualTo(0);
    assertThat(extract.extract("two", 0D)).isEqualTo(0D);
    assertThat(extract.extract("three", 0L)).isEqualTo(0L);
    assertThat(extract.extract("four", "NA")).isEqualTo("NA");
    assertThat(extract.extract("five", false)).isFalse();

    assertThatThrownBy(() -> extract.extract("four"))
      .isInstanceOf(IllegalArgumentException.class);

    LocalDate fourAsLocalDate = extract.extractOrEmpty("four")
      .map(LocalDate::parse)
      .orElse(LocalDate.of(1970, 1, 21));

    assertThat(fourAsLocalDate).isEqualTo(LocalDate.parse("1970-01-21"));
  }

  @Test
  void extractNumber_whenNotANumber_expect_missingValue() {
    String json = "{\"text\":\"foo\",\"bool\":true,\"isNull\":null}";
    Map<String, Object> mapFromJson = mapper.fromJsonObject(json);

    JsonExtract extract = mapper.extract(mapFromJson);
    assertThat(extract.extract("text", 7)).isEqualTo(7);
    assertThat(extract.extract("text", 7L)).isEqualTo(7L);
    assertThat(extract.extract("text", 7.4D)).isEqualTo(7.4D);
    assertThat(extract.extract("bool", 7)).isEqualTo(7);
    assertThat(extract.extract("bool", 7L)).isEqualTo(7L);
    assertThat(extract.extract("bool", 7.4D)).isEqualTo(7.4D);
    assertThat(extract.extract("isNull", 7)).isEqualTo(7);
    assertThat(extract.extract("isNull", 7L)).isEqualTo(7L);
    assertThat(extract.extract("isNull", 7.4D)).isEqualTo(7.4D);
  }

  @Test
  void extract_nestedPath() {
    String json = "{\"outer\":{\"a\":\"v0\", \"b\":1, \"c\":true,\"d\":{\"x\":\"x0\",\"y\":42,\"date\":\"2025-10-20\"}}}";
    Map<String, Object> mapFromJson = mapper.fromJsonObject(json);

    JsonExtract extract = mapper.extract(mapFromJson);
    assertThat(extract.extract("outer.b", 0)).isEqualTo(1);
    assertThat(extract.extract("outer.d.y", 0)).isEqualTo(42);
    assertThat(extract.extract("outer.d.y", "junk")).isEqualTo("42");
    assertThat(extract.extract("outer.a", "NA")).isEqualTo("v0");

    assertThat(extract.extract("outer.d.y", 0L)).isEqualTo(42L);
    assertThat(extract.extract("outer.d.y", 0D)).isEqualTo(42D);
    assertThat(extract.extract("outer.c", false)).isTrue();

    assertThat(extract.extract("outer.c")).isEqualTo("true");

    LocalDate fourAsLocalDate = extract.extractOrEmpty("outer.d.date")
      .map(LocalDate::parse)
      .orElse(LocalDate.of(1970, 1, 21));

    assertThat(fourAsLocalDate).isEqualTo(LocalDate.parse("2025-10-20"));
  }
}
