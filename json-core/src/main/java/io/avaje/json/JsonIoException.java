/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.avaje.json;

import java.io.IOException;

/**
 * Thrown when underlying IOException occurred during read or write of json.
 */
public class JsonIoException extends JsonException {

  static final long serialVersionUID = 1L;

  public JsonIoException(IOException cause) {
    super(cause);
  }

  public JsonIoException(String message, IOException cause) {
    super(message, cause);
  }

  public JsonIoException(String message) {
    super(message);
  }
}
