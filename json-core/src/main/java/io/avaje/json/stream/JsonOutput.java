package io.avaje.json.stream;

import io.helidon.webserver.http.ServerResponse;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Output that can be aware of server content chunking.
 * <p>
 * Typically, for HTTP servers that can send output using fixed length or chunked.
 */
public interface JsonOutput extends Closeable {

  /**
   * Create as a simple wrapper for OutputStream.
   */
  static JsonOutput of(OutputStream outputStream) {
    return new DJsonOutput(outputStream);
  }

  /**
   * Create for Nima ServerResponse.
   */
  static JsonOutput of(ServerResponse nimaServerResponse) {
    return new NimaJsonOutput(nimaServerResponse);
  }

  /**
   * Write the content to the underlying output stream.
   */
  void write(byte[] content, int offset, int length) throws IOException;

  /**
   * Write the last content to the underlying output stream.
   * <p>
   * Given that this is known to be the last content written an implementation can make
   * use of this to optimise for sending as fixed length content.
   */
  default void writeLast(byte[] content, int offset, int length) throws IOException {
    write(content, offset, length);
  }

  /**
   * Flush the underlying OutputStream.
   */
  void flush() throws IOException;

  /**
   * Return the underlying OutputStream.
   * <p>
   * This is used for Jsonb adapters (Jackson) that can't support writeLast() semantics.
   */
  OutputStream unwrapOutputStream();
}
