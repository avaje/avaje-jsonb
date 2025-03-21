package io.avaje.json.stream.core;

import io.avaje.json.JsonReader;
import io.avaje.json.stream.JsonStream;
import io.avaje.json.stream.core.Recyclers.ThreadLocalPool;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JsonReadAdapterTest {

  String jsonStringInput = "{\"name\":\"roberto\", \"age\": 42 , \"notes\" :\"fooFooFoo\"}";

  @Test
  void via_jreader() {
    char[] ch = new char[1000];
    byte[] by = new byte[1000];
    JParser jr = new JParser(ch, by, 0, JParser.ErrorInfo.MINIMAL, JParser.DoublePrecision.DEFAULT, JParser.UnknownNumberParsing.BIGDECIMAL, 100, 50_000);

    byte[] bytes = jsonStringInput.getBytes(StandardCharsets.UTF_8);
    jr.process(bytes, bytes.length);

    JsonReadAdapter reader =
      new JsonReadAdapter(jr, ThreadLocalPool.shared(), true, true);
    readExampleWithAsserts(reader);
    reader.close();
  }

  @Test
  void via_adapter_usingReader() {
    JsonStream adapter = JsonStream.builder().build();
    try (JsonReader reader = adapter.reader(new StringReader(jsonStringInput))) {
      readExampleWithAsserts(reader);
    }
    try (JsonReader reader = adapter.reader(new StringReader(jsonStringInput))) {
      readExampleWithAsserts(reader);
    }
  }

  @Test
  void via_adapter_usingString() {
    JsonStream adapter = JsonStream.builder().build();
    try (JsonReader reader = adapter.reader(jsonStringInput)) {
      readExampleWithAsserts(reader);
    }
    try (JsonReader reader = adapter.reader(jsonStringInput)) {
      readExampleWithAsserts(reader);
    }
  }

  private void readExampleWithAsserts(JsonReader reader) {
    reader.beginObject();
    assertTrue(reader.hasNextField());
    assertEquals("name", reader.nextField());
    assertEquals("roberto", reader.readString());
    assertTrue(reader.hasNextField());
    assertEquals("age", reader.nextField());
    assertEquals(42, reader.readInt());
    assertTrue(reader.hasNextField());
    assertEquals("notes", reader.nextField());
    assertEquals("fooFooFoo", reader.readString());
  }

  @Test
  void bigInt() {
    String input = "{\"name\":\"roberto\", \"val0\": 123, \"val1\": \"1234567890123456789\", \"val2\": 1234567890123456789 , \"notes\" :\"end\"}";

    JsonStream adapter = JsonStream.builder().failOnUnknown(true).build();
    try (JsonReader reader = adapter.reader(input)) {
      reader.beginObject();
      assertTrue(reader.hasNextField());
      assertEquals("name", reader.nextField());
      assertEquals("roberto", reader.readString());
      assertTrue(reader.hasNextField());
      assertEquals("val0", reader.nextField());
      assertEquals(BigInteger.valueOf(123), reader.readBigInteger());
      assertTrue(reader.hasNextField());
      assertEquals("val1", reader.nextField());
      assertEquals(BigInteger.valueOf(1234567890123456789L), reader.readBigInteger());
      assertTrue(reader.hasNextField());
      assertEquals("val2", reader.nextField());
      assertEquals(BigInteger.valueOf(1234567890123456789L), reader.readBigInteger());

      assertTrue(reader.hasNextField());
      assertEquals("notes", reader.nextField());
      assertEquals("end", reader.readString());
    }
  }
}
