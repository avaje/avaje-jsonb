/*
 * Copyright (C) 2018 Square, Inc.
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
package io.avaje.mason;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Customizes how a type is encoded as JSON.
 */
@Retention(RUNTIME)
@Documented
public @interface JsonClass {

//  /**
//   * True to trigger the annotation processor to generate an adapter for this type.
//   *
//   * <p>There are currently some restrictions on which types that can be used with generated
//   * adapters:
//   *
//   * <ul>
//   *   <li>The class must be implemented in Kotlin (unless using a custom generator, see {@link
//   *       #generator()}).
//   *   <li>The class may not be an abstract class, an inner class, or a local class.
//   *   <li>All superclasses must be implemented in Kotlin.
//   *   <li>All properties must be public, protected, or internal.
//   *   <li>All properties must be either non-transient or have a default value.
//   * </ul>
//   */
//  boolean generateAdapter();

  /**
   * Not used yet.
   */
  String generator() default "";
}
