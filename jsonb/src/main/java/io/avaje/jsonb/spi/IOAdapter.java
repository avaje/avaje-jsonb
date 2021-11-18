package io.avaje.jsonb.spi;

import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;

import java.io.*;

public interface IOAdapter {

  JsonReader reader(String json) throws IOException;

  JsonReader reader(Reader reader) throws IOException;

  JsonReader reader(InputStream inputStream) throws IOException;

  JsonWriter writer(Writer writer) throws IOException;

  JsonWriter writer(OutputStream outputStream) throws IOException;

  BufferedJsonWriter bufferedWriter() throws IOException;

  MetaNames properties(String... names);
}
