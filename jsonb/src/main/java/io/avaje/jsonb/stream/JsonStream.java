package io.avaje.jsonb.stream;

import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.spi.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Default implementation of JsonStreamAdapter provided with Jsonb.
 */
public final class JsonStream implements JsonStreamAdapter {

  /**
   * Used to build JsonStream with custom settings.
   */
  public static final class Builder {

    private boolean serializeNulls;
    private boolean serializeEmpty;
    private boolean failOnUnknown;

    /**
     * Set to true to serialize nulls. Defaults to false.
     */
    public Builder serializeNulls(boolean serializeNulls) {
      this.serializeNulls = serializeNulls;
      return this;
    }

    /**
     * Set to true to serialize empty collections. Defaults to false.
     */
    public Builder serializeEmpty(boolean serializeEmpty) {
      this.serializeEmpty = serializeEmpty;
      return this;
    }

    /**
     * Set to true to fail on unknown properties. Defaults to false.
     */
    public Builder failOnUnknown(boolean failOnUnknown) {
      this.failOnUnknown = failOnUnknown;
      return this;
    }

    /**
     * Build and return the JsonStream.
     */
    public JsonStream build() {
      return new JsonStream(serializeNulls, serializeEmpty, failOnUnknown);
    }
  }

  private final boolean serializeNulls;
  private final boolean serializeEmpty;
  private final boolean failOnUnknown;

  /**
   * Create with the given default configuration.
   */
  public JsonStream() {
    this(false, false, false);
  }

  /**
   * Create additionally providing the jsonFactory.
   */
  public JsonStream(boolean serializeNulls, boolean serializeEmpty, boolean failOnUnknown) {
    this.serializeNulls = serializeNulls;
    this.serializeEmpty = serializeEmpty;
    this.failOnUnknown = failOnUnknown;
  }

  /**
   * Return a new builder to create a JsonStream with custom configuration.
   *
   * <pre>{@code
   *
   * var jsonStream = JsonStream.builder()
   *   .serializeNulls(true)
   *   .build();
   *
   * }</pre>
   */
  public static Builder builder() {
    return new Builder();
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
    JsonParser parser = Recycle.parser(json);
    return new JsonReadAdapter(parser, failOnUnknown);
  }

  @Override
  public JsonReader reader(Reader reader) {
    // TODO: Could recycle encoder and buffer
    return reader(new ReaderInputStream(reader, StandardCharsets.UTF_8));
  }

  @Override
  public JsonReader reader(InputStream inputStream) {
    JsonParser parser = Recycle.parser(inputStream);
    return new JsonReadAdapter(parser, failOnUnknown);
  }

  @Override
  public JsonWriter writer(Writer writer) {
    // TODO: Could recycle buffer used
    return writer(new WriterOutputStream(writer, StandardCharsets.UTF_8));
  }

  @Override
  public JsonWriter writer(OutputStream outputStream) {
    return writer(JsonOutput.of(outputStream));
  }

  @Override
  public JsonWriter writer(JsonOutput output) {
    return wrap(gen(output));
  }

  @Override
  public BufferedJsonWriter bufferedWriter() {
    JsonGenerator generator = Recycle.generator();
    return new BufferedWriter(wrap(generator), generator);
  }

  @Override
  public BytesJsonWriter bufferedWriterAsBytes() {
    JsonGenerator generator = Recycle.generator();
    return new BytesWriter(wrap(generator), generator);
  }

  private JsonGenerator gen(JsonOutput output) {
    return Recycle.generator(output);
  }

  private JsonWriteAdapter wrap(JsonGenerator generator) {
    return new JsonWriteAdapter(generator, serializeNulls, serializeEmpty);
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
