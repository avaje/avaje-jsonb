package org.example.jmh;

import com.bluelinelabs.logansquare.LoganSquare;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.JsonView;
import io.avaje.jsonb.Jsonb;
import org.example.jmh.model.OtherPropertyData;
import org.example.jmh.model.SomePropertyData;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.io.StringReader;
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
public class PropertyStrTenTest {

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final Jsonb jsonb = Jsonb.newBuilder().build();
  private static final JsonType<SomePropertyData> jsonbType = jsonb.type(SomePropertyData.class);
  private JsonView<SomePropertyData> allView;
  private JsonView<SomePropertyData> prop35View;

  private JsonAdapter<SomePropertyData> moshiAdapter;

  private SomePropertyData testData;
  private OtherPropertyData loganTestData;
  private String content;

  @Setup
  public void setup() {

    Jsonb viewAwareJsonb = Jsonb.newBuilder().add(SomePropertyData.class, MyViewAdapter::new).build();
    JsonType<SomePropertyData> viewAwareType = viewAwareJsonb.type(SomePropertyData.class);
    allView = viewAwareType.view("(*)");
    prop35View = viewAwareType.view("(anotherSimilarProperty3, lastButNotLeastProperty5)");

    var moshi = new Moshi.Builder().build();
    moshiAdapter = moshi.adapter(SomePropertyData.class);
    testData =       new SomePropertyData("firstNameProperty1", "lastNameProperty2", "anotherSimilarProperty3", "moreOrLessProperty4", "lastButNotLeastProperty5", "property6", "property7", "property8", "property9", "property10");
    loganTestData = new OtherPropertyData("firstNameProperty1", "lastNameProperty2", "anotherSimilarProperty3", "moreOrLessProperty4", "lastButNotLeastProperty5", "property6", "property7", "property8", "property9", "property10");

    content = "{\"prop1\":\"firstNameProperty1\",\"prop2\":\"lastNameProperty2\",\"prop3\":\"anotherSimilarProperty3\",\"prop4\":\"moreOrLessProperty4\",\"prop5\":\"lastButNotLeastProperty5\",\"prop6\":\"property6\",\"prop7\":\"property7\",\"prop8\":\"property8\",\"prop9\":\"property9\",\"prop10\":\"property10\"}";
  }

  @Benchmark
  public String toJson_objectMapper() {
    try {
      return mapper.writeValueAsString(testData);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  //@Benchmark
  public String toJson_moshi() {
    return moshiAdapter.toJson(testData);
  }

  @Benchmark
  public String toJson_jsonb() {
    try {
      return jsonbType.toJson(testData);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Benchmark
  public String toJson_jsonb_viewAll() {
    try {
      return allView.toJson(testData);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Benchmark
  public String toJson_jsonb_viewProp35() {
    try {
      return prop35View.toJson(testData);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  //@Benchmark
  public String toJson_logan() {
    try {
      return LoganSquare.serialize(loganTestData);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  //@Benchmark
  public OtherPropertyData fromJson_logan() {
    try {
      return LoganSquare.parse(content, OtherPropertyData.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Benchmark
  public SomePropertyData fromJson_objectMapper() {
    try {
      return mapper.readValue(content, SomePropertyData.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  //@Benchmark
  public SomePropertyData fromJson_objectMapper_reader() {
    try {
      return mapper.readValue(new StringReader(content), SomePropertyData.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  //@Benchmark
  public SomePropertyData fromJson_moshi() {
    try {
      return moshiAdapter.fromJson(content);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Benchmark
  public SomePropertyData fromJson_jsonb() {
    try {
      return jsonbType.fromJson(content);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  //@Benchmark
  public SomePropertyData fromJson_base_reader() {
    try {
      return jsonbType.fromJson(new StringReader(content));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }


  public static void main(String[] args) {
    PropertyStrTenTest test = new PropertyStrTenTest();
    test.setup();

    String asJson = test.toJson_jsonb_viewProp35();
    System.out.println(asJson);
    SomePropertyData somePropertyData0 = test.fromJson_objectMapper();
    SomePropertyData somePropertyData1 = test.fromJson_jsonb();
    System.out.println(somePropertyData1);
  }

}
