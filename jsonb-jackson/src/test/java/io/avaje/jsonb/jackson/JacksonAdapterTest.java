package io.avaje.jsonb.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.OutputStream;

import org.example.Address;
import org.example.MyComponent;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;

import io.avaje.jsonb.Jsonb;

class JacksonAdapterTest {

  public static final JsonFactory JSON_FACTORY = new JsonFactory()
    .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
    .configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true)
    .configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true) // required to read line breaks
    .configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true);

  public static final JsonFactory HUMAN_READABLE_JSON_FACTORY = JSON_FACTORY.copy()
    .setCharacterEscapes(new HumanReadableCharacterEscapes());

  @Test
  void isJackson() {
    var jsonb = Jsonb.instance();
    assertThat(jsonb.reader("")).isInstanceOf(JacksonReader.class);
    assertThat(jsonb.writer(OutputStream.nullOutputStream())).isInstanceOf(JacksonWriter.class);
  }

  @Test
  void custom_JsonFactory() {

    JacksonAdapter jacksonAdapter = JacksonAdapter.builder()
      .jsonFactory(HUMAN_READABLE_JSON_FACTORY)
      .build();

    Jsonb jsonb = Jsonb.builder()
      .adapter(jacksonAdapter)
      .add(new MyComponent())
      .build();

    String jc = "\n// a comment \n ## yaml-comment \n{\"street\":\"my-street\nwith-new-lines\tand-tabs\"}";

    Address address = jsonb.type(Address.class).fromJson(jc);
    assertThat(address.street()).isEqualTo("my-street\n" +
      "with-new-lines\tand-tabs");

    Address withHumanNewLinesEtc = new Address();
    withHumanNewLinesEtc.street("my-street\nwith-new-lines\tand-tabs");

    String asJson = jsonb.toJson(withHumanNewLinesEtc);
    assertThat(asJson).isEqualTo("{\"street\":\"my-street\n" +
      "with-new-lines\tand-tabs\"}");
  }

  private static class HumanReadableCharacterEscapes extends CharacterEscapes {
    private static final long serialVersionUID = 1L;

    private static final int[] asciiEscapes = CharacterEscapes.standardAsciiEscapesForJSON();
    static {
      asciiEscapes['\t'] = CharacterEscapes.ESCAPE_NONE;
      asciiEscapes['\r'] = CharacterEscapes.ESCAPE_NONE;
      asciiEscapes['\n'] = CharacterEscapes.ESCAPE_NONE;
    }

    @Override
    public SerializableString getEscapeSequence(final int ch) {
      return null;
    }

    @Override
    public int[] getEscapeCodesForAscii() {
      return asciiEscapes;
    }
  }

}
