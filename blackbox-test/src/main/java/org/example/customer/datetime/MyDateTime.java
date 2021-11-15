package org.example.customer.datetime;

import io.avaje.jsonb.Json;

import java.time.*;
import java.util.UUID;

@Json
public record MyDateTime(
  UUID uuid,
  Instant instant,
  LocalDate localDate,
  LocalDateTime localDateTime,
  LocalTime localTime,
  MonthDay monthDay,
  OffsetDateTime offsetDateTime,
  OffsetTime offsetTime,
  Period period,
  Year year,
  YearMonth yearMonth,
  ZonedDateTime zonedDateTime,
  ZoneId zoneId,
  ZoneOffset zoneOffset,
  Month month,
  DayOfWeek dayOfWeek
) {
}
