package org.example.customer.creator;

import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StudentViaStaticMethodTest {

  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void asJson() {
    StudentViaStaticMethod stu = new StudentViaStaticMethod("one", 1);

    String asJson = jsonb.toJson(stu);
    assertThat(asJson).isEqualTo("{\"name\":\"one\",\"rollNo\":1}");

    StudentViaStaticMethod fromJson = jsonb.type(StudentViaStaticMethod.class).fromJson(asJson);
    assertThat(fromJson.getName()).isEqualTo("one");
    assertThat(fromJson.getRollNo()).isEqualTo(3); // the length of name
  }
}
