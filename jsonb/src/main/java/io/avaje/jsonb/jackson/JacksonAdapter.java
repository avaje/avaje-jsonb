package io.avaje.jsonb.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import io.avaje.jsonb.JsonIoException;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.spi.BufferedJsonWriter;
import io.avaje.jsonb.spi.IOAdapter;
import io.avaje.jsonb.spi.PropertyNames;

import java.io.*;

public class JacksonAdapter implements IOAdapter {

  private final JsonFactory jsonFactory;
  private final boolean failOnUnknown;

  public JacksonAdapter(boolean failOnUnknown) {
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
      return new JacksonWriter(jsonFactory.createGenerator(writer));
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }


  @Override
  public JsonWriter writer(OutputStream outputStream) {
    try {
      return new JacksonWriter(jsonFactory.createGenerator(outputStream));
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public BufferedJsonWriter bufferedWriter() {
    SegmentedStringWriter sw = new SegmentedStringWriter(jsonFactory._getBufferRecycler());
    JsonWriter delegate = writer(sw);
    return new JacksonWriteBuffer(delegate, sw);
  }
}
