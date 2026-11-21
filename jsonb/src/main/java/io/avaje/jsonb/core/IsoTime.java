package io.avaje.jsonb.core;

import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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

  static byte[] bytes(String s) {
    return s.getBytes(StandardCharsets.ISO_8859_1);
  }

  static String text(byte[] buf, int start, int end) {
    return new String(buf, start, end - start, StandardCharsets.ISO_8859_1);
  }

  static LocalDate localDate(byte[] b, int start, int end) {
    if (end - start == 10) {
      try {
        return date(b, start);
      } catch (final DateTimeException e) {
        // fall back for the JDK exception
      }
    }
    return null;
  }

  static LocalTime localTime(byte[] b, int start, int end) {
    final var nanoOfDay = nanoOfDay(b, start, end, false);
    return nanoOfDay >= 0 ? LocalTime.ofNanoOfDay(nanoOfDay) : null;
  }

  static LocalDateTime localDateTime(byte[] b, int start, int end) {
    if (end - start >= 16 && b[start + 10] == 'T') {
      try {
        final var date = date(b, start);
        final var nanoOfDay = nanoOfDay(b, start + 11, end, false);
        if (date != null && nanoOfDay >= 0) {
          return LocalDateTime.of(date, LocalTime.ofNanoOfDay(nanoOfDay));
        }
      } catch (final DateTimeException e) {
        // fall back for the JDK exception
      }
    }
    return null;
  }

  static Instant instant(byte[] b, int start, int end) {
    if (end - start >= 20 && b[start + 10] == 'T' && b[end - 1] == 'Z') {
      try {
        final var date = date(b, start);
        final var nanoOfDay = nanoOfDay(b, start + 11, end - 1, true);
        if (date != null && nanoOfDay >= 0) {
          final var epochSecond =
              date.toEpochDay() * SECONDS_PER_DAY + nanoOfDay / NANOS_PER_SECOND;
          return Instant.ofEpochSecond(epochSecond, nanoOfDay % NANOS_PER_SECOND);
        }
      } catch (final DateTimeException e) {
        // fall back for the JDK exception
      }
    }
    return null;
  }

  static boolean fastInstant(Instant value) {
    final var epochSecond = value.getEpochSecond();
    return epochSecond >= SECONDS_0000_TO_1970 && epochSecond < SECONDS_10000_TO_1970;
  }

  static boolean fastYear(int year) {
    return year >= 0 && year <= 9999;
  }

  static int writeInstant(byte[] buf, int pos, Instant value) {
    final var epochSecond = value.getEpochSecond();
    final var epochDay = Math.floorDiv(epochSecond, SECONDS_PER_DAY);
    final var date = LocalDate.ofEpochDay(epochDay);
    final var secondOfDay = (int) (epochSecond - epochDay * SECONDS_PER_DAY);
    pos = writeDate(buf, pos, date);
    buf[pos] = 'T';
    write2(buf, pos + 1, secondOfDay / 3600);
    buf[pos + 3] = ':';
    write2(buf, pos + 4, secondOfDay / 60 % 60);
    buf[pos + 6] = ':';
    write2(buf, pos + 7, secondOfDay % 60);
    pos = writeFraction(buf, pos + 9, value.getNano());
    buf[pos] = 'Z';
    return pos + 1;
  }

  static int writeDateTime(byte[] buf, int pos, LocalDateTime value) {
    pos = writeDate(buf, pos, value.toLocalDate());
    buf[pos] = 'T';
    return writeTime(buf, pos + 1, value.toLocalTime());
  }

  static int writeOffsetDateTime(byte[] buf, int pos, LocalDateTime dateTime, ZoneOffset offset) {
    pos = writeDateTime(buf, pos, dateTime);
    return writeAscii(buf, pos, offset.getId());
  }

  static int writeTime(byte[] buf, int pos, LocalTime value) {
    write2(buf, pos, value.getHour());
    buf[pos + 2] = ':';
    write2(buf, pos + 3, value.getMinute());
    final var second = value.getSecond();
    final var nano = value.getNano();
    if (second == 0 && nano == 0) {
      return pos + 5;
    }
    buf[pos + 5] = ':';
    write2(buf, pos + 6, second);
    return writeFraction(buf, pos + 8, nano);
  }

  static int writeAscii(byte[] buf, int pos, String text) {
    final var len = text.length();
    for (var i = 0; i < len; i++) {
      buf[pos + i] = (byte) text.charAt(i);
    }
    return pos + len;
  }

  /**
   * Return the date of {@code s[0, 10)} in {@code yyyy-MM-dd} form, or null if not in that form.
   *
   * @throws DateTimeException when the fields are out of range
   */
  private static LocalDate date(byte[] b, int off) {
    if (b[off + 4] != '-' || b[off + 7] != '-') {
      return null;
    }
    final var year1 = digits2(b, off);
    final var year2 = digits2(b, off + 2);
    final var month = digits2(b, off + 5);
    final var day = digits2(b, off + 8);
    if ((year1 | year2 | month | day) < 0) {
      return null;
    }
    return LocalDate.of(year1 * 100 + year2, month, day);
  }

  /**
   * Return the nano of day for {@code b[off, end)} in the form {@code HH:mm}, {@code HH:mm:ss} or
   * {@code HH:mm:ss.f} with 1 to 9 fraction digits.
   *
   * @return the nano of day, or -1 if not in that form or out of range
   */
  private static long nanoOfDay(byte[] b, int off, int end, boolean secondsRequired) {
    final var len = end - off;
    if (len < 5 || b[off + 2] != ':') {
      return -1;
    }
    final var hour = digits2(b, off);
    final var minute = digits2(b, off + 3);
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
      if (len < 8 || b[off + 5] != ':') {
        return -1;
      }
      second = digits2(b, off + 6);
      if (second < 0 || second > 59) {
        return -1;
      }
      if (len > 8) {
        final var fractionDigits = len - 9;
        if (b[off + 8] != '.' || fractionDigits < 1 || fractionDigits > 9) {
          return -1;
        }
        for (var i = off + 9; i < end; i++) {
          final var d = b[i] - '0';
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
   * Parse {@code b[start, end)} in the form {@code yyyy-MM-ddTHH:mm[:ss[.f]]} followed by {@code Z}
   * or {@code +HH:MM}.
   *
   * @return the value, or null when the slower JDK parser is needed
   */
  static OffsetDateTime offsetDateTime(byte[] b, int start, int end) {
    if (end - start < 17 || b[start + 10] != 'T') {
      return null;
    }
    final int timeEnd;
    final var last = b[end - 1];
    if (last == 'Z') {
      timeEnd = end - 1;
    } else {
      timeEnd = end - 6;
      if (timeEnd - start < 16 || b[end - 3] != ':') {
        return null;
      }
    }
    try {
      final var date = date(b, start);
      final var nanoOfDay = nanoOfDay(b, start + 11, timeEnd, false);
      if (date == null || nanoOfDay < 0) {
        return null;
      }
      final ZoneOffset offset;
      if (last == 'Z') {
        offset = ZoneOffset.UTC;
      } else {
        final var sign = b[timeEnd];
        final var hours = digits2(b, timeEnd + 1);
        final var minutes = digits2(b, timeEnd + 4);
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
  private static int digits2(byte[] buf, int off) {
    final var a = buf[off] - '0';
    final var b = buf[off + 1] - '0';
    if (a < 0 || a > 9 || b < 0 || b > 9) {
      return -1;
    }
    return a * 10 + b;
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

  static int writeDate(byte[] buf, int pos, LocalDate date) {
    final var year = date.getYear();
    write2(buf, pos, year / 100);
    write2(buf, pos + 2, year % 100);
    buf[pos + 4] = '-';
    write2(buf, pos + 5, date.getMonthValue());
    buf[pos + 7] = '-';
    write2(buf, pos + 8, date.getDayOfMonth());
    return pos + 10;
  }

  private static void write2(byte[] buf, int pos, int value) {
    buf[pos] = (byte) ('0' + value / 10);
    buf[pos + 1] = (byte) ('0' + value % 10);
  }
}
