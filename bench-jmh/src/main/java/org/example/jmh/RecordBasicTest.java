package org.example.jmh;

import com.dslplatform.json.DslJson;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.jackson.JacksonAdapter;
import io.avaje.jsonb.stream.JsonStream;
import org.example.jmh.model.NarrowNamesRecord;
import org.example.jmh.model.WideNamesRecord;
import org.openjdk.jmh.annotations.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
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

  private static final Jsonb jsonbX = Jsonb.newBuilder().adapter(new JacksonAdapter())
    .add(WideNamesRecord.class, MyCustomWideAdapter::new)
    .add(NarrowNamesRecord.class, MyCustomNarrowAdapter::new)
    .build();

  private static final JsonType<WideNamesRecord> jsonbXWide = jsonbX.type(WideNamesRecord.class);
  private static final JsonType<NarrowNamesRecord> jsonbXNarrow = jsonbX.type(NarrowNamesRecord.class);

  private static final Jsonb jsonbStandard = Jsonb.newBuilder().adapter(new JacksonAdapter()).build();
  private static final JsonType<WideNamesRecord> jsonbWideNames = jsonbStandard.type(WideNamesRecord.class);
  private static final JsonType<NarrowNamesRecord> jsonbNarrowNames = jsonbStandard.type(NarrowNamesRecord.class);

//  private static final Jsonb jakartaJsonb = Jsonb.newBuilder().adapter(new JakartaAdapter()).build();
//  private static final JsonType<WideNamesRecord> jakartaJsonbWideNames = jakartaJsonb.type(WideNamesRecord.class);
//  private static final JsonType<NarrowNamesRecord> jakartaJsonbNarrowNames = jakartaJsonb.type(NarrowNamesRecord.class);

  private static final Jsonb dieselJsonb = Jsonb.newBuilder().adapter(new JsonStream(false, false, false)).build();
  private static final JsonType<WideNamesRecord> dieselJsonbWideNames = dieselJsonb.type(WideNamesRecord.class);
  private static final JsonType<NarrowNamesRecord> dieselJsonbNarrowNames = dieselJsonb.type(NarrowNamesRecord.class);


