package org.example.customer.repo;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkipValueTest {

  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void test_skipping() {
    JsonType<List<Repo>> list = jsonb.type(Repo.class).list();

    InputStream is = SkipValueTest.class.getResourceAsStream("/skip-value-test-data.json");
    List<Repo> repos = list.fromJson(is);

    assertThat(repos).hasSize(9);
    String asJson = list.toJson(repos);
    assertThat(asJson).isEqualTo("[{\"id\":1,\"name\":\"n0\"},{\"id\":2,\"name\":\"n1\"},{\"id\":3,\"name\":\"n2\"},{\"id\":4,\"name\":\"n3\"},{\"id\":5,\"name\":\"n4\"},{\"id\":6,\"name\":\"n5\"},{\"id\":7,\"name\":\"n6\"},{\"id\":8,\"name\":\"n7\"},{\"id\":9,\"name\":\"n8\"}]");
  }
}
