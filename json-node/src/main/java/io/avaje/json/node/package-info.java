/**
 * JsonNode types and adapters.
 *
 * <h4>JsonNodeMapper</h4>
 * <p>
 * Create a JsonNodeMapper using default settings. This instance is thread safe,
 * ideally one instance is created and used for all JsonNode use.
 *
 * <pre>{@code
 *
 * static final JsonNodeMapper mapper =
 *     JsonNodeMapper
 *       .builder()
 *       .build();
 *
 * }</pre>
 *
 * <h4>toJson fromJson</h4>
 * <p>
 * JsonNodeMapper provides helper method for {@code toJson()} and {@code fromJson()}.
 *
 * <pre>{@code
 *
 * var jsonObject = JsonObject.create()
 *       .add("name", JsonString.of("foo"))
 *       .add("other", JsonInteger.of(42));
 *
 * String asJson = mapper.toJson(jsonObject);
 *
 * }</pre>
 */
package io.avaje.json.node;
