package io.avaje.json.stream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Default implementation that simply wraps OutputStream.
 */
final class DJsonOutput implements JsonOutput {

  private final OutputStream outputStream;

  DJsonOutput(OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  @Override
  public void write(byte[] content, int offset, int length) throws IOException {
    outputStream.write(content, offset, length);
  }

  @Override
  public void flush() throws IOException {
    outputStream.flush();
  }

  @Override
  public void close() throws IOException {
    outputStream.close();
  }

  @Override
  public OutputStream unwrapOutputStream() {
    return outputStream;
  }
}
