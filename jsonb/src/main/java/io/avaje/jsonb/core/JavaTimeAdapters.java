package io.avaje.jsonb.core;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;

import io.avaje.json.AsciiCodec;
import io.avaje.json.JsonAdapter;
import io.avaje.jsonb.AdapterFactory;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.Jsonb;

import java.time.*;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Adds support for java time types.
 */
final class JavaTimeAdapters {

  static final AdapterFactory FACTORY = (type, jsonb) -> {
    if (type == Instant.class) return JavaTimeAdapters.INSTANT_ADAPTER.nullSafe();
    if (type == LocalDate.class) return JavaTimeAdapters.LOCAL_DATE_ADAPTER.nullSafe();
    if (type == LocalDateTime.class) return JavaTimeAdapters.LOCAL_DATE_TIME_ADAPTER.nullSafe();
    if (type == LocalTime.class) return JavaTimeAdapters.LOCAL_TIME_ADAPTER.nullSafe();
    if (type == MonthDay.class) return JavaTimeAdapters.MONTH_DAY_ADAPTER.nullSafe();
    if (type == OffsetDateTime.class) return JavaTimeAdapters.OFFSET_DATE_TIME_ADAPTER.nullSafe();
    if (type == OffsetTime.class) return JavaTimeAdapters.OFFSET_TIME_ADAPTER.nullSafe();
    if (type == Period.class) return JavaTimeAdapters.PERIOD_ADAPTER.nullSafe();
    if (type == Year.class) return JavaTimeAdapters.YEAR_ADAPTER.nullSafe();
    if (type == YearMonth.class) return JavaTimeAdapters.YEAR_MONTH_ADAPTER.nullSafe();
    if (type == ZonedDateTime.class) return JavaTimeAdapters.ZONED_DATE_TIME_ADAPTER.nullSafe();
    if (type == ZoneId.class) return JavaTimeAdapters.ZONE_ID_ADAPTER.nullSafe();
    if (type == ZoneOffset.class) return JavaTimeAdapters.ZONE_OFFSET_ADAPTER.nullSafe();
    if (type == Date.class) return JavaTimeAdapters.UTIL_DATE.nullSafe();
    if (type == Duration.class) return JavaTimeAdapters.DURATION_ADAPTER.nullSafe();

    return null;
  };

  static final class CalendarFactory implements AdapterFactory {

    private final boolean calendarAsString;

    CalendarFactory(boolean calendarAsString) {
      this.calendarAsString = calendarAsString;
    }

    @Override
    public JsonAdapter<?> create(Type type, Jsonb jsonb) {
      if (type == Calendar.class) {
        return calendarAsString
          ? JavaTimeAdapters.CALENDAR_ZONED.nullSafe()
          : JavaTimeAdapters.CALENDAR_EPOCH_MILLIS.nullSafe() ;
      }
      return null;
    }
  }

  /**
   * Using ISO-8601
   */
  private static final JsonAdapter<Date> UTIL_DATE = new JsonAdapter<>() {
    @Override
    public Date fromJson(JsonReader reader) {
      return Date.from(reader.readString(INSTANT_ADAPTER));
    }

    @Override
    public void toJson(JsonWriter writer, Date value) {
      writer.value(INSTANT_ADAPTER, value.toInstant());
    }

    @Override
    public String toString() {
      return "JsonAdapter(Date)";
    }
  };

  private static final JsonAdapter<Duration> DURATION_ADAPTER = new JsonAdapter<>() {
    @Override
    public Duration fromJson(JsonReader reader) {
      return Duration.parse(reader.readString());
    }

    @Override
    public void toJson(JsonWriter writer, Duration value) {
      writer.value(value.toString());
    }

    @Override
    public String toString() {
      return "JsonAdapter(Duration)";
    }
  };

  static final InstantAdapter INSTANT_ADAPTER = new InstantAdapter();

  static final class InstantAdapter implements JsonAdapter<Instant>, AsciiCodec<Instant> {

    @Override
    public Instant fromJson(JsonReader reader) {
      return reader.readString(this);
    }

