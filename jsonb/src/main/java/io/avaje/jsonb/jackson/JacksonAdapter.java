package io.avaje.jsonb.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.spi.IOAdapter;

import java.io.*;

public class JacksonAdapter implements IOAdapter {

  private final JsonFactory jsonFactory;

  public JacksonAdapter() {
    jsonFactory = new JsonFactory();
  }

  @Override
  public JsonReader reader(String json) throws IOException {
    return new JacksonReader(jsonFactory.createParser(json));
  }

  @Override
  public JsonReader reader(Reader reader) throws IOException {
    return new JacksonReader(jsonFactory.createParser(reader));
  }

  @Override
  public JsonReader reader(InputStream inputStream) throws IOException {
    return new JacksonReader(jsonFactory.createParser(inputStream));
  }

  @Override
  public JsonWriter writer(Writer writer) throws IOException {
    return new JacksonWriter(jsonFactory.createGenerator(writer));
  }


  @Override
  public JsonWriter writer(OutputStream outputStream) throws IOException {
    return new JacksonWriter(jsonFactory.createGenerator(outputStream));
  }
}
