package io.avaje.jsonb.javalin;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import io.avaje.jsonb.Jsonb;
import io.javalin.json.JsonMapper;
import io.javalin.json.PipedStreamExecutor;

public class JavalinJsonb implements JsonMapper {
  protected final Jsonb jsonb;
  protected final boolean useVirtualThreads;
  protected PipedStreamExecutor pipedStreamExecutor = null;

  public JavalinJsonb() {
    this(Jsonb.instance(), false);
  }

  public JavalinJsonb(Jsonb jsonb) {
    this(jsonb, false);
  }

  public JavalinJsonb(boolean useVirtualThreads) {
    this(Jsonb.instance(), useVirtualThreads);
  }

  public JavalinJsonb(Jsonb jsonb, boolean useVirtualThreads) {
    this.jsonb = jsonb;
    this.useVirtualThreads = useVirtualThreads;
  }

  @Override
  public String toJsonString(Object obj, Type type) {
    if (obj instanceof String) {
      return (String) obj;
    } else {
      return jsonb.type(type).toJson(obj);
    }
  }

  @Override
  public InputStream toJsonStream(Object obj, Type type) {
    if (obj instanceof String) {
      return new ByteArrayInputStream(((String) obj).getBytes(StandardCharsets.UTF_8));
    } else {
      if (pipedStreamExecutor == null) pipedStreamExecutor = new PipedStreamExecutor(useVirtualThreads);
      return pipedStreamExecutor.getInputStream(outputStream -> {
        jsonb.type(type).toJson(obj, outputStream);
      });
    }
  }

  @Override
  public void writeToOutputStream(Stream<?> stream, OutputStream outputStream) {
    try (var writer = jsonb.writer(outputStream)){
      writer.beginArray();
      stream.forEach(it -> jsonb.toJson(it, writer));
      writer.endArray();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T fromJsonString(String json, Type targetType) {
    return (T) jsonb.type(targetType).fromJson(json);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T fromJsonStream(InputStream json, Type targetType) {
    return (T) jsonb.type(targetType).fromJson(json);
  }
}
