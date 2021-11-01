package io.avaje.mason.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import io.avaje.mason.JsonReader;
import io.avaje.mason.JsonWriter;
import io.avaje.mason.spi.IOAdapter;

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
