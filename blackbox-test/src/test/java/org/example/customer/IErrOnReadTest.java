package org.example.customer;

import io.avaje.json.*;
import io.avaje.jsonb.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IErrOnReadTest {

  @Test
  void toJson_withError() {
    IErrOnRead bean = new IErrOnRead(UUID.randomUUID(), "first", "last");
    Jsonb jsonb = Jsonb.builder().build();
    JsonType<IErrOnRead> type = jsonb.type(IErrOnRead.class);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      type.toJson(bean, baos);
    } catch (JsonException expectedForTest) {
      assertThat(expectedForTest.getCause()).hasMessage("error reading lastName");
    }
    assertThat(baos.toByteArray()).describedAs("no flush to outputStream expected").hasSize(0);
  }
}
