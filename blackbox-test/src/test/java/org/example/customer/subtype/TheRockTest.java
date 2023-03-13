package org.example.customer.subtype;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

class TheRockTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<TheRock> rockJsonType = jsonb.type(TheRock.class);

  @Test
  void bocchi_toJson() {
    final Bocchi bocchi = new Bocchi(42, "gotoh", 95);

    final String asJson = rockJsonType.toJson(bocchi);
    assertThat(asJson)
        .isEqualTo("{\"@type\":\"Bocchi\",\"size\":42,\"name\":\"gotoh\",\"anxietyLvl\":95}");
  }

  @Test
  void dwayne_toJson() {
    final Dwayne dwayne = new Dwayne(42, "adam", 95);

    final String asJson = rockJsonType.toJson(dwayne);
    assertThat(asJson)
        .isEqualTo("{\"@type\":\"Dwayne\",\"size\":42,\"name\":\"adam\",\"cash\":95}");
  }

  @Test
  void bocchi_fromJson() {

    final TheRock theRock =
        rockJsonType.fromJson(
            "{\"@type\":\"Bocchi\",\"size\":42,\"name\":\"hitori\",\"anxietyLvl\":95}");

    assertThat(theRock).isInstanceOf(Bocchi.class);
    final Bocchi bocchi = (Bocchi) theRock;
    assertThat(bocchi.size()).isEqualTo(42);
    assertThat(bocchi.name()).isEqualTo("hitori");
    assertThat(bocchi.getAnxietyLvl()).isEqualTo(95);
  }

  @Test
  void dwayne_fromJson() {

    final TheRock theRock =
        rockJsonType.fromJson(
            "{\"@type\":\"Dwayne\",\"size\":42,\"name\":\"Johnson\",\"cash\":420}");

    assertThat(theRock).isInstanceOf(Dwayne.class);
    final Dwayne dwayne = (Dwayne) theRock;
    assertThat(dwayne.size()).isEqualTo(42);
    assertThat(dwayne.name()).isEqualTo("Johnson");
    assertThat(dwayne.getCash()).isEqualTo(420);
  }
}
