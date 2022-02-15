package io.avaje.jsonb.diesel;

import io.avaje.jsonb.JsonReader;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonReadAdapterTest {

  @Test
  void via_jreader() {

    char[] ch = new char[1000];
    byte[] by = new byte[1000];
    JsonParser jr = new JsonParser(ch, by, 0, JsonParser.ErrorInfo.MINIMAL, JsonParser.DoublePrecision.DEFAULT, JsonParser.UnknownNumberParsing.BIGDECIMAL, 100, 50_000);

    String input = "{\"name\":\"roberto\", \"age\": 42 , \"notes\" :\"fooFooFoo\"}";
    byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
    jr.process(bytes, bytes.length);

    JsonReadAdapter reader = new JsonReadAdapter(jr, true);
    readExampleWithAsserts(reader);
    reader.close();
  }

  @Test
  void via_adapter() {
    String input = "{\"name\":\"roberto\", \"age\": 42 , \"notes\" :\"fooFooFoo\"}";

    DieselAdapter adapter = new DieselAdapter(false, false, false);
    try (JsonReader reader = adapter.reader(input)) {
      readExampleWithAsserts(reader);
    }
    try (JsonReader reader = adapter.reader(input)) {
      readExampleWithAsserts(reader);
    }
  }

  private void readExampleWithAsserts(JsonReader reader) {
    reader.beginObject();
    assertTrue(reader.hasNextField());
    assertEquals("name", reader.nextField());
    assertEquals("roberto", reader.nextString());
    assertTrue(reader.hasNextField());
    assertEquals("age", reader.nextField());
    assertEquals(42, reader.nextInt());
    assertTrue(reader.hasNextField());
    assertEquals("notes", reader.nextField());
    assertEquals("fooFooFoo", reader.nextString());
  }

  @Test
  void bigInt() {
    String input = "{\"name\":\"roberto\", \"val0\": 123, \"val1\": \"1234567890123456789\", \"val2\": 1234567890123456789 , \"notes\" :\"end\"}";

    DieselAdapter adapter = new DieselAdapter(false, false, false);
    try (JsonReader reader = adapter.reader(input)) {
      reader.beginObject();
      assertTrue(reader.hasNextField());
      assertEquals("name", reader.nextField());
      assertEquals("roberto", reader.nextString());
      assertTrue(reader.hasNextField());
      assertEquals("val0", reader.nextField());
      assertEquals(BigInteger.valueOf(123), reader.nextBigInteger());
      assertTrue(reader.hasNextField());
      assertEquals("val1", reader.nextField());
      assertEquals(BigInteger.valueOf(1234567890123456789L), reader.nextBigInteger());
      assertTrue(reader.hasNextField());
      assertEquals("val2", reader.nextField());
      assertEquals(BigInteger.valueOf(1234567890123456789L), reader.nextBigInteger());

      assertTrue(reader.hasNextField());
      assertEquals("notes", reader.nextField());
      assertEquals("end", reader.nextString());
    }
  }
}
