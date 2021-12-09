package io.avaje.jsonb.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import io.avaje.jsonb.JsonIoException;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.spi.BufferedJsonWriter;
import io.avaje.jsonb.spi.BytesJsonWriter;
import io.avaje.jsonb.spi.IOAdapter;
import io.avaje.jsonb.spi.PropertyNames;

import java.io.*;

/**
 * Jackson Core implementation of IOAdapter.
 */
class JacksonAdapter implements IOAdapter {

  private final JsonFactory jsonFactory;
  private final boolean serializeNulls;
  private final boolean serializeEmpty;
  private final boolean failOnUnknown;

  /**
   * Create with the given default configuration.
   */
  public JacksonAdapter(boolean serializeNulls, boolean serializeEmpty, boolean failOnUnknown) {
    this.serializeNulls = serializeNulls;
    this.serializeEmpty = serializeEmpty;
    this.failOnUnknown = failOnUnknown;
    this.jsonFactory = new JsonFactory();
  }

  @Override
  public PropertyNames properties(String... names) {
    return new JacksonNames(names);
  }

  @Override
  public JsonReader reader(String json) {
    try {
      return new JacksonReader(jsonFactory.createParser(json), failOnUnknown);
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public JsonReader reader(byte[] json) {
    try {
      return new JacksonReader(jsonFactory.createParser(json), failOnUnknown);
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public JsonReader reader(Reader reader) {
    try {
      return new JacksonReader(jsonFactory.createParser(reader), failOnUnknown);
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public JsonReader reader(InputStream inputStream) {
    try {
      return new JacksonReader(jsonFactory.createParser(inputStream), failOnUnknown);
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public JsonWriter writer(Writer writer) {
    try {
      return new JacksonWriter(jsonFactory.createGenerator(writer), serializeNulls, serializeEmpty);
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }


  @Override
  public JsonWriter writer(OutputStream outputStream) {
    try {
      return new JacksonWriter(jsonFactory.createGenerator(outputStream), serializeNulls, serializeEmpty);
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public BufferedJsonWriter bufferedWriter() {
    SegmentedStringWriter buffer = new SegmentedStringWriter(jsonFactory._getBufferRecycler());
    return new JacksonWriteBuffer(writer(buffer), buffer);
  }

  @Override
  public BytesJsonWriter bufferedWriterAsBytes() {
    ByteArrayBuilder buffer = new ByteArrayBuilder(jsonFactory._getBufferRecycler());
    return new JacksonWriteAsBytes(writer(buffer), buffer);
  }
}
