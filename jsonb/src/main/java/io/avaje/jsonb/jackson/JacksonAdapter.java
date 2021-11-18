package io.avaje.jsonb.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.spi.BufferedJsonWriter;
import io.avaje.jsonb.spi.IOAdapter;
import io.avaje.jsonb.spi.MetaNames;

import java.io.*;

public class JacksonAdapter implements IOAdapter {

  private final JsonFactory jsonFactory;
  private final boolean failOnUnknown;

  public JacksonAdapter(boolean failOnUnknown) {
    this.failOnUnknown = failOnUnknown;
    this.jsonFactory = new JsonFactory();
  }

  @Override
  public MetaNames properties(String... names) {
    return new JacksonNames(names);
  }

  @Override
  public JsonReader reader(String json) throws IOException {
    return new JacksonReader(jsonFactory.createParser(json), failOnUnknown);
  }

  @Override
  public JsonReader reader(Reader reader) throws IOException {
    return new JacksonReader(jsonFactory.createParser(reader), failOnUnknown);
  }

  @Override
  public JsonReader reader(InputStream inputStream) throws IOException {
    return new JacksonReader(jsonFactory.createParser(inputStream), failOnUnknown);
  }

  @Override
  public JsonWriter writer(Writer writer) throws IOException {
    return new JacksonWriter(jsonFactory.createGenerator(writer));
  }


  @Override
  public JsonWriter writer(OutputStream outputStream) throws IOException {
    return new JacksonWriter(jsonFactory.createGenerator(outputStream));
  }

  @Override
  public BufferedJsonWriter bufferedWriter() throws IOException {
    SegmentedStringWriter sw = new SegmentedStringWriter(jsonFactory._getBufferRecycler());
    JsonWriter delegate = writer(sw);
    return new JacksonWriteBuffer(delegate, sw);
  }
}