    @Override
    public void toJson(JsonWriter writer, Instant value) {
      writer.value(this, value);
    }

    @Override
    public Instant decode(String text) {
      final var value = IsoTime.instant(IsoTime.bytes(text), 0, text.length());
      return value != null ? value : Instant.parse(text);
    }

    @Override
    public Instant decode(byte[] buffer, int start, int end) {
      final var value = IsoTime.instant(buffer, start, end);
      return value != null ? value : Instant.parse(IsoTime.text(buffer, start, end));
    }

    @Override
    public int maxLength(Instant value) {
      return IsoTime.fastInstant(value) ? 30 : -1;
    }

    @Override
    public int encode(Instant value, byte[] buffer, int pos) {
      return IsoTime.writeInstant(buffer, pos, value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(Instant)";
    }
  }

  static final OffsetDateTimeAdapter OFFSET_DATE_TIME_ADAPTER = new OffsetDateTimeAdapter();

  static final class OffsetDateTimeAdapter implements JsonAdapter<OffsetDateTime>, AsciiCodec<OffsetDateTime> {

    @Override
    public OffsetDateTime fromJson(JsonReader reader) {
      return reader.readString(this);
    }

    @Override
    public void toJson(JsonWriter writer, OffsetDateTime value) {
      writer.value(this, value);
    }

    @Override
    public OffsetDateTime decode(String text) {
      final var value = IsoTime.offsetDateTime(IsoTime.bytes(text), 0, text.length());
      return value != null ? value : OffsetDateTime.parse(text);
    }

    @Override
    public OffsetDateTime decode(byte[] buffer, int start, int end) {
      final var value = IsoTime.offsetDateTime(buffer, start, end);
      return value != null ? value : OffsetDateTime.parse(IsoTime.text(buffer, start, end));
    }

    @Override
    public int maxLength(OffsetDateTime value) {
      return IsoTime.fastYear(value.getYear()) ? 29 + 9 : -1;
    }

    @Override
    public int encode(OffsetDateTime value, byte[] buffer, int pos) {
      return IsoTime.writeOffsetDateTime(buffer, pos, value.toLocalDateTime(), value.getOffset());
    }

    @Override
    public String toString() {
      return "JsonAdapter(OffsetDateTime)";
    }
  }

  private static final JsonAdapter<OffsetTime> OFFSET_TIME_ADAPTER = new JsonAdapter<>() {
    @Override
    public OffsetTime fromJson(JsonReader reader) {
      return OffsetTime.parse(reader.readString());
    }

    @Override
    public void toJson(JsonWriter writer, OffsetTime value) {
      writer.value(value.toString());
    }

    @Override
    public String toString() {
      return "JsonAdapter(OffsetTime)";
    }
  };

  static final ZonedDateTimeAdapter ZONED_DATE_TIME_ADAPTER = new ZonedDateTimeAdapter();

  static final class ZonedDateTimeAdapter implements JsonAdapter<ZonedDateTime>, AsciiCodec<ZonedDateTime> {

    @Override
    public ZonedDateTime fromJson(JsonReader reader) {
      return reader.readString(this);
    }

    @Override
    public void toJson(JsonWriter writer, ZonedDateTime value) {
      writer.value(this, value);
    }

    @Override
    public ZonedDateTime decode(String text) {
      final var value = IsoTime.offsetDateTime(IsoTime.bytes(text), 0, text.length());
      return value != null ? value.toZonedDateTime() : ZonedDateTime.parse(text);
    }

    @Override
    public ZonedDateTime decode(byte[] buffer, int start, int end) {
      final var value = IsoTime.offsetDateTime(buffer, start, end);
      return value != null ? value.toZonedDateTime() : ZonedDateTime.parse(IsoTime.text(buffer, start, end));
    }

    @Override
    public int maxLength(ZonedDateTime value) {
      if (!IsoTime.fastYear(value.getYear())) {
        return -1;
      }
      final var zone = value.getZone();
      return zone instanceof ZoneOffset ? 29 + 9 : 29 + 9 + 2 + zone.getId().length();
    }

