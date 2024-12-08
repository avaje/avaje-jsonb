package io.avaje.json.stream.core;

import io.avaje.json.stream.JsonOutput;

import java.io.Closeable;
import java.io.Flushable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Generate JSON content.
 */
interface JsonGenerator extends Closeable, Flushable {

  /**
   * Enable or disable pretty json formatting.
   */
  void pretty(boolean pretty);

  /**
   * Write start of an ARRAY.
   */
  void startArray();

  /**
   * Write end of an ARRAY.
   */
  void endArray();

  /**
   * Write start of an OBJECT using already encoded and escaped names that can be
   * used via {@link #writeName(int)}.
   */
  void startObject(JsonNames nextNames);

  /**
   * Write start of an OBJECT that will not use already encoded and escaped names.
   */
  void startObject();

  /**
   * Write end of an OBJECT.
   */
  void endObject();

  /**
   * Set the already encoded and escaped names that can be used via {@link #writeName(int)}.
   * <p>
   * These names should be used for all json generation for this generator and set once
   * rather than set per object via {@link #startObject(JsonNames)}.
   */
  void allNames(JsonNames names);

  /**
   * Set the next property name to write by position. This uses the already encoded
   * name values of JsonNames.
   */
  void writeName(int namePos);

  /**
   * Write a field name.
   */
  void writeName(String name);

  /**
   * Write null value.
   */
  void writeNull();

  /**
   * Write a boolean value.
   */
  void write(boolean value);

  /**
   * Write an int value.
   */
  void write(int value);

  /**
   * Write a long value.
   */
  void write(long value);

  /**
   * Write a double value.
   */
  void write(double value);

  /**
   * Write a BigInteger value.
   */
  void write(BigInteger value);

  /**
   * Write a BigDecimal value.
   */
  void write(BigDecimal value);

  /**
   * Write a String value.
   */
  void write(String value);

  /**
   * Write a binary value as base64.
   */
  void write(byte[] value);

  /**
   * Write raw content that is assumed to be valid json.
   */
  void writeRaw(String value);

  /**
   * Write a new line. This is typically used when support line delimited x-json-stream content.
   */
  void writeNewLine();

  /**
   * Flush the content.
   */
  void flush();

  /**
   * Close the generator.
   */
  void close();

  /**
   * Return the underlying content as bytes.
   */
  byte[] toByteArray();

  /**
   * Mark that json generation was not completed due to an error.
   */
  void markIncomplete();

  /**
   * Bind input stream for processing. Stream will be processed in byte[] chunks. If stream is null,
   * reference to stream will be released.
   */
  JsonGenerator prepare(JsonOutput targetStream);
}
