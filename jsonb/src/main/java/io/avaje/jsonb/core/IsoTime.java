package io.avaje.jsonb.core;

import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/** Fast paths for the common ISO-8601 forms of java.time values. */
final class IsoTime {

  private static final long NANOS_PER_SECOND = 1_000_000_000L;
  private static final int SECONDS_PER_DAY = 86_400;

  /** Epoch second of 0000-01-01T00:00:00Z. */
  private static final long SECONDS_0000_TO_1970 = -62_167_219_200L;

  /** Epoch second of 10000-01-01T00:00:00Z. */
  private static final long SECONDS_10000_TO_1970 = 253_402_300_800L;

  private static final int[] FRACTION_SCALE = {
    0, 100_000_000, 10_000_000, 1_000_000, 100_000, 10_000, 1_000, 100, 10, 1
  };

  private IsoTime() {}

  static LocalDate parseLocalDate(String s) {
    if (s.length() == 10) {
      try {
        final var date = date(s);
        if (date != null) {
          return date;
        }
      } catch (final DateTimeException e) {
        // fall back for the JDK exception
      }
    }
    return LocalDate.parse(s);
  }

  static LocalTime parseLocalTime(String s) {
    final var nanoOfDay = nanoOfDay(s, 0, s.length(), false);
    if (nanoOfDay >= 0) {
      return LocalTime.ofNanoOfDay(nanoOfDay);
    }
    return LocalTime.parse(s);
  }

  static LocalDateTime parseLocalDateTime(String s) {
    final var len = s.length();
    if (len >= 16 && s.charAt(10) == 'T') {
      try {
        final var date = date(s);
        final var nanoOfDay = nanoOfDay(s, 11, len, false);
        if (date != null && nanoOfDay >= 0) {
          return LocalDateTime.of(date, LocalTime.ofNanoOfDay(nanoOfDay));
        }
      } catch (final DateTimeException e) {
        // fall back for the JDK exception
      }
    }
    return LocalDateTime.parse(s);
  }

  static OffsetDateTime parseOffsetDateTime(String s) {
    final var value = offsetDateTime(s);
    return value != null ? value : OffsetDateTime.parse(s);
  }

  static ZonedDateTime parseZonedDateTime(String s) {
    final var value = offsetDateTime(s);
    return value != null
        ? value.toZonedDateTime()
        : ZonedDateTime.parse(s);
  }

  static Instant parseInstant(String s) {
    final var len = s.length();
    if (len >= 20 && s.charAt(10) == 'T' && s.charAt(len - 1) == 'Z') {
      try {
        final var date = date(s);
        final var nanoOfDay = nanoOfDay(s, 11, len - 1, true);
        if (date != null && nanoOfDay >= 0) {
          final var epochSecond =
              date.toEpochDay() * SECONDS_PER_DAY + nanoOfDay / NANOS_PER_SECOND;
          return Instant.ofEpochSecond(epochSecond, nanoOfDay % NANOS_PER_SECOND);
        }
      } catch (final DateTimeException e) {
        // fall back for the JDK exception
      }
    }
    return Instant.parse(s);
  }

  /** Return the same text as {@link Instant#toString()}. */
  static String format(Instant value) {
    final var epochSecond = value.getEpochSecond();
    if (epochSecond < SECONDS_0000_TO_1970 || epochSecond >= SECONDS_10000_TO_1970) {
      return value.toString();
    }
    final var epochDay = Math.floorDiv(epochSecond, SECONDS_PER_DAY);
    final var date = LocalDate.ofEpochDay(epochDay);
    final var year = date.getYear();
    final var secondOfDay = (int) (epochSecond - epochDay * SECONDS_PER_DAY);
    final var nano = value.getNano();
    final var buf = new byte[20 + fractionLength(nano)];
    writeDate(buf, year, date.getMonthValue(), date.getDayOfMonth());
    buf[10] = 'T';
    write2(buf, 11, secondOfDay / 3600);
    buf[13] = ':';
    write2(buf, 14, secondOfDay / 60 % 60);
    buf[16] = ':';
    write2(buf, 17, secondOfDay % 60);
    final var pos = writeFraction(buf, 19, nano);
    buf[pos] = 'Z';
    return new String(buf, StandardCharsets.ISO_8859_1);
  }

  /**
   * Return the date of {@code s[0, 10)} in {@code yyyy-MM-dd} form, or null if not in that form.
   *
   * @throws DateTimeException when the fields are out of range
   */
  private static LocalDate date(String s) {
    if (s.charAt(4) != '-' || s.charAt(7) != '-') {
      return null;
    }
    final var year1 = digits2(s, 0);
    final var year2 = digits2(s, 2);
    final var month = digits2(s, 5);
    final var day = digits2(s, 8);
    if ((year1 | year2 | month | day) < 0) {
      return null;
    }
    return LocalDate.of(year1 * 100 + year2, month, day);
  }

