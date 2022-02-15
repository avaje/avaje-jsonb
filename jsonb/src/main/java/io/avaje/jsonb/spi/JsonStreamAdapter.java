package io.avaje.jsonb.spi;

import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;

import java.io.*;

/**
 * Provides the underlying JsonReader and JsonWriter to use.
 */
public interface JsonStreamAdapter {

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
   * @see JsonWriter#names(PropertyNames)
   * @see JsonWriter#name(int)
   */
  PropertyNames properties(String... names);
}
