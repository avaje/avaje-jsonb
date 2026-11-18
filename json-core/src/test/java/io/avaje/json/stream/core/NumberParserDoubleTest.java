package io.avaje.json.stream.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import org.junit.jupiter.api.Test;

import io.avaje.json.stream.JsonStream;

class NumberParserDoubleTest {

  static final JsonStream stream = JsonStream.builder().build();

  static double readDouble(String number) {
    try (var reader = stream.reader("[" + number + "]")) {
      reader.beginArray();
      assertThat(reader.hasNextElement()).isTrue();
      final var v = reader.readDouble();
      assertThat(reader.hasNextElement()).isFalse();
      reader.endArray();
      return v;
    }
  }

  static long readLong(String number) {
    try (var reader = stream.reader("{\"a\":" + number + "}")) {
      reader.beginObject();
      assertThat(reader.hasNextField()).isTrue();
      assertThat(reader.nextField()).isEqualTo("a");
      final var v = reader.readLong();
      assertThat(reader.hasNextField()).isFalse();
      reader.endObject();
      return v;
    }
  }

  static int readInt(String number) {
    try (var reader = stream.reader("{\"a\":" + number + ",\"b\":1}")) {
      reader.beginObject();
      assertThat(reader.hasNextField()).isTrue();
      reader.nextField();
      final var v = reader.readInt();
      assertThat(reader.hasNextField()).isTrue();
      assertThat(reader.nextField()).isEqualTo("b");
      assertThat(reader.readInt()).isEqualTo(1);
      assertThat(reader.hasNextField()).isFalse();
      reader.endObject();
      return v;
    }
  }

  static final String[] DOUBLES = {
    "0",
    "-0",
    "1",
    "-1",
    "0.5",
    "-0.5",
    "1.5",
    "123.456",
    "-123.456",
    "0.1",
    "0.2",
    "0.3",
    "3.14159",
    "2.718281828459045",
    "1e10",
    "1E10",
    "1e+10",
    "1e-10",
    "-1e-10",
    "1.5e3",
    "1.5E-3",
    "1e22",
    "1e23",
    "1e-22",
    "1e-23",
    "1e-100",
    "1e100",
    "-1e-100",
    "1.5e-100",
    "1.5e100",
    "123456789012345",
    "1234567890123456",
    "12345678901234567",
    "9007199254740993",
    "0.000001",
    "0.0000001",
    "1e-7",
    "4.9e-324",
    "1.7976931348623157e308",
    "2.2250738585072014e-308",
    "123.456e-5",
    "123.456e5",
    "0.1e1",
    "0e5",
    "0.0",
    "100",
    "1000000",
    "1.0",
    "10.25",
    "99.99",
    "1.23456789012345678e2",
    "123456789.123456789e-3",
    "1.2345678901234567e-5",
    "12.5 ",
    "12 ",
    "1e5 ",
    "-3.25\n",
    "0.30000000000000004",
    "1e-5",
    "5e-324",
    "1e308",
    "1.0000000000000002",
    "0.9999999999999999",
    "17976931348623157e292"
  };

  @Test
  void readDouble_matchesJdk() {
    final List<String> bad = new ArrayList<>();
    for (final String number : DOUBLES) {
      final var actual = readDouble(number);
      if (Double.compare(actual, Double.parseDouble(number.trim())) != 0)
        bad.add(number + " -> " + actual);
    }
    assertThat(bad).isEmpty();
  }

  @Test
  void readDouble_randomShortValues_matchJdk() {
    final var r = new SplittableRandom(42);
    final List<String> bad = new ArrayList<>();
    for (var i = 0; i < 200_000; i++) {
      var d = Math.round(r.nextDouble() * 1_000_000_00) / 100.0;
      if (r.nextBoolean()) d = -d;
      final var s = Double.toString(d);
      if (Double.compare(readDouble(s), d) != 0) bad.add(s);
      final var sci = String.format("%.6e", d);
      if (Double.compare(readDouble(sci), Double.parseDouble(sci)) != 0) bad.add(sci);
    }
    assertThat(bad).isEmpty();
  }

  @Test
  void readInt_valid() {
    for (final String number :
        new String[] {
          "0", "1", "-1", "42", "2147483647", "-2147483648", "1000000000", "-12345", "7 "
        }) {
      assertThat(readInt(number)).as(number).isEqualTo(Integer.parseInt(number.trim()));
    }
  }

  @Test
  void readInt_invalid() {
    for (final String number :
        new String[] {"2147483648", "-2147483649", "01", "-01", "1.5", "1e2x", "-", "1a"}) {
      assertThatThrownBy(() -> readInt(number)).as(number).isInstanceOf(RuntimeException.class);
    }
  }

  @Test
  void readLong_valid() {
    for (final String number :
        new String[] {
          "0", "1", "-1", "9223372036854775807", "-9223372036854775808", "1234567890123", "5\n"
        }) {
      assertThat(readLong(number)).as(number).isEqualTo(Long.parseLong(number.trim()));
    }
  }

  @Test
  void readLong_invalid() {
    for (final String number :
        new String[] {"9223372036854775808", "-9223372036854775809", "01", "1.5", "-"}) {
      assertThatThrownBy(() -> readLong(number)).as(number).isInstanceOf(RuntimeException.class);
    }
  }

  @Test
  void readDouble_invalid() {
    for (final String number : new String[] {"01", "-01", "1.", "-", "1e", "1x", "1e+", "1.5e", ".5"}) {
      assertThatThrownBy(() -> readDouble(number)).as(number).isInstanceOf(RuntimeException.class);
    }
  }

  @Test
  void numbersAcrossStreamBufferBoundary() {
    final var sb = new StringBuilder("[");
    final List<Double> expected = new ArrayList<>();
    final var r = new SplittableRandom(7);
    for (var i = 0; i < 20_000; i++) {
      final var d = Math.round(r.nextDouble() * 1_000_000) / 1000.0;
      if (i > 0) sb.append(',');
      sb.append(d);
      expected.add(d);
    }
    sb.append(']');
    final var bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
    final List<Double> actual = new ArrayList<>();
    try (var reader = stream.reader(new ByteArrayInputStream(bytes))) {
      reader.beginArray();
      while (reader.hasNextElement()) {
        actual.add(reader.readDouble());
      }
      reader.endArray();
    }
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void intsAcrossStreamBufferBoundary() {
    final var sb = new StringBuilder("[");
    final List<Long> expected = new ArrayList<>();
    final var r = new SplittableRandom(9);
    for (var i = 0; i < 20_000; i++) {
      final var v = r.nextLong() >> r.nextInt(64);
      if (i > 0) sb.append(',');
      sb.append(v);
      expected.add(v);
    }
    sb.append(']');
    final var bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
    final List<Long> actual = new ArrayList<>();
    try (var reader = stream.reader(new ByteArrayInputStream(bytes))) {
      reader.beginArray();
      while (reader.hasNextElement()) {
        actual.add(reader.readLong());
      }
      reader.endArray();
    }
    assertThat(actual).isEqualTo(expected);
  }
}
