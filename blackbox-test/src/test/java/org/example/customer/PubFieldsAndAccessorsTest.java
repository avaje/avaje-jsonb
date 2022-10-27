package org.example.customer;

import io.avaje.jsonb.JsonView;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PubFieldsAndAccessorsTest {

  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void toJsonFromJson() {
    var bean = new PubFieldsAndAccessors();
    bean.name = "foo";
    bean.score = 42;
    bean.setActive(true);
    bean.setRegistered(true);

    String asJson = jsonb.toJson(bean);
    assertThat(asJson).isEqualTo("{\"name\":\"foo\",\"score\":42,\"isActive\":true,\"isRegistered\":true}");

    PubFieldsAndAccessors fromJson = jsonb.type(PubFieldsAndAccessors.class).fromJson(asJson);
    assertThat(fromJson.name).isEqualTo("foo");
    assertThat(fromJson.score).isEqualTo(42);
    assertThat(fromJson.isActive()).isEqualTo(true);
    assertThat(fromJson.getRegistered()).isEqualTo(true);
  }

  @Test
  void objectBooleanAsNull() {
    var bean = new PubFieldsAndAccessors();
    bean.name = "foo";

    String asJson = jsonb.toJson(bean);
    assertThat(asJson).isEqualTo("{\"name\":\"foo\",\"score\":0,\"isActive\":false}");

    PubFieldsAndAccessors fromJson = jsonb.type(PubFieldsAndAccessors.class).fromJson(asJson);
    assertThat(fromJson.name).isEqualTo("foo");
    assertThat(fromJson.score).isEqualTo(0);
    assertThat(fromJson.isActive()).isEqualTo(false);
    assertThat(fromJson.getRegistered()).isNull();


    PubFieldsAndAccessors fromJsonWithNull = jsonb.type(PubFieldsAndAccessors.class).fromJson("{\"name\":\"bar\",\"isRegistered\":null}");
    assertThat(fromJsonWithNull.name).isEqualTo("bar");
    assertThat(fromJsonWithNull.score).isEqualTo(0);
    assertThat(fromJsonWithNull.isActive()).isEqualTo(false);
    assertThat(fromJsonWithNull.getRegistered()).isNull();
  }

  @Test
  void viewUsesPropertyName() {
    var bean = new PubFieldsAndAccessors();
    bean.name = "foo";
    bean.setProblem(true);

    String asJson = jsonb.toJson(bean);
    assertThat(asJson).isEqualTo("{\"name\":\"foo\",\"score\":0,\"isActive\":false,\"problem\":true}");

    JsonView<PubFieldsAndAccessors> view = jsonb.type(PubFieldsAndAccessors.class).view("name,problem");
    String viewJson = view.toJson(bean);
    assertThat(viewJson).isEqualTo("{\"name\":\"foo\",\"problem\":true}");
  }
}
