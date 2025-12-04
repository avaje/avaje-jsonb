package io.avaje.json.stream.core;

final class ThreadFunctions {
  private ThreadFunctions() {}

  public static long getId() {
    return Thread.currentThread().getId();
  }

  public static boolean isVirtual() {
    return false;
  }
}
