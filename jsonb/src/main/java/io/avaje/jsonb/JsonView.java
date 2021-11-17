package io.avaje.jsonb;

import java.io.OutputStream;
import java.io.Writer;

public interface JsonView<T> {

  String toJson(T value);

  void toJson(JsonWriter writer, T value);

  void toJson(Writer writer, T value);

  void toJson(OutputStream outputStream, T value);
}