    @Override
    public int encode(ZonedDateTime value, byte[] buffer, int pos) {
      final var offset = value.getOffset();
      pos = IsoTime.writeOffsetDateTime(buffer, pos, value.toLocalDateTime(), offset);
      final var zone = value.getZone();
      if (zone != offset) {
        buffer[pos++] = '[';
        pos = IsoTime.writeAscii(buffer, pos, zone.getId());
        buffer[pos++] = ']';
      }
      return pos;
    }

    @Override
    public String toString() {
      return "JsonAdapter(ZonedDateTime)";
    }
  }

  private static final JsonAdapter<Calendar> CALENDAR_ZONED = new JsonAdapter<>() {
    @Override
    public Calendar fromJson(JsonReader reader) {
      final ZonedDateTime zdt = reader.readString(ZONED_DATE_TIME_ADAPTER);
      return GregorianCalendar.from(zdt);
    }

    @Override
    public void toJson(JsonWriter writer, Calendar value) {
      TimeZone timeZone = value.getTimeZone();
      ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(value.toInstant(), timeZone.toZoneId());
      writer.value(ZONED_DATE_TIME_ADAPTER, zonedDateTime);
    }

    @Override
    public String toString() {
      return "JsonAdapter(Calendar)";
    }
  };

  private static final JsonAdapter<Calendar> CALENDAR_EPOCH_MILLIS = new JsonAdapter<>() {
    private final TimeZone UTC = TimeZone.getTimeZone(ZoneId.of("UTC"));

    @Override
    public Calendar fromJson(JsonReader reader) {
      long epochMillis = reader.readLong();
      Calendar instance = Calendar.getInstance();
      instance.setTimeInMillis(epochMillis);
      instance.setTimeZone(UTC);
      return instance;
    }

    @Override
    public void toJson(JsonWriter writer, Calendar value) {
      writer.value(value.getTimeInMillis());
    }

    @Override
    public String toString() {
      return "JsonAdapter(Calendar)";
    }
  };

  private static final JsonAdapter<ZoneOffset> ZONE_OFFSET_ADAPTER = new JsonAdapter<>() {
    @Override
    public ZoneOffset fromJson(JsonReader reader) {
      return ZoneOffset.of(reader.readString());
    }

    @Override
    public void toJson(JsonWriter writer, ZoneOffset value) {
      writer.value(value.toString());
    }

    @Override
    public String toString() {
      return "JsonAdapter(ZoneOffset)";
    }
  };

  private static final JsonAdapter<ZoneId> ZONE_ID_ADAPTER = new JsonAdapter<>() {
    @Override
    public ZoneId fromJson(JsonReader reader) {
      return ZoneId.of(reader.readString());
    }

    @Override
    public void toJson(JsonWriter writer, ZoneId value) {
      writer.value(value.toString());
    }

    @Override
    public String toString() {
      return "JsonAdapter(ZoneId)";
    }
  };

  static final LocalDateAdapter LOCAL_DATE_ADAPTER = new LocalDateAdapter();

  static final class LocalDateAdapter implements JsonAdapter<LocalDate>, AsciiCodec<LocalDate> {

    @Override
    public LocalDate fromJson(JsonReader reader) {
      return reader.readString(this);
    }

    @Override
    public void toJson(JsonWriter writer, LocalDate value) {
      writer.value(this, value);
    }

    @Override
    public LocalDate decode(String text) {
      final var value = IsoTime.localDate(IsoTime.bytes(text), 0, text.length());
      return value != null ? value : LocalDate.parse(text);
    }

    @Override
    public LocalDate decode(byte[] buffer, int start, int end) {
      final var value = IsoTime.localDate(buffer, start, end);
      return value != null ? value : LocalDate.parse(IsoTime.text(buffer, start, end));
    }

    @Override
    public int maxLength(LocalDate value) {
      return IsoTime.fastYear(value.getYear()) ? 10 : -1;
    }

    @Override
    public int encode(LocalDate value, byte[] buffer, int pos) {
      return IsoTime.writeDate(buffer, pos, value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(LocalDate)";
    }
  }

