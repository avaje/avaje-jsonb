package io.avaje.jsonb.core;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
      check(bad, s, "LocalDate", IsoTime::parseLocalDate, LocalDate::parse);
      check(bad, s, "LocalTime", IsoTime::parseLocalTime, LocalTime::parse);
      check(bad, s, "LocalDateTime", IsoTime::parseLocalDateTime, LocalDateTime::parse);
      check(bad, s, "OffsetDateTime", IsoTime::parseOffsetDateTime, OffsetDateTime::parse);
      check(bad, s, "ZonedDateTime", IsoTime::parseZonedDateTime, ZonedDateTime::parse);
      check(bad, s, "Instant", IsoTime::parseInstant, Instant::parse);
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
      if (!IsoTime.format(instant).equals(instant.toString())) bad.add("format " + instant);
      ZoneOffset offset = ZoneOffset.ofTotalSeconds(r.nextInt(-18 * 4, 18 * 4 + 1) * 900);
      OffsetDateTime odt = instant.atOffset(offset);
      LocalDateTime ldt = odt.toLocalDateTime();

      check(bad, instant.toString(), "Instant", IsoTime::parseInstant, Instant::parse);
      check(bad, odt.toString(), "OffsetDateTime", IsoTime::parseOffsetDateTime, OffsetDateTime::parse);
      check(bad, odt.toZonedDateTime().toString(), "ZonedDateTime", IsoTime::parseZonedDateTime, ZonedDateTime::parse);
      check(bad, ldt.toString(), "LocalDateTime", IsoTime::parseLocalDateTime, LocalDateTime::parse);
      check(bad, ldt.toLocalDate().toString(), "LocalDate", IsoTime::parseLocalDate, LocalDate::parse);
      check(bad, ldt.toLocalTime().toString(), "LocalTime", IsoTime::parseLocalTime, LocalTime::parse);
      if (!IsoTime.parseInstant(instant.toString()).equals(instant)) bad.add("roundTrip " + instant);
    }
    assertThat(bad).isEmpty();
  }

  @Test
  void format_outsideFastRange() {
    for (Instant instant : new Instant[]{Instant.MIN, Instant.MAX, Instant.EPOCH, Instant.ofEpochSecond(-62167219201L), Instant.ofEpochSecond(253402300800L)}) {
      assertThat(IsoTime.format(instant)).isEqualTo(instant.toString());
    }
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
    // compare via equals and toString, ZonedDateTime equality includes the zone
    if (!Objects.equals(expected, actual) || !String.valueOf(expected).equals(String.valueOf(actual))) {
      bad.add(type + " '" + s + "' expected " + expected + " but got " + actual);
    }
  }
}
