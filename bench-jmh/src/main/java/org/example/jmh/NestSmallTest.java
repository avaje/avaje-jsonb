package org.example.jmh;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.jackson.JacksonAdapter;
import org.example.jmh.model.NarrowNamesRecord;
import org.example.jmh.model.NestAddress;
import org.example.jmh.model.NestCust;
import org.example.jmh.model.WideNamesRecord;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Test for serializing a pojo with 10 properties
 */
@BenchmarkMode(Mode.Throughput)
@Timeout(time = 20)
@State(Scope.Benchmark)
@Warmup(iterations = 3)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class NestSmallTest {

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final Jsonb jsonbStandard = Jsonb.newBuilder().adapter(new JacksonAdapter()).build();
  private static final JsonType<NestCust> jsonbNestCust = jsonbStandard.type(NestCust.class);

  private NestCust testData;
  private String content;

  @Setup
  public void setup() {

    NestAddress bill = new NestAddress("123 foobar street", "somewhere interesting", "forever", "place");
    NestAddress ship = new NestAddress("234 foobar street", "somewhere else", "temporal", "person");
    testData = new NestCust(424234L, "my first customer", Instant.now().toString(), Instant.now().toString(), "5asodmasd", bill, ship);
    content = "{\"id\":424234,\"name\":\"my first customer\",\"whenCreated\":\"2021-11-18T08:54:25.842796352Z\",\"whenModified\":\"2021-11-18T08:54:25.848786953Z\",\"notes\":\"5asodmasd\",\"billingAddress\":{\"street1\":\"123 foobar street\",\"street2\":\"somewhere interesting\",\"suburb\":\"forever\",\"city\":\"place\"},\"shippingAddress\":{\"street1\":\"234 foobar street\",\"street2\":\"somewhere else\",\"suburb\":\"temporal\",\"city\":\"person\"}}";
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
    return jsonbNestCust.toJson(testData);
  }

  @Benchmark
  public NestCust fromJson_objectMapper() {
    try {
      return mapper.readValue(content, NestCust.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }


  @Benchmark
  public NestCust fromJson_jsonb() {
    return jsonbNestCust.fromJson(content);
  }

  public static void main(String[] args) {
    NestSmallTest test = new NestSmallTest();
    test.setup();

    String m1 = test.toJson_objectMapper();
    String m2 = test.toJson_jsonb();

    NestCust nestCust = test.fromJson_objectMapper();
    NestCust nestCust1 = test.fromJson_jsonb();
    System.out.println("" + m1 + m2);
  }


//Benchmark                             Mode  Cnt     Score     Error   Units
//NestSmallTest.fromJson_jsonb         thrpt    4  1257.446 ± 158.361  ops/ms
//NestSmallTest.fromJson_objectMapper  thrpt    4  1118.420 ± 180.670  ops/ms
//NestSmallTest.toJson_jsonb           thrpt    4  2465.142 ± 375.753  ops/ms
//NestSmallTest.toJson_objectMapper    thrpt    4  2320.639 ± 699.458  ops/ms

}
