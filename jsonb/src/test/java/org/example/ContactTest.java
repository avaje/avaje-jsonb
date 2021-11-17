package org.example;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ContactTest {


  final String jsonStart = "{\"id\":44,\"firstName\":\"rob\",\"lastName\":\"foo\"";

  @Test
  void toJson() throws IOException {

    Jsonb jsonb = Jsonb.newBuilder()
      .add(Contact.class, ContactJsonAdapter::new)
//      .add((type, annotations, json) -> {
//        if (type.equals(Contact.class)) {
//          return new ContactJsonAdapter(json);
//        }
//        return null;
//      })
      .build();


    Contact contact = new Contact(44L, "rob", "foo");


    JsonType<Contact> contactType = jsonb.type(Contact.class);
    String asJson = contactType.toJson(contact);
    assertThat(asJson).startsWith(jsonStart);

    Contact from2 = contactType.fromJson(asJson);
    assertThat(from2.id()).isEqualTo(contact.id());
    assertThat(from2.firstName()).isEqualTo(contact.firstName());
    assertThat(from2.lastName()).isEqualTo(contact.lastName());

  }

}
