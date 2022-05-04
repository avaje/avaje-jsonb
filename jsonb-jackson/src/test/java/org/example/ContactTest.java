package org.example;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ContactTest {


  final String jsonStart = "{\"id\":44,\"firstName\":\"rob\",\"lastName\":\"foo\"";

  @Test
  void toJson_fromJson() {

    Jsonb jsonb = Jsonb.builder()
      // a silly example exercising add of Jsonb.Component - don't do this yourself
      .add(builder -> builder.add(Contact.class, ContactJsonAdapter::new))
      .build();

    Contact contact = new Contact(44L, "rob", "foo");

    JsonType<Contact> contactType = jsonb.type(Contact.class);
    String asJson = contactType.toJson(contact);
    assertThat(asJson).startsWith(jsonStart);

    Contact from2 = contactType.fromJson(asJson);
    assertThat(from2.id()).isEqualTo(contact.id());
    assertThat(from2.firstName()).isEqualTo(contact.firstName());
    assertThat(from2.lastName()).isEqualTo(contact.lastName());

    Contact from3 = contactType.fromJson(asJson.getBytes(StandardCharsets.UTF_8));
    assertThat(from3.id()).isEqualTo(contact.id());
    assertThat(from3.firstName()).isEqualTo(contact.firstName());
    assertThat(from3.lastName()).isEqualTo(contact.lastName());
  }

  @Test
  void toJson_asBytes() {
    Jsonb jsonb = Jsonb.builder()
      .add(Contact.class, ContactJsonAdapter::new)
      .build();

    Contact contact = new Contact(44L, "rob", "foo");

    JsonType<Contact> contactType = jsonb.type(Contact.class);
    String asJsonString = contactType.toJson(contact);
    assertThat(asJsonString).isEqualTo("{\"id\":44,\"firstName\":\"rob\",\"lastName\":\"foo\"}");

    byte[] asJsonBytes = contactType.toJsonBytes(contact);
    assertThat(asJsonBytes).isEqualTo(asJsonString.getBytes(StandardCharsets.UTF_8));
  }
}