  static final LocalDateTimeAdapter LOCAL_DATE_TIME_ADAPTER = new LocalDateTimeAdapter();

  static final class LocalDateTimeAdapter implements JsonAdapter<LocalDateTime>, AsciiCodec<LocalDateTime> {

    @Override
    public LocalDateTime fromJson(JsonReader reader) {
      return reader.readString(this);
    }

    @Override
    public void toJson(JsonWriter writer, LocalDateTime value) {
      writer.value(this, value);
    }

    @Override
    public LocalDateTime decode(String text) {
      final var value = IsoTime.localDateTime(IsoTime.bytes(text), 0, text.length());
      return value != null ? value : LocalDateTime.parse(text);
    }

    @Override
    public LocalDateTime decode(byte[] buffer, int start, int end) {
      final var value = IsoTime.localDateTime(buffer, start, end);
      return value != null ? value : LocalDateTime.parse(IsoTime.text(buffer, start, end));
    }

    @Override
    public int maxLength(LocalDateTime value) {
      return IsoTime.fastYear(value.getYear()) ? 29 : -1;
    }

    @Override
    public int encode(LocalDateTime value, byte[] buffer, int pos) {
      return IsoTime.writeDateTime(buffer, pos, value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(LocalDateTime)";
    }
  }

  static final LocalTimeAdapter LOCAL_TIME_ADAPTER = new LocalTimeAdapter();

  static final class LocalTimeAdapter implements JsonAdapter<LocalTime>, AsciiCodec<LocalTime> {

    @Override
    public LocalTime fromJson(JsonReader reader) {
      return reader.readString(this);
    }

    @Override
    public void toJson(JsonWriter writer, LocalTime value) {
      writer.value(this, value);
    }

    @Override
    public LocalTime decode(String text) {
      final var value = IsoTime.localTime(IsoTime.bytes(text), 0, text.length());
      return value != null ? value : LocalTime.parse(text);
    }

    @Override
    public LocalTime decode(byte[] buffer, int start, int end) {
      final var value = IsoTime.localTime(buffer, start, end);
      return value != null ? value : LocalTime.parse(IsoTime.text(buffer, start, end));
    }

    @Override
    public int maxLength(LocalTime value) {
      return 18;
    }

    @Override
    public int encode(LocalTime value, byte[] buffer, int pos) {
      return IsoTime.writeTime(buffer, pos, value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(LocalTime)";
    }
  }

  private static final JsonAdapter<MonthDay> MONTH_DAY_ADAPTER = new JsonAdapter<>() {
    @Override
    public MonthDay fromJson(JsonReader reader) {
      return MonthDay.parse(reader.readString());
    }

    @Override
    public void toJson(JsonWriter writer, MonthDay value) {
      writer.value(value.toString());
    }

    @Override
    public String toString() {
      return "JsonAdapter(MonthDay)";
    }
  };


  private static final JsonAdapter<Period> PERIOD_ADAPTER = new JsonAdapter<>() {
    @Override
    public Period fromJson(JsonReader reader) {
      return Period.parse(reader.readString());
    }

    @Override
    public void toJson(JsonWriter writer, Period value) {
      writer.value(value.toString());
    }

    @Override
    public String toString() {
      return "JsonAdapter(Period)";
    }
  };

  private static final JsonAdapter<Year> YEAR_ADAPTER = new JsonAdapter<>() {
    @Override
    public Year fromJson(JsonReader reader) {
      return Year.of(reader.readInt());
    }

    @Override
    public void toJson(JsonWriter writer, Year value) {
      writer.value(value.getValue());
    }

    @Override
    public String toString() {
      return "JsonAdapter(Year)";
    }
  };

  private static final JsonAdapter<YearMonth> YEAR_MONTH_ADAPTER = new JsonAdapter<>() {
    @Override
    public YearMonth fromJson(JsonReader reader) {
      return YearMonth.parse(reader.readString());
    }

    @Override
    public void toJson(JsonWriter writer, YearMonth value) {
      writer.value(value.toString());
    }

    @Override
    public String toString() {
      return "JsonAdapter(YearMonth)";
    }
  };
}
