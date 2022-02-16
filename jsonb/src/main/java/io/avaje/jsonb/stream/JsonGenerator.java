package io.avaje.jsonb.stream;

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
   * Write start of an OBJECT.
   */
  void startObject();

  /**
   * Write end of an OBJECT.
   */
  void endObject();

  /**
   * Set the already encoded and escaped names that can be used via {@link #writeName(int)}.
   */
  void names(JsonNames nextNames);

  /**
   * Write a field name that is already encoded and escaped.
   * <p>
   * The namePos is the name position in the JsonNames that has been set.
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
   * Write a binary value.
   */
  void writeBinary(byte[] value);

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

}
