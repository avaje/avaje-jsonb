package io.avaje.jsonb.stream;

import io.helidon.http.Http;
import io.helidon.webserver.http.ServerResponse;

import java.io.IOException;
import java.io.OutputStream;

final class NimaJsonOutput implements JsonOutput {

  private final ServerResponse res;
  private OutputStream os;
  private boolean fixedLength;

  NimaJsonOutput(ServerResponse res) {
    this.res = res;
  }

  private OutputStream os() {
    if (os == null) {
      if (!fixedLength) {
        // going to use chunked content
        res.header(Http.HeaderNames.TRANSFER_ENCODING, "chunked");
      }
      os = res.outputStream();
    }
    return os;
  }

  @Override
  public void write(byte[] content, int offset, int length) throws IOException {
    os().write(content, offset, length);
  }

  @Override
  public void writeLast(byte[] content, int offset, int length) throws IOException {
    if (offset == 0 && os == null) {
      // going to use fixed length content
      fixedLength = true;
      res.contentLength(length);
    }
    os().write(content, offset, length);
  }

  @Override
  public void flush() throws IOException {
    if (os != null) {
      os.flush();
    }
  }

  @Override
  public void close() throws IOException {
    if (os != null) {
      os.close();
    }
  }

  @Override
  public OutputStream unwrapOutputStream() {
    return res.outputStream();
  }
}
