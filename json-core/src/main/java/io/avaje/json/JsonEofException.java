package io.avaje.json;

import java.io.IOException;

/**
 * Thrown when we hit EOF unexpectedly.
 */
public class JsonEofException extends JsonIoException {

  static final long serialVersionUID = 1L;

  public JsonEofException(IOException cause) {
    super(cause);
  }

  public JsonEofException(String message, IOException cause) {
    super(message, cause);
  }

  public JsonEofException(String message) {
    super(message);
  }
}
