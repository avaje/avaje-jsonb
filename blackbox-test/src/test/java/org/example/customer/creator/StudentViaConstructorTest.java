package org.example.customer.creator;

import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class StudentViaConstructorTest {

  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void asJson() {
    StudentViaConstructor stu = new StudentViaConstructor("one", 1);

    String asJson = jsonb.toJson(stu);
    assertThat(asJson).isEqualTo("{\"name\":\"one\",\"rollNo\":1}");

    StudentViaConstructor fromJson = jsonb.type(StudentViaConstructor.class).fromJson(asJson);
    assertThat(fromJson.getName()).isEqualTo("one");
    assertThat(fromJson.getRollNo()).isEqualTo(3); // the length of name
  }
}
