package io.avaje.json.node;

import io.avaje.json.JsonWriter;
import io.avaje.json.mapper.JsonMapper;
import io.avaje.json.stream.JsonStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

import io.avaje.json.stream.JsonStream;

import static org.assertj.core.api.Assertions.assertThat;

class NodeMapperTest {

  static final JsonNodeMapper mapper = JsonNodeMapper.builder().build();
  static final JsonMapper.Type<JsonNode> nodeMapper = mapper.nodeMapper();

  @Test
  void toJson() {
    var obj = JsonObject.create().add("greet", "hi");

    var asJson = nodeMapper.toJson(obj);
    assertThat(asJson).isEqualTo("{\"greet\":\"hi\"}");
  }

  @Test
  void toJsonPretty() {
    var obj = JsonObject.create().add("greet", "hi");

    String asJsonPretty = nodeMapper.toJsonPretty(obj);
    assertThat(asJsonPretty).isEqualTo("{\n" +
      "  \"greet\": \"hi\"\n" +
      "}");
  }

  @Test
  void toJsonWriter() {
    var obj = JsonObject.create().add("greet", "hi");

    StringWriter writer = new StringWriter();
    nodeMapper.toJson(obj, writer);
    assertThat(writer.toString()).isEqualTo("{\"greet\":\"hi\"}");
  }

  @Test
  void toJsonWriteBytes() {
    var obj = JsonObject.create().add("greet", "hi");

    var baos = new ByteArrayOutputStream();
    nodeMapper.toJson(obj, baos);
    String asStr = baos.toString();
    assertThat(asStr).isEqualTo("{\"greet\":\"hi\"}");
  }

  @Test
  void toJsonBytes() {
    var obj = JsonObject.create().add("greet", "hi");

    var bytes = nodeMapper.toJsonBytes(obj);
    String asStr = new String(bytes);
    assertThat(asStr).isEqualTo("{\"greet\":\"hi\"}");
  }

  @Test
  void toJsonUsingJsonWriter() {
    var obj = JsonObject.create().add("greet", "hi");

    JsonStream otherJsonStream = JsonStream.builder().build();

    StringWriter stringWriter = new StringWriter();
    try (JsonWriter jsonWriter = otherJsonStream.writer(stringWriter)) {
      nodeMapper.toJson(obj, jsonWriter);
    }
    assertThat(stringWriter.toString()).isEqualTo("{\"greet\":\"hi\"}");
  }

  @Test
  void objectMapper() {
    JsonMapper.Type<JsonObject> objectMapper = mapper.objectMapper();

    JsonObject jsonObject = objectMapper.fromJson("{\"greet\":\"hi\"}");
    assertThat(jsonObject.toString()).isEqualTo("{greet=hi}");
  }

  @Test
  void arrayMapper() {
    JsonMapper.Type<JsonArray> arrayMapper = mapper.arrayMapper();

    JsonArray jsonArray = arrayMapper.fromJson("[\"a\",\"b\",\"c\"]");
    assertThat(jsonArray.toString()).isEqualTo("[a, b, c]");
  }

  @Test
  void fromJson_largeDecimal_withExtendedDigitLimit() {
    // default limit is 100 digits; use maxNumberDigits(200) to allow the large decimal
    JsonStream jsonStream = JsonStream.builder().maxNumberDigits(200).build();
    JsonNode result = JsonNodeMapper.builder().jsonStream(jsonStream).build()
      .fromJson("1.0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001");
    // default NumberAdapter reads as double — precision beyond double is lost,
    // 1.000...001 becomes 1.0 which is integral, so returns JsonLong(1)
    assertThat(result).isInstanceOf(JsonLong.class);
    assertThat(((JsonLong) result).longValue()).isEqualTo(1L);
  }

}
