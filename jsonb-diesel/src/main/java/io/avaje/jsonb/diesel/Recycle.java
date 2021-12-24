package io.avaje.jsonb.diesel;

import java.io.OutputStream;

class Recycle {

  static ThreadLocal<JGenerator> managed = ThreadLocal.withInitial(() -> new JGenerator(4096));

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
}
