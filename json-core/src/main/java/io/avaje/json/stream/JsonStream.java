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
   * Return this stream configured to verify property names when reading.
   *
   * <p>Implementations that do not support strict property-name matching must throw when enabled.
   */
  default JsonStream withStrictPropertyNames(boolean strictPropertyNames) {
    if (strictPropertyNames) {
      throw new UnsupportedOperationException("Strict property name matching is not supported");
    }
    return this;
  }

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

    /**
     * Set to true to confirm property names when reading. Defaults to false.
     *
     * <p>By default property names are matched by their hash (FNV-1a 32 bit) only, so a key that is
     * not a property name but shares the hash of a property name is read as that property. Set this
     * to true when json content is checked or filtered by key name before it is read (for example
     * by a gateway), such that a key is only read as a property when it equals the property name.
     * Enabling this has a 15-25% cost to read performance.
     */
    Builder strictPropertyNames(boolean strictPropertyNames);

    /**
     * Set the default parser limits used by readers created from this stream.
     * System properties and environment variables provide the defaults when this is not set.
     */
    default Builder parserLimits(ParserLimits parserLimits) {
      throw new UnsupportedOperationException("Parser limits are not supported");
    }

    /** Determines how byte buffers are recycled */
    Builder bufferRecycling(BufferRecycleStrategy strategy);

    /** Build and return the JsonStream. */
    JsonStream build();
  }
}
