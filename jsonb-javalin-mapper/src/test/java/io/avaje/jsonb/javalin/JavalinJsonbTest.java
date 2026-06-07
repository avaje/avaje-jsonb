package io.avaje.jsonb.javalin;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.Jsonb;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.Unirest;

// This is a port of Javalin's JSON tests from Kotlin to Java
public class JavalinJsonbTest {
  @Json
  static class Foo {
    public final long value;

    Foo(long value) {
      this.value = value;
    }
  }

  static Javalin appWithJsonb() {
    return Javalin.create(config -> {
      config.jsonMapper(new JavalinJsonb());
    });
  }

  @Test
  @DisplayName("JavalinJsonb can convert a small Stream to JSON")
  void smallStreamToJSON() {
    List<Foo> source = List.of(new Foo(1_000_000), new Foo(1_000_001));
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new JavalinJsonb().writeToOutputStream(source.stream(), baos);
    assertThat(baos.toString(StandardCharsets.UTF_8)).isEqualTo("[{\"value\":1000000},{\"value\":1000001}]");
  }

  static class CountingOutputStream extends OutputStream {
    int count = 0;

    @Override
    public void write(int b) throws IOException {
      count++;
    }
  }

  @Test
  @DisplayName("JavalinJsonb can convert a large Stream to JSON")
  void largeStreamToJSON() {
    JsonMapper jsonMapper = new JavalinJsonb();
    long valueLength = 1_000_000;
    int oneElementLength = jsonMapper.toJsonString(new Foo(valueLength), Foo.class).length();
    int numElements = 50_000;
    Stream<Foo> sequence = LongStream.iterate(valueLength, i -> i + 1).mapToObj(i -> new Foo(i)).limit(numElements);
    CountingOutputStream countingOutputStream = new CountingOutputStream();
    jsonMapper.writeToOutputStream(sequence, countingOutputStream);
    int expectedCharacterCount = 2 + // bookend brackets
        (numElements - 1) + // commas
        oneElementLength * numElements; // elements
    assertThat(countingOutputStream.count).isEqualTo(expectedCharacterCount);
  }

  static String getBody(Javalin app, String endpoint) {
    return Unirest.get(String.format("http://localhost:%d%s", app.port(), endpoint)).asString().getBody();
  }

  static HttpRequestWithBody post(Javalin app, String endpoint) {
    return Unirest.post(String.format("http://localhost:%d%s", app.port(), endpoint));
  }

  @Test
  @DisplayName("user can serialize objects using avaje-jsonb mapper")
  void serialize() {
    Javalin app = appWithJsonb().start(0);
    app.unsafe.routes.get("/", context -> context.json(new SerializableObject()));
    assertThat(getBody(app, "/")).isEqualTo(Jsonb.instance().toJson(new SerializableObject()));
    app.stop();
  }

  @Test
  @DisplayName("user can deserialize objects using avaje-jsonb mapper")
  void deserialize() {
    Javalin app = appWithJsonb().start(0);
    app.unsafe.routes.post("/", context -> context.result(context.bodyAsClass(SerializableObject.class).value1));
    assertThat(post(app, "/").body(Jsonb.instance().toJson(new SerializableObject())).asString().getBody())
        .isEqualTo(new SerializableObject().value1);
    app.stop();
  }

  @Test
  @DisplayName("JavalinJsonb properly handles json stream")
  void stream() {
    Javalin app = appWithJsonb().start(0);
    app.unsafe.routes.get("/", context -> context.jsonStream(Stream.of("1")));
    assertThat(getBody(app, "/")).isEqualTo("[\"1\"]");
    app.stop();
  }

  @Test
  @DisplayName("toJsonStream treats Strings as already being json")
  void streamStrings() {
    Javalin app = appWithJsonb().start(0);
    app.unsafe.routes.get("/", context -> context.jsonStream("{a:b}"));
    assertThat(getBody(app, "/")).isEqualTo("{a:b}");
    app.stop();
  }

  @Test
  @DisplayName("toJson treats Strings as already being json")
  void stringStrings() {
    Javalin app = appWithJsonb().start(0);
    app.unsafe.routes.get("/", context -> context.json("{a:b}"));
    assertThat(getBody(app, "/")).isEqualTo("{a:b}");
    app.stop();
  }
}