  /**
   * Return the nano of day for {@code s[off, end)} in the form {@code HH:mm}, {@code HH:mm:ss} or
   * {@code HH:mm:ss.f} with 1 to 9 fraction digits.
   *
   * @return the nano of day, or -1 if not in that form or out of range
   */
  private static long nanoOfDay(String s, int off, int end, boolean secondsRequired) {
    final var len = end - off;
    if (len < 5 || s.charAt(off + 2) != ':') {
      return -1;
    }
    final var hour = digits2(s, off);
    final var minute = digits2(s, off + 3);
    if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
      return -1;
    }
    var second = 0;
    var nano = 0;
    if (len == 5) {
      if (secondsRequired) {
        return -1;
      }
    } else {
      if (len < 8 || s.charAt(off + 5) != ':') {
        return -1;
      }
      second = digits2(s, off + 6);
      if (second < 0 || second > 59) {
        return -1;
      }
      if (len > 8) {
        final var fractionDigits = len - 9;
        if (s.charAt(off + 8) != '.' || fractionDigits < 1 || fractionDigits > 9) {
          return -1;
        }
        for (var i = off + 9; i < end; i++) {
          final var d = s.charAt(i) - '0';
          if (d < 0 || d > 9) {
            return -1;
          }
          nano = nano * 10 + d;
        }
        nano *= FRACTION_SCALE[fractionDigits];
      }
    }
    return (hour * 3600L + minute * 60L + second) * NANOS_PER_SECOND + nano;
  }

  /**
   * Parse {@code yyyy-MM-ddTHH:mm[:ss[.f]]} followed by {@code Z} or {@code +HH:MM}.
   *
   * @return the value, or null when the slower JDK parser is needed
   */
  private static OffsetDateTime offsetDateTime(String s) {
    final var len = s.length();
    if (len < 17 || s.charAt(10) != 'T') {
      return null;
    }
    final int timeEnd;
    final var last = s.charAt(len - 1);
    if (last == 'Z') {
      timeEnd = len - 1;
    } else {
      timeEnd = len - 6;
      if (timeEnd < 16 || s.charAt(len - 3) != ':') {
        return null;
      }
    }
    try {
      final var date = date(s);
      final var nanoOfDay = nanoOfDay(s, 11, timeEnd, false);
      if (date == null || nanoOfDay < 0) {
        return null;
      }
      final ZoneOffset offset;
      if (last == 'Z') {
        offset = ZoneOffset.UTC;
      } else {
        final var sign = s.charAt(timeEnd);
        final var hours = digits2(s, timeEnd + 1);
        final var minutes = digits2(s, timeEnd + 4);
        if (sign != '+' && sign != '-' || (hours | minutes) < 0) {
          return null;
        }
        offset =
            sign == '+'
                ? ZoneOffset.ofHoursMinutes(hours, minutes)
                : ZoneOffset.ofHoursMinutes(-hours, -minutes);
      }
      return OffsetDateTime.of(LocalDateTime.of(date, LocalTime.ofNanoOfDay(nanoOfDay)), offset);
    } catch (final DateTimeException e) {
      return null;
    }
  }

  /** Return the value of two ASCII digits at {@code off}, or -1 if either is not a digit. */
  private static int digits2(String s, int off) {
    final var a = s.charAt(off) - '0';
    final var b = s.charAt(off + 1) - '0';
    if (a < 0 || a > 9 || b < 0 || b > 9) {
      return -1;
    }
    return a * 10 + b;
  }

  private static int fractionLength(int nano) {
    if (nano == 0) {
      return 0;
    }
    if (nano % 1_000_000 == 0) {
      return 4;
    }
    if (nano % 1_000 == 0) {
      return 7;
    }
    return 10;
  }

  private static int writeFraction(byte[] buf, int pos, int nano) {
    if (nano == 0) {
      return pos;
    }
    buf[pos] = '.';
    pos++;
    final int digits;
    int value;
    if (nano % 1_000_000 == 0) {
      digits = 3;
      value = nano / 1_000_000;
    } else if (nano % 1_000 == 0) {
      digits = 6;
      value = nano / 1_000;
    } else {
      digits = 9;
      value = nano;
    }
    for (var i = pos + digits - 1; i >= pos; i--) {
      buf[i] = (byte) ('0' + value % 10);
      value /= 10;
    }
    return pos + digits;
  }

  private static void writeDate(byte[] buf, int year, int month, int day) {
    write2(buf, 0, year / 100);
    write2(buf, 2, year % 100);
    buf[4] = '-';
    write2(buf, 5, month);
    buf[7] = '-';
    write2(buf, 8, day);
  }

  private static void write2(byte[] buf, int pos, int value) {
    buf[pos] = (byte) ('0' + value / 10);
    buf[pos + 1] = (byte) ('0' + value % 10);
  }
}
