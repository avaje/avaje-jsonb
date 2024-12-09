/**
 * Default stream handling provided with Jsonb.
 * <p>
 * This implementation is provided by default as it is known to perform faster
 * than Jackson's JsonParser/JsonGenerator that comes with <code>jackson-core</code>.
 * <p>
 * Including <code>avaje-jsonb-jackson</code> would replace this implementation
 * with one based on <code>jackson-core</code> to do the underlying JSON parsing
 * and generation.
 *
 * @see io.avaje.json.stream.core.JsonStreamBuilder
 */
package io.avaje.json.stream.core;