//  private static final Jsonb jakartaJsonbBase = Jsonb.newBuilder().adapter(new JakartaIOAdapter())
//    .add(WideNamesRecord.class, MyCustomWideAdapter::new)
//    .add(NarrowNamesRecord.class, MyCustomNarrowAdapter::new)
//    .build();
//
//  private static final JsonType<WideNamesRecord> jakartaJsonbWideNamesBase = jakartaJsonbBase.type(WideNamesRecord.class);
//  private static final JsonType<NarrowNamesRecord> jakartaJsonbNarrowNamesBase = jakartaJsonbBase.type(NarrowNamesRecord.class);

  private NarrowNamesRecord testDataNarrowNames;
  private WideNamesRecord testDataWideNames, testDataWideNames2;
  private String content;

  private final DslJson<Object> dsl = new DslJson<>();

  @Setup
  public void setup() {
    //testData = new MyRecord("property1property1property1property1", "property2property2property2property2", "property3property3property3property3", "property4property4property4property4", "property5property5property5property5");
    testDataWideNames = new WideNamesRecord("1", "2", "3", "4", "5");
    testDataWideNames2 = new WideNamesRecord("property1property1property1property1", "property2property2property2property2", "property3property3property3property3", "property4property4property4property4", "property5property5property5property5");
    testDataNarrowNames = new NarrowNamesRecord("property1property1property1property1", "property2property2property2property2", "property3property3property3property3", "property4property4property4property4", "property5property5property5property5");
    content = "{\"firstNameProperty1\":\"property1property1property1property1\",\"lastNameProperty2\":\"property2property2property2property2\",\"anotherSimilarProperty3\":\"property3property3property3property3\",\"moreOrLessProperty4\":\"property4property4property4property4\",\"lastButNotLeastProperty5\":\"property5property5property5property5\"}";
  }

  //@Benchmark
  public String toJson_wideNames2_dsl() throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    dsl.serialize(testDataWideNames2, os);
    return os.toString(StandardCharsets.UTF_8);
  }

  //@Benchmark
  public String toJson_wideNames2_objectMapper() {
    try {
      return mapper.writeValueAsString(testDataWideNames2);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  //@Benchmark
  public String toJson_wideNames2_jsonb_diesel() {
    return dieselJsonbWideNames.toJson(testDataWideNames2);
  }

  //@Benchmark
  public String toJson_wideNames2_jsonb_jackson() {
    return jsonbWideNames.toJson(testDataWideNames2);
  }


  // ---------------------------------------------

  //@Benchmark
  public String toJson_wideNames_dsl() throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    dsl.serialize(testDataWideNames, os);
    return os.toString(StandardCharsets.UTF_8);
  }

  //@Benchmark
  public String toJson_wideNames_objectMapper() {
    try {
      return mapper.writeValueAsString(testDataWideNames);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  //@Benchmark
  public String toJson_wideNames_jsonb_diesel() {
    return dieselJsonbWideNames.toJson(testDataWideNames);
  }

  //@Benchmark
  public String toJson_wideNames_jsonb_jackson() {
    return jsonbWideNames.toJson(testDataWideNames);
  }

  // ---------------------------------------------------------

  //@Benchmark
  public String toJson_narrowNames_dsl() throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    dsl.serialize(testDataNarrowNames, os);
    return os.toString(StandardCharsets.UTF_8);
  }

  //@Benchmark
  public String toJson_narrowNames_objectMapper() {
    try {
      return mapper.writeValueAsString(testDataNarrowNames);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  //@Benchmark
  public String toJson_narrowNames_jsonb_diesel() {
    return dieselJsonbNarrowNames.toJson(testDataNarrowNames);
  }

  //@Benchmark
  public String toJson_narrowNames_jsonb_jackson() {
    return jsonbNarrowNames.toJson(testDataNarrowNames);
  }

  // -------------------------------------------

  @Benchmark
  public WideNamesRecord fromJson_wideNames_objectMapper() {
    try {
      return mapper.readValue(content, WideNamesRecord.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Benchmark
  public WideNamesRecord fromJson_wideNames_jsonb_jackson() {
    return jsonbWideNames.fromJson(content);
  }

  @Benchmark
  public WideNamesRecord fromJson_wideNames_jsonb_diesel() {
    return dieselJsonbWideNames.fromJson(content);
  }


  @Benchmark
  public WideNamesRecord fromJson_wideNames_dsl() throws IOException {
    byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
    return dsl.deserialize(WideNamesRecord.class, bytes, bytes.length);
  }

  //@Benchmark
  public String toJson_wideNames_method() {
    try {
      SegmentedStringWriter sw = new SegmentedStringWriter(jsonFactory._getBufferRecycler());
      //StringWriter sw = new StringWriter(300);
      JsonGenerator writer = jsonFactory.createGenerator(sw);
      toJson1(writer, testDataWideNames);
      writer.close();
      return sw.getAndClear();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void toJson1(JsonGenerator writer, WideNamesRecord myRecord) throws IOException {
    writer.writeStartObject();
    writer.writeFieldName("firstNameProperty1");
    writer.writeString(myRecord.firstNameProperty1());
    writer.writeFieldName("lastNameProperty2");
    writer.writeString(myRecord.lastNameProperty2());
    writer.writeFieldName("anotherSimilarProperty3");
    writer.writeString(myRecord.anotherSimilarProperty3());
    writer.writeFieldName("moreOrLessProperty4");
    writer.writeString(myRecord.moreOrLessProperty4());
    writer.writeFieldName("lastButNotLeastProperty5");
    writer.writeString(myRecord.lastButNotLeastProperty5());
    writer.writeEndObject();
  }

  final SerializedString p1 = key("firstNameProperty1");
  final SerializedString p2 = key("lastNameProperty2");
  final SerializedString p3 = key("anotherSimilarProperty3");
  final SerializedString p4 = key("moreOrLessProperty4");
  final SerializedString p5 = key("lastButNotLeastProperty5");

  SerializedString key(String key) {
    SerializedString v = new SerializedString(key);
    v.asQuotedUTF8();
    v.asQuotedChars();
    v.asQuotedChars();
    return v;
  }

  //@Benchmark
  public String toJson_wideNames_method2() {
    try {
      SegmentedStringWriter sw = new SegmentedStringWriter(jsonFactory._getBufferRecycler());
      //StringWriter sw = new StringWriter(300);
      JsonGenerator writer = jsonFactory.createGenerator(sw);
      toJson2(writer, testDataWideNames);
      writer.close();
      return sw.getAndClear();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void toJson2(JsonGenerator writer, WideNamesRecord myRecord) throws IOException {
    writer.writeStartObject();
    writer.writeFieldName(p1);
    writer.writeString(myRecord.firstNameProperty1());
    writer.writeFieldName(p2);
    writer.writeString(myRecord.lastNameProperty2());
    writer.writeFieldName(p3);
    writer.writeString(myRecord.anotherSimilarProperty3());
    writer.writeFieldName(p4);
    writer.writeString(myRecord.moreOrLessProperty4());
    writer.writeFieldName(p5);
    writer.writeString(myRecord.lastButNotLeastProperty5());
    writer.writeEndObject();
  }

  public static void main(String[] args) throws IOException {
    RecordBasicTest test = new RecordBasicTest();
    test.setup();

    WideNamesRecord wideNamesRecord = test.fromJson_wideNames_dsl();


//    String m1 = test.toJson_wideNames_method();
//    String m2 = test.toJson_wideNames_method2();
//    String asJson1 = test.toJson_wideNames_objectMapper();
//    String asJson2 = test.toJson_wideNames_jsonb_diesel();
//    String asJson3 = test.toJson_wideNames_jsonb_jackson();
//
//    String asJsonnarrow1 = test.toJson_narrowNames_objectMapper();
//    String asJsonnarrow2 = test.toJson_narrowNames_jsonb_diesel();
//    String asJsonnarrow3 = test.toJson_narrowNames_jsonb_jackson();
//    String asJsonnarrow3a = test.toJson_narrowNames_jsonb_jakarta_preparedKey();
//    String asJsonnarrow3b = test.toJson_narrowNames_jsonb_jakarta_preparedKey();
//    String asJsonnarrow3c = test.toJson_narrowNames_jsonb_jakarta_preparedKey();
//    String asJsonnarrow3d = test.toJson_narrowNames_jsonb_jakarta_preparedKey();
//    String asJsonnarrow3e = test.toJson_narrowNames_jsonb_jakarta_preparedKey();
//    String asJsonnarrow3f = test.toJson_narrowNames_jsonb_jakarta_preparedKey();
//
//    System.out.println(asJson1);
//    System.out.println(asJson2);
    WideNamesRecord from1 = test.fromJson_wideNames_jsonb_jackson();
    WideNamesRecord from2 = test.fromJson_wideNames_objectMapper();
    System.out.println("" + from1 + from2);
  }

}
