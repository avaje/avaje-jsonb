package io.avaje.jsonb.jakarta;

import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.spi.BufferedJsonWriter;
import io.avaje.jsonb.spi.BytesJsonWriter;
import io.avaje.jsonb.spi.IOAdapter;
import io.avaje.jsonb.spi.PropertyNames;
import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;

import java.io.*;

public final class JakartaIOAdapter implements IOAdapter {

  private final boolean serializeNulls;
  private final boolean serializeEmpty;
  private final boolean failOnUnknown;
  private final JsonProvider provider;
  private final BufferRecycler bufferRecycler;

  /**
   * Create with the given configuration.
   */
  public JakartaIOAdapter(boolean serializeNulls, boolean serializeEmpty, boolean failOnUnknown) {
    this.serializeNulls = serializeNulls;
    this.serializeEmpty = serializeEmpty;
    this.failOnUnknown = failOnUnknown;
    this.provider = JsonProvider.provider();
    this.bufferRecycler = init();
  }

  private BufferRecycler init() {
    if (!Boolean.getBoolean("avaje.jsonb.disableThreadLocalBuffering")) {
      return BufferRecycleProvider.provide();
    }
    return new BufferRecyclerAtomic();
  }

  /**
   * Create with default configuration - no serialization of nulls or empty and not fail on unknown.
   */
  public JakartaIOAdapter() {
    this(false, false, false);
  }

  @Override
  public PropertyNames properties(String... names) {
    JsonGenerator.Key[] keys = new JsonGenerator.Key[names.length];
    for (int i = 0; i < names.length; i++) {
      keys[i] = provider.createGeneratorKey(names[i]);
    }
    return new DPropertyNames(keys);
  }

  private JsonReader reader(JsonParser parser) {
    return new JakartaJsonReader(parser, failOnUnknown);
  }

  @Override
  public JsonReader reader(String json) {
    return reader(provider.createParser(new StringReader(json)));
  }

  @Override
  public JsonReader reader(byte[] json) {
    return reader(provider.createParser(new ByteArrayInputStream(json)));
  }

  @Override
  public JsonReader reader(Reader reader) {
    return reader(provider.createParser(reader));
  }

  @Override
  public JsonReader reader(InputStream inputStream) {
    return reader(provider.createParser(inputStream));
  }

  private JsonWriter writer(JsonGenerator generator) {
    return new JakartaJsonWriter(generator, serializeNulls, serializeEmpty);
  }

  @Override
  public JsonWriter writer(Writer writer) {
    return writer(provider.createGenerator(writer));
  }

  @Override
  public JsonWriter writer(OutputStream outputStream) {
    return writer(provider.createGenerator(outputStream));
  }

  @Override
  public BufferedJsonWriter bufferedWriter() {
    final TextBufferWriter bufferWriter = new TextBufferWriter(new TextBuffer(bufferRecycler));
    return new TextBufferJsonWriter(writer(provider.createGenerator(bufferWriter)), bufferWriter);

    //
//    // review this, no buffer recycling option?
//    //final SBWriter buffer = new SBWriter(100);
//    return new PBufferedJsonWriter3(writer(provider.createGenerator(bufferWriter)), bufferWriter);

//    // review this, no buffer recycling option?
//    final SBWriter buffer = new SBWriter(100);
//    return new PBufferedJsonWriter2(writer(provider.createGenerator(buffer)), buffer);
//
//    //final SBWriterPool.Buffer buffer = writerPool.take();
//    return new PBufferedJsonWriter(writer(provider.createGenerator(buffer.writer())), buffer);
  }

  @Override
  public BytesJsonWriter bufferedWriterAsBytes() {
    // review this, no buffer recycling option?
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream(200);
    return new PBytesJsonWriter(writer(provider.createGenerator(buffer)), buffer);
  }

}
