package io.avaje.jsonb.stream;

import java.io.Closeable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Parse JSON content.
 */
public interface JsonParser extends Closeable {

  /**
   * Set the current object names that can are "prepared" and can be used
   * to optimise the reading of <code>nextField()</code>.
   * <p>
   * Using this means that the parser can lookup the name allowing it to skip
   * reading the name as chars and creating a string instance.
   */
  void names(JsonNames names);

  /**
   * Return the current token.
   */
  byte currentToken();

  /**
   * Return the next token.
   */
  byte nextToken();

  /**
   * Read and return the field name.
   */
  String nextField();

  /**
   * Read the start of an ARRAY.
   */
  void startArray();

  /**
   * Read the end of an ARRAY.
   */
  void endArray();

  /**
   * Read the start of an OBJECT.
   */
  void startObject();

  /**
   * Read the end of an OBJECT.
   */
  void endObject();

  /**
   * Skip reading the current value.
   * <p>
   * This is typically use when we have read <code>nextField()</code>
   * and deemed that we are not interested in the value for that field.
   */
  byte skipValue();

  /**
   * Return true if the value to be read is NULL.
   */
  boolean isNullValue();

  /**
   * Read and return an int value.
   */
  int readInt();

  /**
   * Read and return a long value.
   */
  long readLong();

  /**
   * Read and return a short value.
   */
  short readShort();

  /**
   * Read and return a double value.
   */
  double readDouble();

  /**
   * Read and return a BigDecimal value.
   */
  BigDecimal readDecimal();

  /**
   * Read and return a BigInteger value.
   */
  BigInteger readBigInteger();

  /**
   * Read and return a boolean value.
   */
  boolean readBoolean();

  /**
   * Read and return a String value.
   */
  String readString();

  /**
   * Return the current location. Generally used for reporting errors.
   */
  String location();

  /**
   * Close the parser.
   */
  void close();

}
