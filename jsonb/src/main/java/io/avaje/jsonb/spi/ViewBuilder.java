package io.avaje.jsonb.spi;

import io.avaje.jsonb.JsonAdapter;

import java.lang.invoke.MethodHandle;

public interface ViewBuilder {

  void beginObject(String name, MethodHandle methodHandle);

  void add(String name, JsonAdapter<?> adapter, MethodHandle methodHandle);

  void addArray(String name, JsonAdapter<?> adapter, MethodHandle methodHandle);

  void endObject();

  MethodHandle method(Class<?> cls, String methodName, Class<?> returnType);
}
