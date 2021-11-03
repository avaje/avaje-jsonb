package io.avaje.jsonb;

import io.avaje.jsonb.core.DefaultBootstrap;
import io.avaje.jsonb.spi.Bootstrap;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

public interface Jsonb {

  static Builder newBuilder() {
    Iterator<Bootstrap> bootstrapService = ServiceLoader.load(Bootstrap.class).iterator();
    if (bootstrapService.hasNext()) {
      return bootstrapService.next().newBuilder();
    }
    return DefaultBootstrap.newBuilder();
  }

  JsonReader reader(String json) throws IOException;

  JsonReader reader(Reader reader) throws IOException;

  JsonReader reader(InputStream inputStream) throws IOException;

  JsonWriter writer(Writer writer) throws IOException;

  JsonWriter writer(OutputStream outputStream) throws IOException;

  <T> JsonType<T> type(Class<T> cls);

  <T> JsonType<T> type(Type type);

  <T> JsonAdapter<T> adapter(Class<T> cls);

  <T> JsonAdapter<T> adapter(Type type);

  <T> JsonAdapter<T> adapter(Type type, Set<? extends Annotation> annotations);

  /**
   * Build the Jsonb instance adding JsonAdapter, Factory or AdapterBuilder.
   */
  interface Builder {

    <T> Jsonb.Builder add(Type type, JsonAdapter<T> jsonAdapter);

    Jsonb.Builder add(Type type, AdapterBuilder builder);

    <T> Jsonb.Builder add(Type type, Class<? extends Annotation> annotation, JsonAdapter<T> jsonAdapter);

    Jsonb.Builder add(JsonAdapter.Factory factory);

    /**
     * Build the Jsonb instance with all the given adapters and factories registered.
     */
    Jsonb build();
  }

  /**
   * Function to build a JsonAdapter that needs Jsonb.
   */
  @FunctionalInterface
  interface AdapterBuilder {

    /**
     * Create a JsonAdapter given the Jsonb instance.
     */
    JsonAdapter<?> build(Jsonb jsonb);
  }

  /**
   * Components register JsonAdapters Jsonb.Builder
   */
  @FunctionalInterface
  interface Component {

    /**
     * Register JsonAdapters with the Builder.
     */
    void register(Builder builder);
  }
}
