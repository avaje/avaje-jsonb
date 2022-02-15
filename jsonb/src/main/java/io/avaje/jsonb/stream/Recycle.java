package io.avaje.jsonb.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

final class Recycle {

  static ThreadLocal<JGenerator> managed = ThreadLocal.withInitial(() -> new JGenerator(4096));

  static ThreadLocal<JParser> read = ThreadLocal.withInitial(() -> {
    char[] ch = new char[4096];
    byte[] by = new byte[4096];
    return new JParser(ch, by, 0, JParser.ErrorInfo.MINIMAL, JParser.DoublePrecision.DEFAULT, JParser.UnknownNumberParsing.BIGDECIMAL, 100, 50_000);
  });

  /**
   * Return a recycled generator with the given target OutputStream.
   */
  static JsonGenerator generator(OutputStream target) {
    return managed.get().prepare(target);
  }

  /**
   * Return a recycled generator with expected "to String" result.
   */
  static JsonGenerator generator() {
    return managed.get().prepare(null);
  }

  static JsonParser parser(byte[] bytes) {
    return read.get().process(bytes, bytes.length);
  }

  static JsonParser parser(InputStream in) throws IOException {
    return read.get().process(in);
  }
}
