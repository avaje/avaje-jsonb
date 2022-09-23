package org.example.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.locks.ReentrantLock;

public class LockTest {

  private static Object lock = new Object();
  private static ReentrantLock reentrantLock = new ReentrantLock();

  private static long cnt = 0;

  @Benchmark
  @Measurement(iterations = 2)
  @Threads(10)
  @Fork(0)
  @Warmup(iterations = 5, time = 10)
  public void testWithoutLock(){
    doSomething();
  }

  @Benchmark
  @Measurement(iterations = 2)
  @Threads(10)
  @Fork(0)
  @Warmup(iterations = 5, time = 10)
  public long testReentrantLock(){
    reentrantLock.lock();
    try {
      return doSomething();
    } finally {
      reentrantLock.unlock();
    }
  }

  @Benchmark
  @Measurement(iterations = 2)
  @Threads(10)
  @Fork(0)
  @Warmup(iterations = 5, time = 10)
  public long testSynchronized(){
    synchronized (lock) {
      return doSomething();
    }
  }

  private long doSomething() {
    cnt += 1;
    if (cnt == (Long.MAX_VALUE)) {
      cnt = 0;
    }
    return cnt;
  }

  public static void main(String[] args) {
    Options options = new OptionsBuilder().include(LockTest.class.getSimpleName()).build();
    try {
      new Runner(options).run();
    } catch (Exception e) {

    } finally {
    }
  }
}
