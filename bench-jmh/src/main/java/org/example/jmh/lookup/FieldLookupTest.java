package org.example.jmh.lookup;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.openjdk.jmh.annotations.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Reads objects where every property name is resolved via the PropertyNames hash lookup,
 * to measure the cost of confirming the key after a hash match.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(3)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
public class FieldLookupTest {

  private final Jsonb jsonb = Jsonb.builder().build();
  private final JsonType<Narrow5> narrowType = jsonb.type(Narrow5.class);
  private final JsonType<Wide20> wideType = jsonb.type(Wide20.class);
  private final JsonType<SpecialNames> specialType = jsonb.type(SpecialNames.class);

  private String narrowString;
  private byte[] narrowBytes;
  private String wideString;
  private byte[] wideBytes;
  private byte[] wideShuffledBytes;
  private byte[] wideUnknownBytes;
  private byte[] specialBytes;

  @Setup
  public void setup() {
    final List<String> narrow = List.of(
      pair("id", "a1b2c3"), pair("name", "Jane Citizen"), pair("email", "jane@example.org"),
      pair("city", "Auckland"), pair("country", "NZ"));
    narrowString = object(narrow);
    narrowBytes = narrowString.getBytes(StandardCharsets.UTF_8);

    final List<String> wide = new ArrayList<>(List.of(
      pair("id", "a1b2c3"), pair("firstName", "Jane"), pair("lastName", "Citizen"),
      pair("emailAddress", "jane@example.org"), pair("phoneNumber", "+64 21 555 0100"),
      pair("streetAddress", "1 Queen Street"), pair("city", "Auckland"), pair("region", "Auckland"),
      pair("postalCode", "1010"), pair("country", "NZ"), pair("companyName", "Example Ltd"),
      pair("jobTitle", "Engineer"), pair("department", "Platform"), pair("managerId", "z9y8x7"),
      pair("status", "ACTIVE"), pair("createdBy", "system"), pair("createdAt", "2026-01-02T03:04:05Z"),
      pair("notes", "some notes about this person"), "\"score\":42", "\"active\":true"));
    wideString = object(wide);
    wideBytes = wideString.getBytes(StandardCharsets.UTF_8);

    final List<String> shuffled = new ArrayList<>(wide);
    Collections.shuffle(shuffled, new Random(42));
    wideShuffledBytes = object(shuffled).getBytes(StandardCharsets.UTF_8);

    final List<String> withUnknown = new ArrayList<>();
    for (int i = 0; i < wide.size(); i++) {
      withUnknown.add(wide.get(i));
      withUnknown.add(pair("unknownKey" + i, "ignored"));
    }
    wideUnknownBytes = object(withUnknown).getBytes(StandardCharsets.UTF_8);

    specialBytes = "{\"café\":\"a\",\"naïve\":\"b\",\"plain\":\"c\"}".getBytes(StandardCharsets.UTF_8);
  }

  private static String pair(String name, String value) {
    return "\"" + name + "\":\"" + value + "\"";
  }

  private static String object(List<String> pairs) {
    return "{" + String.join(",", pairs) + "}";
  }

  @Benchmark
  public Narrow5 narrow_string() {
    return narrowType.fromJson(narrowString);
  }

  @Benchmark
  public Narrow5 narrow_bytes() {
    return narrowType.fromJson(narrowBytes);
  }

  @Benchmark
  public Wide20 wide_string() {
    return wideType.fromJson(wideString);
  }

  @Benchmark
  public Wide20 wide_bytes() {
    return wideType.fromJson(wideBytes);
  }

  @Benchmark
  public Wide20 wide_shuffled_bytes() {
    return wideType.fromJson(wideShuffledBytes);
  }

  @Benchmark
  public Wide20 wide_unknownKeys_bytes() {
    return wideType.fromJson(wideUnknownBytes);
  }

  @Benchmark
  public Wide20 wide_stream() {
    return wideType.fromJson(new ByteArrayInputStream(wideBytes));
  }

  @Benchmark
  public SpecialNames special_bytes() {
    return specialType.fromJson(specialBytes);
  }
}
