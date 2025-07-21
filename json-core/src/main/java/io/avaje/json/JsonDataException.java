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

/**
 * Thrown when data being parsed is not encoded as valid json or attempting to write invalid json.
 */
public class JsonDataException extends JsonException {

  private static final long serialVersionUID = 1L;

  public JsonDataException(String message) {
    super(message);
  }

  public JsonDataException(Throwable cause) {
    super(cause);
  }

  public JsonDataException(String description, Throwable cause) {
    super(description, cause);
  }
}
