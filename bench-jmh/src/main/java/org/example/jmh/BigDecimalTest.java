package org.example.jmh;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.jackson.JacksonAdapter;
import org.example.jmh.model.MyMathBigD;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * Test for serializing a pojo with 10 properties
 */
@BenchmarkMode(Mode.Throughput)
@Timeout(time = 20)
@State(Scope.Benchmark)
@Warmup(iterations = 3)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BigDecimalTest {

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final Jsonb jsonbStandard = Jsonb.newBuilder().adapter(new JacksonAdapter()).build();
  private static final JsonType<MyMathBigD> jsonbType = jsonbStandard.type(MyMathBigD.class);

  private MyMathBigD testData;
  private String content;

  @Setup
  public void setup() {
    testData = new MyMathBigD();
    testData.setOne(new BigDecimal("78.89"));
    testData.setTwo(new BigDecimal("10"));
    content = "{\"one\":78.89,\"two\":10}";
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
    return jsonbType.toJson(testData);
  }

  @Benchmark
  public MyMathBigD fromJson_objectMapper() {
    try {
      return mapper.readValue(content, MyMathBigD.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Benchmark
  public MyMathBigD fromJson_jsonb() {
    return jsonbType.fromJson(content);
  }

  public static void main(String[] args) {
    BigDecimalTest test = new BigDecimalTest();
    test.setup();

    String m1 = test.toJson_objectMapper();
    String m2 = test.toJson_jsonb();
    MyMathBigD nestCust = test.fromJson_objectMapper();
    MyMathBigD nestCust1 = test.fromJson_jsonb();
    System.out.println("" + m1);
  }

}
