package org.example.customer.raw;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

class WithRawContentTest {

  Jsonb jsonb = Jsonb.newBuilder().build();
  JsonType<WithRawContent> withRawType = jsonb.type(WithRawContent.class);

  @Test
  void toJson() {
    String rawJsonContent = "{\"foo\": 99, \"bar\": \"aaa\", \"bazz\":[1,2,3]}";

    WithRawContent withRaw = new WithRawContent(42, "rob");
    withRaw.content(rawJsonContent);

    String asJson = jsonb.toJson(withRaw);
    assertThat(asJson).isEqualTo("{\"id\":42,\"name\":\"rob\",\"content\":{\"foo\": 99, \"bar\": \"aaa\", \"bazz\":[1,2,3]}}");
  }

  @Test
  void toJson_when_null() {
    WithRawContent withRaw = new WithRawContent(42, "rob");
    String asJson = jsonb.toJson(withRaw);

    assertThat(asJson).isEqualTo("{\"id\":42,\"name\":\"rob\"}");
  }

  @Test
  void toJson_array() {
    WithRawContent withRaw = new WithRawContent(42, "rob");
    withRaw.content(" [ 1 , 2 , 3 ] ");

    String asJson = jsonb.toJson(withRaw);
    assertThat(asJson).isEqualTo("{\"id\":42,\"name\":\"rob\",\"content\": [ 1 , 2 , 3 ] }");
  }

  @Test
  void toFromJson_when_null() {
    WithRawContent withRaw = new WithRawContent(42, "rob");

    String asJson = jsonb.toJson(withRaw);
    assertThat(asJson).isEqualTo("{\"id\":42,\"name\":\"rob\"}");

    WithRawContent bean = withRawType.fromJson(asJson);
    String content = bean.content();
    assertThat(content).isNull();
  }

  @Test
  void toFromJson_when_literalNull() {

    WithRawContent withRaw = new WithRawContent(42, "rob");
    withRaw.content("null");

    String asJson = jsonb.toJson(withRaw);
    assertThat(asJson).isEqualTo("{\"id\":42,\"name\":\"rob\"}");

    WithRawContent bean = withRawType.fromJson(asJson);
    String content = bean.content();
    assertThat(content).isNull();
  }

  @Test
  void toFromJson() {
    toFromJsonWith("{\"foo\":99}");
    toFromJsonWith("[1,2,3]");
    toFromJsonWith("42");
    toFromJsonWith("true");
    toFromJsonWith("false");
    toFromJsonWith("{\"foo\": 99, \"bar\": \"aaa\", \"bazz\":[1,2,3]}");
  }

  @Test
  void toFromJson_with_stream() {
    toFromJsonWith("{\"foo\":99}", true);
    toFromJsonWith("[1,2,3]", true);
    toFromJsonWith("42", true);
    toFromJsonWith("true", true);
    toFromJsonWith("false", true);
    toFromJsonWith("{\"foo\": 99, \"bar\": \"aaa\", \"bazz\":[1,2,3]}", true);
  }

  private void toFromJsonWith(String rawJsonContent) {
    toFromJsonWith(rawJsonContent, false);
  }

  private void toFromJsonWith(String rawJsonContent, boolean stream) {
    WithRawContent withRaw = new WithRawContent(42, "rob");
    withRaw.content(rawJsonContent);

    String asJson = jsonb.toJson(withRaw);
    assertThat(asJson).isEqualTo("{\"id\":42,\"name\":\"rob\",\"content\":" + rawJsonContent + "}");

    WithRawContent bean;
    if (stream) {
      bean = withRawType.fromJson(new StringReader(asJson));
    } else {
      bean = withRawType.fromJson(asJson);
    }
    String content = bean.content();
    assertThat(content).isEqualTo(rawJsonContent);
  }
}
