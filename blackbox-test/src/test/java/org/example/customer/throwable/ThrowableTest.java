package org.example.customer.throwable;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Json.Import(MyThrowable.class)
class ThrowableTest {

  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void toJson() {
    MyThrowable e = new MyThrowable("foo");
    String asJson = jsonb.toJson(e);
    assertThat(asJson).startsWith("{\"message\":\"foo\",\"stackTrace\":[\"org.example.customer.throwable.ThrowableTest.toJson(ThrowableTest.java:17)\",");
  }


  @Test
  void toJsonViaThrowable() {
    MyThrowable e = new MyThrowable("foo");

    JsonType<Throwable> jsonThrowable = jsonb.type(Throwable.class);
    String asJson = jsonThrowable.toJson(e);
    assertThat(asJson).startsWith("{\"type\":\"class org.example.customer.throwable.MyThrowable\",\"message\":\"foo\",\"stackTrace\":[\"org.example.customer.throwable.ThrowableTest.toJsonViaThrowable");
  }

  @Test
  void toJsonWithCauseViaThrowable() {
    IllegalArgumentException cause = new IllegalArgumentException("bar");
    MyThrowable e = new MyThrowable("foo", cause);

    JsonType<Throwable> jsonThrowable = jsonb.type(Throwable.class);
    String asJson = jsonThrowable.toJson(e);
    assertThat(asJson).startsWith("{\"type\":\"class org.example.customer.throwable.MyThrowable\",\"message\":\"foo\",\"stackTrace\":[\"org.example.customer.throwable.ThrowableTest.toJsonWithCauseViaThrowable");
    assertThat(asJson).contains("],\"cause\":{\"type\":\"class java.lang.IllegalArgumentException\",\"message\":\"bar\",\"stackTrace\":[");
  }
}
