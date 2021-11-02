package io.avaje.mason.spi;

import io.avaje.mason.JsonReader;
import io.avaje.mason.JsonWriter;

import java.io.*;

public interface IOAdapter {

  JsonReader reader(String json) throws IOException;

  JsonReader reader(Reader reader) throws IOException;

  JsonReader reader(InputStream inputStream) throws IOException;

  JsonWriter writer(Writer writer) throws IOException;

  JsonWriter writer(OutputStream outputStream) throws IOException;


}
