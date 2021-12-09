package org.example.jmh;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.jackson.JacksonIOAdapter;
import org.example.jmh.model.MyMathBigD;
import org.example.jmh.model.MyMathBigInt;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

/**
 * Test for serializing a pojo with 10 properties
 */
@BenchmarkMode(Mode.Throughput)
@Timeout(time = 20)
@State(Scope.Benchmark)
@Warmup(iterations = 3)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BigIntTest {

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final Jsonb jsonbStandard = Jsonb.newBuilder().adapter(new JacksonIOAdapter()).build();
  private static final JsonType<MyMathBigInt> jsonbType = jsonbStandard.type(MyMathBigInt.class);

  private MyMathBigInt testData;
  private String content;

  @Setup
  public void setup() {
    testData = new MyMathBigInt();
    testData.setOne(new BigInteger("12345"));
    testData.setTwo(new BigInteger("987576"));
    content = "{\"one\":12345,\"two\":987576}";
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
  public MyMathBigInt fromJson_objectMapper() {
    try {
      return mapper.readValue(content, MyMathBigInt.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Benchmark
  public MyMathBigInt fromJson_jsonb() {
    return jsonbType.fromJson(content);
  }

  public static void main(String[] args) {
    BigIntTest test = new BigIntTest();
    test.setup();

    String m1 = test.toJson_objectMapper();
    String m2 = test.toJson_jsonb();
    MyMathBigInt nestCust = test.fromJson_objectMapper();
    MyMathBigInt nestCust1 = test.fromJson_jsonb();
    System.out.println("" + m1);
  }

}
