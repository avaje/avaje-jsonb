package io.avaje.jsonb.diesel;

import java.io.Closeable;
import java.io.Flushable;
import java.math.BigDecimal;
import java.math.BigInteger;

public interface JsonGenerator extends Closeable, Flushable {

  void startArray();

  void endArray();

  void startObject();

  void endObject();

  void writeName(String name);

  void writeName(byte[] escapedName);

  void writeNull();

  void write(boolean value);

  void write(int value);

  void write(long value);

  void write(double value);

  void write(BigInteger value);

  void write(BigDecimal value);

  void write(String value);

  void writeBinary(byte[] value);

  void writeNewLine();

  void flush();

  byte[] toByteArray();

}
