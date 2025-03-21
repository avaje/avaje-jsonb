package io.avaje.json.stream.core;

import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.PropertyNames;
import io.avaje.json.stream.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

/** Default implementation of JsonStreamAdapter provided with Jsonb. */
final class CoreJsonStream implements JsonStream {

  private final boolean serializeNulls;
  private final boolean serializeEmpty;
  private final boolean failOnUnknown;
  private final boolean failOnNullPrimitives;
  private final BufferRecycler recycle;

  /** Create additionally providing the jsonFactory. */
  CoreJsonStream(
      boolean serializeNulls,
      boolean serializeEmpty,
      boolean failOnUnknown,
      boolean failOnNullPrimitives,
      BufferRecycleStrategy recycle) {
    this.serializeNulls = serializeNulls;
    this.serializeEmpty = serializeEmpty;
    this.failOnUnknown = failOnUnknown;
    this.failOnNullPrimitives = failOnNullPrimitives;
    this.recycle = init2Recycler(recycle);
  }

  private static BufferRecycler init2Recycler(BufferRecycleStrategy recycle) {
    switch (recycle) {
      case NO_RECYCLING: return BufferRecycler.nonRecyclingPool();
      case LOCK_FREE: return BufferRecycler.lockFreePool();
      case THREAD_LOCAL: return BufferRecycler.threadLocalPool();
      case HYBRID_POOL: return BufferRecycler.hybrid();
      default:
        throw new IllegalStateException();
    }
  }

  /**
   * Return a new builder to create a JsonStream with custom configuration.
   *
   * <pre>{@code
   * var jsonStream = JsonStream.builder()
   *   .serializeNulls(true)
   *   .build();
   *
   * }</pre>
   */
  public static JsonStreamBuilder builder() {
    return new JsonStreamBuilder();
  }

  @Override
  public PropertyNames properties(String... names) {
    return JsonNames.of(names);
  }

  @Override
  public JsonReader reader(String json) {
    return reader(json.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public JsonReader reader(byte[] json) {
    JsonParser parser = recycle.parser(json);
    return new JsonReadAdapter(parser, recycle, failOnUnknown, failOnNullPrimitives);
  }

  @Override
  public JsonReader reader(Reader reader) {
    return reader(new ReaderInputStream(reader, StandardCharsets.UTF_8));
  }

  @Override
  public JsonReader reader(InputStream inputStream) {
    JsonParser parser = recycle.parser(inputStream);
    return new JsonReadAdapter(parser, recycle, failOnUnknown, failOnNullPrimitives);
  }

  @Override
  public JsonWriter writer(Writer writer) {
    // TODO: Could recycle buffer used
    return writer(new WriterOutputStream(writer, StandardCharsets.UTF_8));
  }

  @Override
  public JsonWriter writer(OutputStream outputStream) {
    return writer(JsonOutput.ofStream(outputStream));
  }

  @Override
  public JsonWriter writer(JsonOutput output) {
    return wrap(gen(output));
  }

  @Override
  public BufferedJsonWriter bufferedWriter() {
    JsonGenerator generator = recycle.generator();
    return new BufferedWriter(wrap(generator), generator);
  }

  @Override
  public BytesJsonWriter bufferedWriterAsBytes() {
    JsonGenerator generator = recycle.generator();
    return new BytesWriter(wrap(generator), generator);
  }

  private JsonGenerator gen(JsonOutput output) {
    return recycle.generator(output);
  }

  private JsonWriteAdapter wrap(JsonGenerator generator) {
    return new JsonWriteAdapter(generator, recycle, serializeNulls, serializeEmpty);
  }

  private static class BufferedWriter extends DelegateJsonWriter implements BufferedJsonWriter {

    private final JsonGenerator generator;

    BufferedWriter(JsonWriteAdapter delegate, JsonGenerator generator) {
      super(delegate);
      this.generator = generator;
    }

    @Override
    public String result() {
      return generator.toString();
    }
  }

  private static class BytesWriter extends DelegateJsonWriter implements BytesJsonWriter {

    private final JsonGenerator generator;

    public BytesWriter(JsonWriteAdapter delegate, JsonGenerator generator) {
      super(delegate);
      this.generator = generator;
    }

    @Override
    public byte[] result() {
      return generator.toByteArray();
    }
  }
}
