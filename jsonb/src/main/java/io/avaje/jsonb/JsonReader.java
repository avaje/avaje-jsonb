package io.avaje.jsonb;

import java.io.IOException;

public interface JsonReader {

  void beginArray() throws IOException;

  void endArray();

  boolean hasNextElement() throws IOException;

  void beginObject() throws IOException;

  void endObject() throws IOException;

  boolean hasNextField() throws IOException;

  String nextField() throws IOException;

  boolean nextBoolean() throws IOException;

  int nextInt() throws IOException;

  long nextLong() throws IOException;

  double nextDouble() throws IOException;

  String nextString() throws IOException;

  boolean peekIsNull();

  <T> T nextNull();

  String path();

}
