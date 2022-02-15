package io.avaje.jsonb.stream;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public interface JsonParser extends Closeable {

  void names(JsonNames nextNames);

  byte currentToken();

  byte nextToken();

  String nextField();

  void startArray();

  void endArray();

  void startObject();

  void endObject();

  byte skipValue();

  boolean isNullValue();

  int readInt();

  long readLong();

  short readShort();

  double readDouble();

  BigDecimal readDecimal();

  BigInteger readBigInteger();

  boolean readBoolean();

  String readString();

  /**
   * Return the current location. Generally used for reporting errors.
   */
  String location();

  void close();

}
