package io.avaje.jsonb.stream;

import java.io.InputStream;
import java.io.OutputStream;

final class Recycle {
  private Recycle() {
  }

  private static final int GENERATOR_BUFFER_SIZE = Integer.getInteger("jsonb.generatorBufferSize", 4096);
  private static final int PARSER_BUFFER_SIZE = Integer.getInteger("jsonb.parserBufferSize", 4096);
  private static final int PARSER_CHAR_BUFFER_SIZE = Integer.getInteger("jsonb.parserCharBufferSize", 4096);

  private static boolean jvmRecycle;
  private static ThreadLocal<JParser> read;
  private static ThreadLocal<JGenerator> managed;

  static {

    if (Boolean.getBoolean("jsonb.useJVMBufferRecycling")) {
      jvmRecycle = true;
    } else {
      managed = ThreadLocal.withInitial(Recycle::createGenerator);
      read = ThreadLocal.withInitial(Recycle::createParser);
    }
  }

  /**
   * Return a recycled generator with the given target OutputStream.
   */
  static JsonGenerator generator(JsonOutput target) {
    return (jvmRecycle ? createGenerator() : managed.get()).prepare(target);
  }

  /**
   * Return a recycled generator with expected "to String" result.
   */
  static JsonGenerator generator() {
    return (jvmRecycle ? createGenerator() : managed.get()).prepare(null);
  }

  static JsonParser parser(byte[] bytes) {
    return (jvmRecycle ? createParser() : read.get()).process(bytes, bytes.length);
  }

  static JsonParser parser(InputStream in) {
    return (jvmRecycle ? createParser() : read.get()).process(in);
  }

  static JGenerator createGenerator() {
    return new JGenerator(GENERATOR_BUFFER_SIZE);
  }

  static JParser createParser() {
    final char[] ch = new char[PARSER_CHAR_BUFFER_SIZE];
    final byte[] by = new byte[PARSER_BUFFER_SIZE];
    return new JParser(
      ch,
      by,
      0,
      JParser.ErrorInfo.MINIMAL,
      JParser.DoublePrecision.DEFAULT,
      JParser.UnknownNumberParsing.BIGDECIMAL,
      100,
      50_000);
  }
}
