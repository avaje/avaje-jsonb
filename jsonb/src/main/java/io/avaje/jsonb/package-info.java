/**
 * Core API of Jsonb.
 *
 * <h4>Initialise with defaults</h3>
 *
 * <pre>{@code
 *   Jsonb jsonb = Jsonb.newBuilder().build();
 * }</pre>
 *
 * <h4>Initialise with some configuration</h3>
 *
 * <pre>{@code
 *   Jsonb jsonb = Jsonb.newBuilder()
 *     .serializeNulls(true)
 *     .serializeEmpty(true)
 *     .failOnUnknown(true)
 *     .build();
 * }</pre>
 *
 * <h4>fromJson</h4>
 * <p>
 * Read json content from: String, byte[], Reader, InputStream, JsonReader
 * </p>
 * <pre>{@code
 *
 *  JsonType<Customer> customerType = jsonb.type(Customer.class);
 *
 *  Customer customer = customerType.fromJson(content);
 *
 * }</pre>
 *
 * <h4>toJson</h4>
 * <p>
 * Write json content to: String, byte[], Writer, OutputStream, JsonWriter
 * </p>
 * <pre>{@code
 *
 *  JsonType<Customer> customerType = jsonb.type(Customer.class);
 *
 *  String asJson = customerType.toJson(customer);
 *
 * }</pre>
 *
 * @see io.avaje.jsonb.Jsonb
 * @see io.avaje.jsonb.JsonType
 */
package io.avaje.jsonb;
