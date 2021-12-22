package io.avaje.jsonb.jakarta;

import java.io.StringWriter;
import java.io.Writer;

/**
 * Efficient alternative to {@link StringWriter}, based on using segmented internal buffer.
 */
final class TextBufferWriter extends Writer {

  private final TextBuffer buffer;

  TextBufferWriter(TextBuffer buffer) {
    super();
    this.buffer = buffer;
  }

  String getAndClear() {
    return buffer.getAndClear();
  }

  @Override
  public Writer append(char c) {
    write(c);
    return this;
  }

  @Override
  public Writer append(CharSequence csq) {
    String str = csq.toString();
    buffer.append(str, 0, str.length());
    return this;
  }

  @Override
  public Writer append(CharSequence csq, int start, int end) {
    String str = csq.subSequence(start, end).toString();
    buffer.append(str, 0, str.length());
    return this;
  }

  @Override
  public void close() {
    // do nothing
  }

  @Override
  public void flush() {
    // do nothing
  }

  @Override
  public void write(char[] cbuf) {
    buffer.append(cbuf, 0, cbuf.length);
  }

  @Override
  public void write(char[] cbuf, int off, int len) {
    buffer.append(cbuf, off, len);
  }

  @Override
  public void write(int c) {
    buffer.append((char) c);
  }

  @Override
  public void write(String str) {
    buffer.append(str, 0, str.length());
  }

  @Override
  public void write(String str, int off, int len) {
    buffer.append(str, off, len);
  }
}
