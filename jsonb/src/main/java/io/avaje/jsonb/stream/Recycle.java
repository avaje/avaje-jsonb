package io.avaje.jsonb.stream;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executors;

final class Recycle {
  private Recycle() {}

  private static boolean virtualThreads;
  private static ThreadLocal<JGenerator> managed;
  private static ThreadLocal<JParser> read;

  static {
    try {
      Executors.class.getMethod("newVirtualThreadPerTaskExecutor").invoke(Executors.class);
      virtualThreads = true;
    } catch (final Exception e) {
      virtualThreads = false;
    }
    if (!virtualThreads) {
      managed = ThreadLocal.withInitial(Recycle::getGenerator);
      read = ThreadLocal.withInitial(Recycle::getParser);
    }
  }

  /** Return a recycled generator with the given target OutputStream. */
  static JsonGenerator generator(OutputStream target) {
    return (virtualThreads ? getGenerator() : managed.get()).prepare(target);
  }

  /** Return a recycled generator with expected "to String" result. */
  static JsonGenerator generator() {
    return (virtualThreads ? getGenerator() : managed.get()).prepare(null);
  }

  static JsonParser parser(byte[] bytes) {
    return (virtualThreads ? getParser() : read.get()).process(bytes, bytes.length);
  }

  static JsonParser parser(InputStream in) {
    return (virtualThreads ? getParser() : read.get()).process(in);
  }

  static JGenerator getGenerator() {
    return new JGenerator(4096);
  }

  static JParser getParser() {
    final char[] ch = new char[4096];
    final byte[] by = new byte[4096];
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
