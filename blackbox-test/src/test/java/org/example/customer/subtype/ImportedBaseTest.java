package org.example.customer.subtype;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.example.customer.subtype.subpackage.Imported;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImportedBaseTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<ImportedBase> type = jsonb.type(ImportedBase.class);

  @Test
  void toJson_fromJson_subtypeInSubpackage() {
    Imported.Subtype subtype = new Imported.Subtype();
    subtype.setName("hello");

    String json = type.toJson(subtype);
    ImportedBase result = type.fromJson(json);

    assertThat(result).isInstanceOf(Imported.Subtype.class);
    assertThat(result.getName()).isEqualTo("hello");
  }
}
