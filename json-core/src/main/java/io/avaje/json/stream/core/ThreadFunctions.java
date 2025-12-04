package io.avaje.json.stream.core;

final class ThreadFunctions {
  private ThreadFunctions() {}

  static long getId() {
    return Thread.currentThread().getId();
  }

  static boolean isVirtual() {
    return false;
  }
}
