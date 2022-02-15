package io.avaje.jsonb.diesel;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public interface JsonParser {

  String location();

  void names(JsonNames nextNames);

  byte currentToken();

  byte nextToken() throws IOException;

  String nextField() throws IOException;

  boolean currentIsNull() throws ParsingException;

  byte skip() throws IOException;

  void startArray() throws IOException;

  void endArray() throws IOException;

  void startObject() throws IOException;

  void endObject() throws IOException;

  int readInt() throws IOException;

  long readLong() throws IOException;

  short readShort() throws IOException;

  double readDouble() throws IOException;

  BigDecimal readDecimal() throws IOException;

  BigInteger readBigInteger() throws IOException;

  boolean readBoolean() throws IOException;

  String readString() throws IOException;

  void close();

}
