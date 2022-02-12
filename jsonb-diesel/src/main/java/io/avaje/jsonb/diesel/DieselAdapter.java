package io.avaje.jsonb.diesel;

import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.diesel.read.JReader;
import io.avaje.jsonb.spi.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Jackson Core implementation of IOAdapter.
 */
public class DieselAdapter implements IOAdapter {

  //private final JsonFactory jsonFactory;
  private final boolean serializeNulls;
  private final boolean serializeEmpty;
  private final boolean failOnUnknown;

  /**
   * Create with the given default configuration.
   */
  public DieselAdapter() {
    this(false, false, false);
  }

  /**
   * Create additionally providing the jsonFactory.
   */
  public DieselAdapter(boolean serializeNulls, boolean serializeEmpty, boolean failOnUnknown) {
    this.serializeNulls = serializeNulls;
    this.serializeEmpty = serializeEmpty;
    this.failOnUnknown = failOnUnknown;
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
    JReader reader1 = Recycle.reader(json);
    return new JsonReadAdapter(reader1);
  }

  @Override
  public JsonReader reader(Reader reader) {
    return null;
//    try {
//      return new JacksonReader(jsonFactory.createParser(reader), failOnUnknown);
//    } catch (IOException e) {
//      throw new JsonIoException(e);
//    }
  }

  @Override
  public JsonReader reader(InputStream inputStream) {
    try {
      JReader reader1 = Recycle.reader(inputStream);
      return new JsonReadAdapter(reader1);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public JsonWriter writer(Writer writer) {
    return writer(new WriterOutputStream(writer, StandardCharsets.UTF_8));
  }

  @Override
  public JsonWriter writer(OutputStream outputStream) {
    return wrap(gen(outputStream));
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

  private JsonGenerator gen(OutputStream outputStream) {
    return Recycle.generator(outputStream);
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
