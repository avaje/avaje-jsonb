package org.example.customer.datetime;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

class CalendarTest {

  @Test
  void calendarAsEpochMillis() {
    Jsonb jsonb = Jsonb.builder().calendarAsString(false).build();
    JsonType<MyCalData> type = jsonb.type(MyCalData.class);

    var zdt = ZonedDateTime.of(2001, 3, 23, 0, 1, 2, 0, ZoneId.of("Pacific/Auckland"));
    var calendar = GregorianCalendar.from(zdt);
    var data = new MyCalData(calendar);
    var asJson = type.toJson(data);
    var fromJson = type.fromJson(asJson);

    assertThat(fromJson.cal().toInstant()).isEqualTo(data.cal().toInstant());
    // deserialized as UTC
    assertThat(fromJson.cal().getTimeZone()).isEqualTo(TimeZone.getTimeZone(ZoneId.of("UTC")));
  }

  @Test
  void calendarAsString() {
    Jsonb jsonb = Jsonb.builder().calendarAsString(true).build();
    JsonType<MyCalData> type = jsonb.type(MyCalData.class);

    var zdt = ZonedDateTime.of(2001, 3, 23, 0, 1, 2, 0, ZoneId.of("Pacific/Auckland"));
    // note calendar deserialized using ISO8601 with
    // setFirstDayOfWeek(MONDAY) and setMinimalDaysInFirstWeek(4);
    var calendar = GregorianCalendar.from(zdt);
    var data = new MyCalData(calendar);
    var asJson = type.toJson(data);
    var fromJson = type.fromJson(asJson);

    assertThat(fromJson.cal().toInstant()).isEqualTo(data.cal().toInstant());
    assertThat(fromJson.cal().getTimeZone()).isEqualTo(data.cal().getTimeZone());
    assertThat(fromJson.cal().getFirstDayOfWeek()).isEqualTo(data.cal().getFirstDayOfWeek());
    assertThat(fromJson.cal().getMinimalDaysInFirstWeek()).isEqualTo(data.cal().getMinimalDaysInFirstWeek());
    assertThat(fromJson).isEqualTo(data);
  }
}
