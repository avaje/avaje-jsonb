package io.avaje.json.stream.core;

import java.io.Closeable;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Parse JSON content.
 */
interface JsonParser extends Closeable {

  /**
   * Return the current token.
   */
  byte currentToken();

  /**
   * Return the next token.
   */
  byte nextToken();

  /**
   * Return true if there is a next element of an ARRAY.
   */
  boolean hasNextElement();

  /**
   * Return true if there is a next element of an ARRAY or stream.
   * Support x-json-stream new line delimited json.
   */
  boolean hasNextStreamElement();

  /**
   * Read and return the field name.
   */
  String nextField();

  /**
   * Start a stream which could be an ARRAY or x-json-stream new line delimited json.
   */
  void startStream();

  /**
   * End a stream which could be an ARRAY or x-json-stream new line delimited json.
   */
  void endStream();

  /**
   * Read the start of an ARRAY.
   */
  void startArray();

  /**
   * Read the end of an ARRAY.
   */
  void endArray();

  /**
   * Set the current object names that can are "prepared" and can be used
   * to optimise the reading of <code>nextField()</code>.
   * <p>
   * Using this means that the parser can lookup the name allowing it to skip
   * reading the name as chars and creating a string instance.
   */
  void startObject(JsonNames names);

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
  void skipValue();

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
   * Read and return a binary value from base64.
   */
  byte[] readBinary();

  /**
   * Read and return raw json content.
   */
  String readRaw();

  /**
   * Return the current location. Generally used for reporting errors.
   */
  String location();

  /**
   * Close the parser.
   */
  void close();

  /**
   * Bind input stream for processing. Stream will be processed in byte[] chunks. If stream is null,
   * reference to stream will be released.
   */
  JParser process(InputStream newStream);

  /**
   * Bind byte[] buffer for processing. If this method is used in combination with
   * process(InputStream) this buffer will be used for processing chunks of stream. If null is sent
   * for byte[] buffer, new length for valid input will be set for existing buffer.
   *
   * @param newBuffer new buffer to use for processing
   * @param newLength length of buffer which can be used
   */
  JParser process(byte[] newBuffer, int newLength);
}
