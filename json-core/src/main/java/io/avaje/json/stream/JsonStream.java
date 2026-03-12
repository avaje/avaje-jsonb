package io.avaje.json.stream;

import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.PropertyNames;
import io.avaje.json.stream.core.JsonStreamBuilder;

import java.io.*;

/**
 * Provides the underlying JsonReader and JsonWriter to use.
 */
public interface JsonStream {

  /**
   * Return the JsonReader given json string content.
   */
  JsonReader reader(String json);

  /**
   * Return the JsonReader given json content as bytes.
   */
  JsonReader reader(byte[] json);

  /**
   * Return the JsonReader given json string content.
   */
  JsonReader reader(Reader reader);

  /**
   * Return the JsonReader given json string content.
   */
  JsonReader reader(InputStream inputStream);

  /**
   * Return the JsonWriter given writer to use.
   */
  JsonWriter writer(Writer writer);

  /**
   * Return the JsonWriter given the outputStream.
   */
  JsonWriter writer(OutputStream outputStream);

  /**
   * Return the JsonWriter given the output.
   */
  JsonWriter writer(JsonOutput output);

  /**
   * Return a JsonWriter for use for writing to json string.
   */
  BufferedJsonWriter bufferedWriter();

  /**
   * Return a JsonWriter to use for writing json to byte array.
   */
  BytesJsonWriter bufferedWriterAsBytes();

  /**
   * Return PropertyNames given the names of properties.
   * <p>
   * The PropertyNames can prepare the names for writing such as
   * escaping quotes and encoding to bytes so that the names can
   * be written more efficiently.
   *
   * @see JsonWriter#allNames(PropertyNames)
   * @see JsonWriter#name(int)
   */
  PropertyNames properties(String... names);

  /**
   * Create and return a builder for the default JsonStreamAdapter implementation.
   */
  static Builder builder() {
    return new JsonStreamBuilder();
  }

  /** Used to build JsonStream with custom settings. */
  interface Builder {

    /** Set to true to serialize nulls. Defaults to false. */
    Builder serializeNulls(boolean serializeNulls);

    /** Set to true to serialize empty collections. Defaults to false. */
    Builder serializeEmpty(boolean serializeEmpty);

    /** Set to true to fail on unknown properties. Defaults to false. */
    Builder failOnUnknown(boolean failOnUnknown);

    /** Set to true to fail on NULL for primitive types. Defaults to false. */
    Builder failOnNullPrimitives(boolean failOnNullPrimitives);

    /** Determines how byte buffers are recycled */
    Builder bufferRecycling(BufferRecycleStrategy strategy);

    /** Build and return the JsonStream. */
    JsonStream build();
  }
}
