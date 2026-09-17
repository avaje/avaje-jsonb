package io.avaje.jsonb.generator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ComponentMetaDataTest {

  @Test
  void deduplicatesNormalizedNamesAndRetainsFirstValue() {
    ComponentMetaData metaData = new ComponentMetaData();

    metaData.addFactory("example.Outer.InnerJsonAdapter");
    metaData.addFactory("example.Outer$InnerJsonAdapter");

    assertThat(metaData.allFactories()).containsExactly("example.Outer.InnerJsonAdapter");
  }

  @Test
  void retainsInsertionOrder() {
    ComponentMetaData metaData = new ComponentMetaData();

    metaData.addFactory("example.FirstJsonAdapter");
    metaData.addFactory("example.SecondJsonAdapter");

    assertThat(metaData.allFactories())
      .containsExactly("example.FirstJsonAdapter", "example.SecondJsonAdapter");
  }

  @Test
  void keepsRegistrationCategoriesIndependent() {
    ComponentMetaData metaData = new ComponentMetaData();
    String type = "example.CustomJsonAdapter";

    metaData.addFactory(type);
    metaData.addWithType(type);

    assertThat(metaData.allFactories()).containsExactly(type);
    assertThat(metaData.withTypes()).containsExactly(type);
  }

}
