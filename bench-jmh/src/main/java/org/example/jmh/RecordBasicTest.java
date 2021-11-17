package org.example.jmh;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.example.jmh.model.MyRecord;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.concurrent.TimeUnit;

/**
 * Test for serializing a pojo with 10 properties
 */
@BenchmarkMode(Mode.Throughput)
@Timeout(time = 20)
@State(Scope.Benchmark)
@Warmup(iterations = 3)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class RecordBasicTest {

  private static final ObjectMapper mapper = new ObjectMapper();
  private static final JsonFactory jsonFactory = new JsonFactory();

  private static final Jsonb jsonb = Jsonb.newBuilder().build();
  private static final JsonType<MyRecord> jsonbType = jsonb.type(MyRecord.class);

  private MyRecord testData;
  private String content;

  @Setup
  public void setup() {
    //testData = new MyRecord("property1property1property1property1", "property2property2property2property2", "property3property3property3property3", "property4property4property4property4", "property5property5property5property5");
    testData = new MyRecord("1", "2", "3", "4", "5");
    content = "{\"prop1\":\"property1property1property1property1\",\"prop2\":\"property2property2property2property2\",\"prop3\":\"property3property3property3property3\",\"prop4\":\"property4property4property4property4\",\"prop5\":\"property5property5property5property5\"}";
  }

  @Benchmark
  public String toJson_objectMapper() {
    try {
      return mapper.writeValueAsString(testData);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Benchmark
  public String toJson_jsonb() {
    try {
      return jsonbType.toJson(testData);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }


  //@Benchmark
  public MyRecord fromJson_objectMapper() {
    try {
      return mapper.readValue(content, MyRecord.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  //@Benchmark
  public MyRecord fromJson_jsonb() {
    try {
      return jsonbType.fromJson(content);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Benchmark
  public String toJson_method() {
    try {
      SegmentedStringWriter sw = new SegmentedStringWriter(jsonFactory._getBufferRecycler());
      //StringWriter sw = new StringWriter(300);
      JsonGenerator writer = jsonFactory.createGenerator(sw);
      toJson1(writer, testData);
      writer.close();
      return sw.getAndClear();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void toJson1(JsonGenerator writer, MyRecord myRecord) throws IOException {
    writer.writeStartObject();
    writer.writeFieldName("prop1");
    writer.writeString(myRecord.prop1());
    writer.writeFieldName("prop2");
    writer.writeString(myRecord.prop2());
    writer.writeFieldName("prop3");
    writer.writeString(myRecord.prop3());
    writer.writeFieldName("prop4");
    writer.writeString(myRecord.prop4());
    writer.writeFieldName("prop5");
    writer.writeString(myRecord.prop5());
    writer.writeEndObject();
  }

  final SerializedString p1 = key("prop1");
  final SerializedString p2 = key("prop2");
  final SerializedString p3 = key("prop3");
  final SerializedString p4 = key("prop4");
  final SerializedString p5 = key("prop5");

  SerializedString key(String key) {
    SerializedString v = new SerializedString("prop1");
    v.asQuotedUTF8();
    v.asQuotedChars();
    v.asQuotedChars();
    return v;
  }

  @Benchmark
  public String toJson_method2() {
    try {
      SegmentedStringWriter sw = new SegmentedStringWriter(jsonFactory._getBufferRecycler());
      //StringWriter sw = new StringWriter(300);
      JsonGenerator writer = jsonFactory.createGenerator(sw);
      toJson2(writer, testData);
      writer.close();
      return sw.getAndClear();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void toJson2(JsonGenerator writer, MyRecord myRecord) throws IOException {
    writer.writeStartObject();
    writer.writeFieldName(p1);
    writer.writeString(myRecord.prop1());
    writer.writeFieldName(p2);
    writer.writeString(myRecord.prop2());
    writer.writeFieldName(p3);
    writer.writeString(myRecord.prop3());
    writer.writeFieldName(p4);
    writer.writeString(myRecord.prop4());
    writer.writeFieldName(p5);
    writer.writeString(myRecord.prop5());
    writer.writeEndObject();
  }

  public static void main(String[] args) {
    RecordBasicTest test = new RecordBasicTest();
    test.setup();

    String m1 = test.toJson_method();
    String m2 = test.toJson_method2();

    String asJson1 = test.toJson_objectMapper();
    String asJson2 = test.toJson_jsonb();
    System.out.println(asJson1);
    System.out.println(asJson2);
    MyRecord from1 = test.fromJson_jsonb();
    MyRecord from2 = test.fromJson_objectMapper();
    System.out.println("" + from1 + from2);
  }

}
