package io.avaje.jsonb.diesel;

import io.avaje.jsonb.diesel.read.JReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class Recycle {

  static ThreadLocal<JGenerator> managed = ThreadLocal.withInitial(() -> new JGenerator(4096));

  static ThreadLocal<JReader> read = ThreadLocal.withInitial(() -> {
    char[] ch = new char[1000];
    byte[] by = new byte[1000];
    return new JReader(ch, by, 0, JReader.ErrorInfo.MINIMAL, JReader.DoublePrecision.DEFAULT, JReader.UnknownNumberParsing.BIGDECIMAL, 100, 50_000);
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

  static JReader reader(byte[] bytes) {
    return read.get().process(bytes, bytes.length);
  }

  static JReader reader(InputStream in) throws IOException {
    return read.get().process(in);
  }
}
