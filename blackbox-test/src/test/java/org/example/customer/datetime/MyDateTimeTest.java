package org.example.customer.datetime;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.*;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MyDateTimeTest {

  Jsonb jsonb = Jsonb.newBuilder().build();
  JsonType<MyDateTime> jsonType = jsonb.type(MyDateTime.class);

  @Test
  void toJson_fromJson() throws IOException {

    MyDateTime myDateTime = new MyDateTime(UUID.randomUUID(), Instant.now(),
      LocalDate.now(),
      LocalDateTime.now(),
      LocalTime.now(),
      MonthDay.now(),
      OffsetDateTime.now(),
      OffsetTime.now(),
      Period.of(2, 4, 5),
      Year.now(),
      YearMonth.now(),
      ZonedDateTime.now(),
      ZoneId.systemDefault(),
      ZoneOffset.UTC,
      Month.JULY,
      DayOfWeek.FRIDAY);

    String asJson = jsonType.toJson(myDateTime);
    MyDateTime fromJson = jsonType.fromJson(asJson);

    assertThat(fromJson).isEqualTo(myDateTime);
  }
}
