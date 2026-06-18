package io.avaje.json.stream.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for Recyclers.intSetting() — system property resolution and invalid value handling.
 *
 * <p>Environment variable resolution cannot be tested within the same JVM (env is read-only after
 * launch). The supported ENV var names are:
 * <ul>
 *   <li>JSONB_GENERATOR_BUFFER_SIZE</li>
 *   <li>JSONB_PARSER_BUFFER_SIZE</li>
 *   <li>JSONB_PARSER_CHAR_BUFFER_SIZE</li>
 *   <li>JSONB_PARSER_MAX_NUMBER_DIGITS</li>
 *   <li>JSONB_PARSER_MAX_STRING_BUFFER</li>
 * </ul>
 */
class RecyclerSettingsTest {

  private static final String TEST_PROP = "jsonb.test.intSetting";

  @AfterEach
  void cleanup() {
    System.clearProperty(TEST_PROP);
  }

  @Test
  void intSetting_returnsDefault_whenNeitherSet() {
    System.clearProperty(TEST_PROP);
    int result = Recyclers.intSetting(TEST_PROP, "JSONB_TEST_INT_SETTING_ABSENT", 42);
    assertThat(result).isEqualTo(42);
  }

  @Test
  void intSetting_returnsSystemProperty_whenSet() {
    System.setProperty(TEST_PROP, "9999");
    int result = Recyclers.intSetting(TEST_PROP, "JSONB_TEST_INT_SETTING_ABSENT", 42);
    assertThat(result).isEqualTo(9999);
  }

  @Test
  void intSetting_throwsNumberFormatException_onInvalidSystemProperty() {
    System.setProperty(TEST_PROP, "not-a-number");
    assertThatThrownBy(() -> Recyclers.intSetting(TEST_PROP, "JSONB_TEST_INT_SETTING_ABSENT", 42))
      .isInstanceOf(NumberFormatException.class);
  }
}
