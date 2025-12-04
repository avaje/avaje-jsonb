package io.avaje.json.stream.core;

final class ThreadFunctions {
  private ThreadFunctions() {}

  public static long getId() {
    return Thread.currentThread().threadId();
  }

  public static boolean isVirtual() {
    return Thread.currentThread().isVirtual();
  }
}
