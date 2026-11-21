package io.avaje.json.stream.core;

import org.junit.jupiter.api.Test;

import io.avaje.json.JsonEofException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JParserTest {

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

  @Test
  void readRaw_when_streaming_exceedsStringLimit() {
    JParser parser = newParser(4096, 50_000, 200_000);
    String raw = "\"" + "x".repeat(100_001) + "\"";
    String content = "{\"id\":43, \"content\":" + raw + "}";
    initParserViaStream(parser, content);

    assertThat(parser.nextField()).isEqualTo("content");
    assertThat(parser.readRaw()).isEqualTo(raw);
  }

  @Test
  void readRaw_when_streaming_exceedsRawLimit() {
    JParser parser = newParser(4096, 200_000, 50_000);
    String raw = "\"" + "x".repeat(100_001) + "\"";
    String content = "{\"id\":43, \"content\":" + raw + "}";
    initParserViaStream(parser, content);

    assertThat(parser.nextField()).isEqualTo("content");
    assertThatThrownBy(parser::readRaw)
      .hasMessageContaining("Maximum buffer limit exceeded for raw content: '50000'");
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

  @Test
  void isNullValue_topLevel_null() {
    JParser parser = newParser(1000);
    byte[] bytes = "null".getBytes(StandardCharsets.UTF_8);
    parser.process(bytes, bytes.length);
    assertThat(parser.isNullValue()).isTrue();
  }

  @Test
  void isNullValue_topLevel_string() {
    JParser parser = newParser(1000);
    byte[] bytes = "\"hi\"".getBytes(StandardCharsets.UTF_8);
    parser.process(bytes, bytes.length);
    assertThat(parser.isNullValue()).isFalse();
    assertThat(parser.readString()).isEqualTo("hi");
  }

  @Test
  void readString_asciiCodec_respectsMaxStringBuffer() {
    JParser withinLimitParser = newParser(64, 8);
    byte[] withinLimit = ("\"" + "x".repeat(8) + "\"").getBytes(StandardCharsets.UTF_8);
    withinLimitParser.process(withinLimit, withinLimit.length);
    withinLimitParser.nextToken();
    String decoded = withinLimitParser.readString(text -> text);
    assertThat(decoded).isEqualTo("x".repeat(8));

    JParser overLimitParser = newParser(64, 8);
    byte[] overLimit = ("\"" + "x".repeat(9) + "\"").getBytes(StandardCharsets.UTF_8);
    overLimitParser.process(overLimit, overLimit.length);
    overLimitParser.nextToken();
    assertThatThrownBy(() -> overLimitParser.readString(text -> text))
      .hasMessageContaining("Maximum string buffer limit exceeded");
  }

  @Test
  void readString_nonPowerOfTwoMaxStringBuffer() {
    JParser parser = newParser(64, 100);

    String exactAscii = "x".repeat(100);
    byte[] exactBytes = ("\"" + exactAscii + "\"").getBytes(StandardCharsets.UTF_8);
    parser.process(exactBytes, exactBytes.length);
    parser.nextToken();
    assertThat(parser.readString()).isEqualTo(exactAscii);

    byte[] exactEscaped = ("\"" + "x".repeat(99) + "\\u0041\"").getBytes(StandardCharsets.UTF_8);
    parser.process(exactEscaped, exactEscaped.length);
    parser.nextToken();
    assertThat(parser.readString()).isEqualTo("x".repeat(99) + "A");

    String emoji = "\uD83D\uDE00"; // 2 UTF-16 chars
    byte[] exactSurrogate = ("\"" + "x".repeat(98) + emoji + "\"").getBytes(StandardCharsets.UTF_8);
    parser.process(exactSurrogate, exactSurrogate.length);
    parser.nextToken();
    assertThat(parser.readString()).isEqualTo("x".repeat(98) + emoji);

    byte[] overLimit = ("\"" + "x".repeat(101) + "\"").getBytes(StandardCharsets.UTF_8);
    parser.process(overLimit, overLimit.length);
    parser.nextToken();
    assertThatThrownBy(parser::readString)
      .hasMessageContaining("Maximum string buffer limit exceeded");

    JParser maxIntParser = newParser(64, Integer.MAX_VALUE);
    maxIntParser.process(exactBytes, exactBytes.length);
    maxIntParser.nextToken();
    assertThat(maxIntParser.readString()).isEqualTo(exactAscii);
  }

  @Test
  void readString_whenCharBufferExceedsMaxStringBuffer_failsFastOnStream() {
    JParser parser = newParser(4096, 10);

    byte[] withinLimit = ("\"" + "x".repeat(10) + "\"").getBytes(StandardCharsets.UTF_8);
    parser.process(withinLimit, withinLimit.length);
    parser.nextToken();
    assertThat(parser.readString()).isEqualTo("x".repeat(10));
    parser.close();

    var readCount = new java.util.concurrent.atomic.AtomicInteger();
    byte[] streamBytes = ("\"" + "x".repeat(20_000) + "\"").getBytes(StandardCharsets.UTF_8);
    parser.process(new ByteArrayInputStream(streamBytes) {
      @Override
      public int read(byte[] b, int off, int len) {
        int n = super.read(b, off, len);
        if (n > 0) {
          readCount.addAndGet(n);
        }
        return n;
      }
    });
    parser.nextToken();
    assertThatThrownBy(parser::readString)
      .hasMessageContaining("Maximum string buffer limit exceeded");
    // Fails after reading only the initial parser byte buffer (4096), not the full 20,000-byte stream
    assertThat(readCount.get()).isEqualTo(4096);
  }

  @Test
  void isNullValue_topLevel_int() {
    JParser parser = newParser(1000);
    byte[] bytes = "42".getBytes(StandardCharsets.UTF_8);
    parser.process(bytes, bytes.length);
    assertThat(parser.isNullValue()).isFalse();
    assertThat(parser.readInt()).isEqualTo(42);
  }

  @Test
  void process_afterReadFailedInsideObjectWithNames_keysAreDecoded() {
    JParser parser = newParser(1000);
    JsonReadAdapter reader = new JsonReadAdapter(parser, Recyclers.ThreadLocalPool.shared(), true, true);

    byte[] failing = "{\"m\":{\"a\":1".getBytes(StandardCharsets.UTF_8);
    parser.process(failing, failing.length);
    reader.beginObject(JsonNames.of("m", "after"));
    assertThat(reader.hasNextField()).isTrue();
    assertThat(reader.nextField()).isEqualTo("m");
    reader.beginObject();
    assertThat(reader.hasNextField()).isTrue();
    assertThat(reader.nextField()).isEqualTo("a");
    assertThat(reader.readInt()).isEqualTo(1);
    assertThatThrownBy(reader::hasNextField).isInstanceOf(JsonEofException.class);

    byte[] content = "{\"b\\\"c\":1}".getBytes(StandardCharsets.UTF_8);
    parser.process(content, content.length);
    reader.beginObject();
    assertThat(reader.hasNextField()).isTrue();
    assertThat(reader.nextField()).isEqualTo("b\"c");
    assertThat(reader.readInt()).isEqualTo(1);
    assertThat(reader.hasNextField()).isFalse();
    reader.endObject();
  }

  private JParser newParser(int len) {
    return newParser(len, maxStringBuffer);
  }

  private JParser newParser(int len, int maxStringBuffer) {
    return newParser(len, maxStringBuffer, maxStringBuffer);
  }

  private JParser newParser(int len, int maxStringBuffer, int maxRawBuffer) {
    final char[] tmp = new char[len];
    final byte[] buffer = new byte[len];
    return new JParser(tmp, buffer, buffer.length, doublePrecision, unknownNumbers, maxNumberDigits, maxStringBuffer, maxRawBuffer);
  }
}
