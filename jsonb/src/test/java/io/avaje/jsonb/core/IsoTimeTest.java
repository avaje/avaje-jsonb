package io.avaje.jsonb.core;

import io.avaje.json.AsciiCodec;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class IsoTimeTest {

  static final String[] EDGE = {
    "2020-01-01", "0000-01-01", "9999-12-31", "2020-02-29", "2021-02-29", "2020-02-30", "2021-13-01",
    "2021-00-10", "2021-01-00", "2020-1-01", "20200-01-01", "+10000-01-01", "-0001-01-01", "2020/01/01",
    "10:15", "10:15:30", "10:15:30.1", "10:15:30.123", "10:15:30.123456789", "10:15:30.1234567890",
    "10:15:30.", "24:00", "23:59:60", "23:60", "1:15", "10:15:3", "10-15-30", "10:15:30,5",
    "2020-01-01T10:15", "2020-01-01T10:15:30", "2020-01-01t10:15:30", "2020-01-01 10:15:30",
    "2020-01-01T10:15:30.5", "2020-01-01T24:00", "2020-01-01T23:59:60", "2021-02-29T10:15",
    "2020-01-01T10:15Z", "2020-01-01T10:15:30Z", "2020-01-01T10:15:30.123Z", "2020-01-01T10:15:30.123456Z",
    "2020-01-01T10:15:30.123456789Z", "2020-01-01T10:15:30z", "2020-01-01T23:59:60Z", "2020-01-01T24:00:00Z",
    "2020-01-01T10:15:30+01:00", "2020-01-01T10:15:30-05:30", "2020-01-01T10:15:30-00:00", "2020-01-01T10:15+18:00",
    "2020-01-01T10:15:30+19:00", "2020-01-01T10:15:30+01:60", "2020-01-01T10:15:30+0100", "2020-01-01T10:15:30+01",
    "2020-01-01T10:15:30+01:00:30", "2020-01-01T10:15:30.5+01:00", "2020-01-01T10:15:30+01:00[Europe/Paris]",
    "2020-01-01T10:15:30Z[UTC]", "2020-01-01T10:15:30*01:00", "2021-02-29T10:15:30Z", "2021-02-29T10:15:30+01:00",
    "0000-01-01T00:00:00Z", "9999-12-31T23:59:59.999999999Z", "+10000-01-01T00:00:00Z", "-0001-01-01T00:00:00Z",
    "2020-01-01T10:15:30.Z", "2020-01-01T10:15:30.1234567890Z", "", "x", "2020-01-01T", "2020-01-01T10:15:30+",
  };

  @Test
  void edgeCases_matchJdk() {
    List<String> bad = new ArrayList<>();
    for (String s : EDGE) {
      check(bad, s, "LocalDate", JavaTimeAdapters.LOCAL_DATE_ADAPTER::decode, LocalDate::parse);
      check(bad, s, "LocalTime", JavaTimeAdapters.LOCAL_TIME_ADAPTER::decode, LocalTime::parse);
      check(bad, s, "LocalDateTime", JavaTimeAdapters.LOCAL_DATE_TIME_ADAPTER::decode, LocalDateTime::parse);
      check(bad, s, "OffsetDateTime", JavaTimeAdapters.OFFSET_DATE_TIME_ADAPTER::decode, OffsetDateTime::parse);
      check(bad, s, "ZonedDateTime", JavaTimeAdapters.ZONED_DATE_TIME_ADAPTER::decode, ZonedDateTime::parse);
      check(bad, s, "Instant", JavaTimeAdapters.INSTANT_ADAPTER::decode, Instant::parse);
    }
    assertThat(bad).isEmpty();
  }

  @Test
  void random_roundTrip_matchJdk() {
    SplittableRandom r = new SplittableRandom(3);
    List<String> bad = new ArrayList<>();
    for (int i = 0; i < 100_000; i++) {
      long epochSecond = r.nextLong(-62167219200L, 253402300800L); // years 0000 to 9999
      int kind = r.nextInt(4);
      int nano = kind == 0 ? 0
        : kind == 1 ? r.nextInt(1000) * 1_000_000
        : kind == 2 ? r.nextInt(1_000_000) * 1_000
        : r.nextInt(1_000_000_000);
      if (r.nextInt(8) == 0) epochSecond -= epochSecond % 60;
      Instant instant = Instant.ofEpochSecond(epochSecond, nano);
      ZoneOffset offset = ZoneOffset.ofTotalSeconds(r.nextInt(-18 * 4, 18 * 4 + 1) * 900);
      OffsetDateTime odt = instant.atOffset(offset);
      LocalDateTime ldt = odt.toLocalDateTime();

      check(bad, instant.toString(), "Instant", JavaTimeAdapters.INSTANT_ADAPTER::decode, Instant::parse);
      check(bad, odt.toString(), "OffsetDateTime", JavaTimeAdapters.OFFSET_DATE_TIME_ADAPTER::decode, OffsetDateTime::parse);
      check(bad, odt.toZonedDateTime().toString(), "ZonedDateTime", JavaTimeAdapters.ZONED_DATE_TIME_ADAPTER::decode, ZonedDateTime::parse);
      check(bad, ldt.toString(), "LocalDateTime", JavaTimeAdapters.LOCAL_DATE_TIME_ADAPTER::decode, LocalDateTime::parse);
      check(bad, ldt.toLocalDate().toString(), "LocalDate", JavaTimeAdapters.LOCAL_DATE_ADAPTER::decode, LocalDate::parse);
      check(bad, ldt.toLocalTime().toString(), "LocalTime", JavaTimeAdapters.LOCAL_TIME_ADAPTER::decode, LocalTime::parse);
      if (!JavaTimeAdapters.INSTANT_ADAPTER.decode(instant.toString()).equals(instant)) bad.add("roundTrip " + instant);
    }
    assertThat(bad).isEmpty();
  }

  @Test
  void random_encode_matchesToString() {
    SplittableRandom r = new SplittableRandom(5);
    String[] zones = {"Europe/Paris", "America/St_Johns", "Asia/Kathmandu", "UTC", "GMT", "Etc/GMT+5", "UTC+01:30"};
    List<String> bad = new ArrayList<>();
    for (int i = 0; i < 100_000; i++) {
      long epochSecond = r.nextLong(-62167219200L + 86400, 253402300800L - 86400);
      int kind = r.nextInt(5);
      int nano = kind == 0 ? 0
        : kind == 1 ? r.nextInt(1000) * 1_000_000
        : kind == 2 ? r.nextInt(1_000_000) * 1_000
        : r.nextInt(1_000_000_000);
      if (r.nextInt(4) == 0) epochSecond -= epochSecond % 60;
      if (r.nextInt(8) == 0) epochSecond -= epochSecond % 3600;
      Instant instant = Instant.ofEpochSecond(epochSecond, nano);
      ZoneOffset offset = r.nextInt(8) == 0
        ? ZoneOffset.ofTotalSeconds(r.nextInt(-18 * 3600, 18 * 3600 + 1))
        : ZoneOffset.ofTotalSeconds(r.nextInt(-18 * 4, 18 * 4 + 1) * 900);
      OffsetDateTime odt = instant.atOffset(offset);
      ZonedDateTime zdt = r.nextBoolean() ? odt.toZonedDateTime() : instant.atZone(java.time.ZoneId.of(zones[r.nextInt(zones.length)]));
      LocalDateTime ldt = odt.toLocalDateTime();

      checkEncode(bad, JavaTimeAdapters.INSTANT_ADAPTER, instant);
      checkEncode(bad, JavaTimeAdapters.OFFSET_DATE_TIME_ADAPTER, odt);
      checkEncode(bad, JavaTimeAdapters.ZONED_DATE_TIME_ADAPTER, zdt);
      checkEncode(bad, JavaTimeAdapters.LOCAL_DATE_TIME_ADAPTER, ldt);
      checkEncode(bad, JavaTimeAdapters.LOCAL_DATE_ADAPTER, ldt.toLocalDate());
      checkEncode(bad, JavaTimeAdapters.LOCAL_TIME_ADAPTER, ldt.toLocalTime());
    }
    assertThat(bad).isEmpty();
  }

  @Test
  void encode_outsideFastRange_usesToString() {
    Jsonb jsonb = Jsonb.builder().build();
    LocalDateTime[] values = {LocalDateTime.MIN, LocalDateTime.MAX, LocalDateTime.of(-1, 1, 1, 0, 0), LocalDateTime.of(10000, 1, 1, 0, 0), LocalDateTime.of(0, 1, 1, 0, 0), LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999_999_999)};
    for (LocalDateTime ldt : values) {
      assertThat(jsonb.toJson(ldt)).isEqualTo("\"" + ldt + "\"");
      assertThat(jsonb.toJson(ldt.toLocalDate())).isEqualTo("\"" + ldt.toLocalDate() + "\"");
      OffsetDateTime odt = ldt.atOffset(ZoneOffset.ofHoursMinutesSeconds(-5, -30, -15));
      assertThat(jsonb.toJson(odt)).isEqualTo("\"" + odt + "\"");
      ZonedDateTime zdt = ldt.atZone(ZoneOffset.UTC);
      assertThat(jsonb.toJson(zdt)).isEqualTo("\"" + zdt + "\"");
    }
    for (Instant instant : new Instant[]{Instant.MIN, Instant.MAX, Instant.ofEpochSecond(-62167219201L), Instant.ofEpochSecond(253402300800L), Instant.ofEpochSecond(-62167219200L), Instant.ofEpochSecond(253402300799L, 999_999_999)}) {
      assertThat(jsonb.toJson(instant)).isEqualTo("\"" + instant + "\"");
    }
  }

  private static <T> void checkEncode(List<String> bad, AsciiCodec<T> codec, T value) {
    String expected = value.toString();
    int max = codec.maxLength(value);
    if (max < expected.length()) {
      bad.add("maxLength " + max + " for " + expected);
      return;
    }
    byte[] buf = new byte[max + 4];
    int end = codec.encode(value, buf, 2);
    String actual = new String(buf, 2, end - 2, StandardCharsets.ISO_8859_1);
    if (!expected.equals(actual)) {
      bad.add("encode " + expected + " got " + actual);
    }
  }

  @Test
  void stream_acrossBufferBoundaries() {
    // values straddle the stream buffer refills, so some take the readString() fallback
    List<LocalDate> dates = new ArrayList<>();
    SplittableRandom r = new SplittableRandom(11);
    for (int i = 0; i < 10_000; i++) {
      dates.add(LocalDate.ofEpochDay(r.nextLong(-700_000, 2_900_000)));
    }
    JsonType<List<LocalDate>> dateList = Jsonb.builder().build().type(LocalDate.class).list();
    String json = dateList.toJson(dates);
    assertThat(dateList.fromJson(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))).isEqualTo(dates);
  }

  @Test
  void escaped_and_nonAscii_fallBackToString() {
    var type = Jsonb.builder().build().type(LocalDate.class);
    assertThat(type.fromJson("\"\\u0032020-01-15\"")).isEqualTo(LocalDate.of(2020, 1, 15));
    String nonAscii = "2020-01-1\u00e9";
    assertThat(result(() -> type.fromJson("\"" + nonAscii + "\"")))
      .isEqualTo(result(() -> LocalDate.parse(nonAscii)));
  }

  private static Object result(java.util.function.Supplier<Object> supplier) {
    try {
      return supplier.get();
    } catch (RuntimeException e) {
      return e.getClass().getName() + ": " + e.getMessage();
    }
  }

  @Test
  void utilDate_roundTrip() {
    var dateType = Jsonb.builder().build().type(Date.class);
    for (String iso : new String[]{"\"1970-01-01T00:00:00Z\"", "\"2024-06-15T12:34:56.789Z\"", "\"1969-12-31T23:59:59.001Z\""}) {
      Date parsed = dateType.fromJson(iso);
      assertThat(parsed).isEqualTo(Date.from(Instant.parse(iso.substring(1, iso.length() - 1))));
      assertThat(dateType.toJson(parsed)).isEqualTo(iso);
    }
    assertThat(dateType.fromJson("null")).isNull();
  }

  private static <T> void check(List<String> bad, String s, String type, Function<String, T> fast, Function<String, T> jdk) {
    Object expected;
    Object actual;
    try {
      expected = jdk.apply(s);
    } catch (RuntimeException e) {
      expected = e.getClass().getName() + ": " + e.getMessage();
    }
    try {
      actual = fast.apply(s);
    } catch (RuntimeException e) {
      actual = e.getClass().getName() + ": " + e.getMessage();
    }
    compare(bad, s, type, expected, actual);

    // the byte path, with content in the middle of a buffer
    byte[] buf = ("@\"_" + s + "\"@").getBytes(StandardCharsets.ISO_8859_1);
    try {
      actual = codec(type).decode(buf, 3, 3 + s.length());
    } catch (RuntimeException e) {
      actual = e.getClass().getName() + ": " + e.getMessage();
    }
    compare(bad, s, type + "(bytes)", expected, actual);
  }

  private static void compare(List<String> bad, String s, String type, Object expected, Object actual) {
    // compare via equals and toString, ZonedDateTime equality includes the zone
    if (!Objects.equals(expected, actual) || !String.valueOf(expected).equals(String.valueOf(actual))) {
      bad.add(type + " '" + s + "' expected " + expected + " but got " + actual);
    }
  }

  private static AsciiCodec<?> codec(String type) {
    switch (type) {
      case "LocalDate": return JavaTimeAdapters.LOCAL_DATE_ADAPTER;
      case "LocalTime": return JavaTimeAdapters.LOCAL_TIME_ADAPTER;
      case "LocalDateTime": return JavaTimeAdapters.LOCAL_DATE_TIME_ADAPTER;
      case "OffsetDateTime": return JavaTimeAdapters.OFFSET_DATE_TIME_ADAPTER;
      case "ZonedDateTime": return JavaTimeAdapters.ZONED_DATE_TIME_ADAPTER;
      case "Instant": return JavaTimeAdapters.INSTANT_ADAPTER;
      default: throw new IllegalArgumentException(type);
    }
  }
}
