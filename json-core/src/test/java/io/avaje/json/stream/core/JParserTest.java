package io.avaje.json.stream.core;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JParserTest {

  final JParser.ErrorInfo errorInfo = JParser.ErrorInfo.MINIMAL;
  final JParser.DoublePrecision doublePrecision = JParser.DoublePrecision.HIGH;
  final JParser.UnknownNumberParsing unknownNumbers = JParser.UnknownNumberParsing.BIGDECIMAL;
  final int maxNumberDigits = 100;
  final int maxStringBuffer = 1000;

  @Test
  void readRaw_when_bytes() {
    JParser parser = newParser(1000);

    String content = "{\"id\":43, \"content\":\"my-content\"}";
    initParserViaByteArray(parser, content);

    assertThat(parser.nextField()).isEqualTo("content");
    assertThat(parser.readRaw()).isEqualTo("\"my-content\"");
  }

  @Test
  void readRaw_when_bytes_inputExceedsBuffer() {
    JParser parser = newParser(100);

    String raw = createLargeContent();
    String content = "{\"id\":43, \"content\":" + raw + "}";
    initParserViaByteArray(parser, content);

    assertThat(parser.nextField()).isEqualTo("content");
    assertThat(parser.readRaw()).isEqualTo(raw);
  }

  @Test
  void readRaw_when_streaming_notExceedingBuffer() {
    JParser parser = newParser(1000);

    String content = "{\"id\":43, \"content\":\"my-content\"}";
    initParserViaStream(parser, content);

    assertThat(parser.nextField()).isEqualTo("content");
    assertThat(parser.readRaw()).isEqualTo("\"my-content\"");
  }

  @Test
  void readRaw_when_streaming_exceedBuffer() {
    JParser parser = newParser(100);

    String content = "{\"id\":43, \"content\":\"this-is-my-content-that-exceeds-buffer-size|this-is-my-content-that-exceeds-buffer-size\"}";
    initParserViaStream(parser, content);

    assertThat(parser.nextField()).isEqualTo("content");
    assertThat(parser.readRaw()).isEqualTo("\"this-is-my-content-that-exceeds-buffer-size|this-is-my-content-that-exceeds-buffer-size\"");
  }

  @Test
  void readRaw_when_streaming_exceedBufferMore() {
    JParser parser = newParser(100);

    String raw = createLargeContent();
    String content = "{\"id\":43, \"content\":" + raw + "}";

    initParserViaStream(parser, content);
    assertThat(parser.nextField()).isEqualTo("content");
    assertThat(parser.readRaw()).isEqualTo(raw);
  }

  private String createLargeContent() {
    StringBuilder raw = new StringBuilder("\"begin");
    String add = "|this-is-my-content-that-exceeds-buffer-size";
    for (int i = 0; i < 10; i++) {
      raw.append(add);
    }
    raw.append("|end\"");
    return raw.toString();
  }

  private void initParserViaByteArray(JParser parser, String content) {
    byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
    parser.process(bytes, bytes.length);
    initialReadToContent(parser);
  }

  private void initParserViaStream(JParser parser, String content) {
    parser.process(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    initialReadToContent(parser);
  }

  private void initialReadToContent(JParser parser) {
    parser.nextToken();
    parser.nextToken();
    assertThat(parser.nextField()).isEqualTo("id");
    assertThat(parser.readInt()).isEqualTo(43);
    parser.nextToken();
    parser.nextToken();
  }

  private JParser newParser(int len) {
    final char[] tmp = new char[len];
    final byte[] buffer = new byte[len];
    return new JParser(tmp, buffer, buffer.length, errorInfo, doublePrecision, unknownNumbers, maxNumberDigits, maxStringBuffer);
  }
}
