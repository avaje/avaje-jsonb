package io.avaje.jsonb.stream;

import java.io.InputStream;
import java.io.OutputStream;

final class Recycle {
  private Recycle() {}

  private static final int generatorBufferSize = Integer.getInteger("jsonb.generatorBufferSize", 4096);
  private static final int parserBufferSize = Integer.getInteger("jsonb.parserBufferSize", 4096);
  private static final int parserCharBufferSize = Integer.getInteger("jsonb.parserCharBufferSize", 4096);

  private static boolean jvmRecycle;
  private static ThreadLocal<JParser> read;
  private static ThreadLocal<JGenerator> managed;

  static {
    if (Float.parseFloat(System.getProperty("java.specification.version")) >= 19
        && !Boolean.getBoolean("jsonb.useTLBuffers")) {
      jvmRecycle = true;
    } else {
      managed = ThreadLocal.withInitial(Recycle::getGenerator);
      read = ThreadLocal.withInitial(Recycle::getParser);
    }
  }

  /** Return a recycled generator with the given target OutputStream. */
  static JsonGenerator generator(OutputStream target) {
    return (jvmRecycle ? getGenerator() : managed.get()).prepare(target);
  }

  /** Return a recycled generator with expected "to String" result. */
  static JsonGenerator generator() {
    return (jvmRecycle ? getGenerator() : managed.get()).prepare(null);
  }

  static JsonParser parser(byte[] bytes) {
    return (jvmRecycle ? getParser() : read.get()).process(bytes, bytes.length);
  }

  static JsonParser parser(InputStream in) {
    return (jvmRecycle ? getParser() : read.get()).process(in);
  }

  static JGenerator getGenerator() {
    return new JGenerator(generatorBufferSize);
  }

  static JParser getParser() {
    final char[] ch = new char[parserCharBufferSize];
    final byte[] by = new byte[parserBufferSize];
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
