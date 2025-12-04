package io.avaje.json.stream.core;

final class ThreadFunctions {
  private ThreadFunctions() {}

  static long getId() {
    return Thread.currentThread().threadId();
  }

  static boolean isVirtual() {
    return Thread.currentThread().isVirtual();
  }
}
