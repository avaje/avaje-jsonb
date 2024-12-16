package io.avaje.json.node;

import io.avaje.json.JsonWriter;
import io.avaje.json.simple.SimpleMapper;
import io.avaje.json.stream.JsonStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

class NodeMapperTest {

  static final JsonNodeMapper mapper = JsonNodeMapper.builder().build();
  static final SimpleMapper.Type<JsonNode> nodeMapper = mapper.nodeMapper();

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
    SimpleMapper.Type<JsonObject> objectMapper = mapper.objectMapper();

    JsonObject jsonObject = objectMapper.fromJson("{\"greet\":\"hi\"}");
    assertThat(jsonObject.toString()).isEqualTo("{greet=hi}");
  }

  @Test
  void arrayMapper() {
    SimpleMapper.Type<JsonArray> arrayMapper = mapper.arrayMapper();

    JsonArray jsonArray = arrayMapper.fromJson("[\"a\",\"b\",\"c\"]");
    assertThat(jsonArray.toString()).isEqualTo("[a, b, c]");
  }

}
