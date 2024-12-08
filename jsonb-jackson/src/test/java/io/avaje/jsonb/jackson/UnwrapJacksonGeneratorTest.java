package io.avaje.jsonb.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonpCharacterEscapes;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.Jsonb;
import org.example.Address;
import org.example.MyComponent;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

class UnwrapJacksonGeneratorTest {

  @Test
  void writer_unwrap() {
    Jsonb jsonb = Jsonb.builder()
      .adapter(JacksonAdapter.builder().build())
      .add(new MyComponent())
      .build();

    StringWriter sw = new StringWriter();
    try (JsonWriter writer = jsonb.writer(sw)) {
      writer.unwrap(JsonGenerator.class).setCharacterEscapes(new HTMLCharacterEscapes());

      Address withHtml = new Address().street("<p>my-html-content-with[&][\"][']</p>");
      jsonb.toJson(withHtml, writer);
    }
    assertThat(sw.toString()).isEqualTo("{\"street\":\"&#60;p&#62;my-html-content-with[&#38;][&#34;][&#39;]&#60;/p&#62;\"}");
  }

  static class HTMLCharacterEscapes extends JsonpCharacterEscapes {

    @Override
    public int[] getEscapeCodesForAscii() {
      int[] asciiEscapes = CharacterEscapes.standardAsciiEscapesForJSON();
      // and force escaping of a few others:
      asciiEscapes['<'] = CharacterEscapes.ESCAPE_CUSTOM;
      asciiEscapes['>'] = CharacterEscapes.ESCAPE_CUSTOM;
      asciiEscapes['&'] = CharacterEscapes.ESCAPE_CUSTOM;
      asciiEscapes['"'] = CharacterEscapes.ESCAPE_CUSTOM;
      asciiEscapes['\''] = CharacterEscapes.ESCAPE_CUSTOM;
      return asciiEscapes;
    }

    @Override
    public SerializableString getEscapeSequence(int ch) {
      switch (ch) {
        case '&' : return new SerializedString("&#38;");
        case '<' : return new SerializedString("&#60;");
        case '>' : return new SerializedString("&#62;");
        case '\"' : return new SerializedString("&#34;");
        case '\'' : return new SerializedString("&#39;");
        default : return super.getEscapeSequence(ch);
      }
    }
  }
}
