package io.avaje.jsonb.spi;

import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;

import java.io.*;

public interface IOAdapter {

  JsonReader reader(String json);

  JsonReader reader(Reader reader);

  JsonReader reader(InputStream inputStream);

  JsonWriter writer(Writer writer);

  JsonWriter writer(OutputStream outputStream);

  BufferedJsonWriter bufferedWriter();

  PropertyNames properties(String... names);
}
