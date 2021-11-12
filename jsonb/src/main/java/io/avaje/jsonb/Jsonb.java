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

  /**
   * Create and return a new Jsonb.Builder to configure before building the Jsonb instance.
   * <p>
   * We can register JsonAdapter's to use for specific types before building and returning
   * the Jsonb instance to use.
   * <p>
   * Note that JsonAdapter's that are generated are automatically registered via service
   * loading so there is no need to explicitly register those generated JsonAdapters.
   */
  static Builder newBuilder() {
    Iterator<Bootstrap> bootstrapService = ServiceLoader.load(Bootstrap.class).iterator();
    if (bootstrapService.hasNext()) {
      return bootstrapService.next().newBuilder();
    }
    return DefaultBootstrap.newBuilder();
  }

  /**
   * Return the JsonType used to read and write json for the given class.
   */
  <T> JsonType<T> type(Class<T> cls);

  /**
   * Return the JsonType used to read and write json for the given type.
   */
  <T> JsonType<T> type(Type type);

  /**
   * Return the JsonAdapter used to read and write json for the given class.
   */
  <T> JsonAdapter<T> adapter(Class<T> cls);

  /**
   * Return the JsonAdapter used to read and write json for the given type.
   */
  <T> JsonAdapter<T> adapter(Type type);

  /**
   * Return the JsonAdapter used to read and write json for the given type and qualifier annotations.
   */
  <T> JsonAdapter<T> adapter(Type type, Set<? extends Annotation> annotations);

  /**
   * Return the JsonReader used to read the given json content.
   */
  JsonReader reader(String json) throws IOException;

  /**
   * Return the JsonReader used to read the json content from the given reader.
   */
  JsonReader reader(Reader reader) throws IOException;

  /**
   * Return the JsonReader used to read the json content from the given inputStream.
   */
  JsonReader reader(InputStream inputStream) throws IOException;

  /**
   * Return the JsonWriter used to write json to the given writer.
   */
  JsonWriter writer(Writer writer) throws IOException;

  /**
   * Return the JsonWriter used to write json to the given outputStream.
   */
  JsonWriter writer(OutputStream outputStream) throws IOException;

  /**
   * Build the Jsonb instance adding JsonAdapter, Factory or AdapterBuilder.
   */
  interface Builder {

    /**
     * Set failOnUnknown to true such that an exception is thrown when unknown
     * properties are read in the json content.
     */
    Builder failOnUnknown(boolean failOnUnknown);

    /**
     * Add a JsonAdapter to use for the given type.
     */
    <T> Jsonb.Builder add(Type type, JsonAdapter<T> jsonAdapter);

    /**
     * Add a AdapterBuilder which provides a JsonAdapter to use for the given type.
     */
    Jsonb.Builder add(Type type, AdapterBuilder builder);

    /**
     * Add a JsonAdapter.Factory which provides JsonAdapters to use.
     */
    Jsonb.Builder add(JsonAdapter.Factory factory);

    /**
     * Build and return the Jsonb instance with all the given adapters and factories registered.
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
