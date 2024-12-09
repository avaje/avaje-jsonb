package io.avaje.jsonb;

import io.avaje.json.JsonReader;

import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Provides API to serialise a type to and from JSON.
 * <p>
 * JsonType provides the main API used to read and write json.
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
 * <p>
 * Moshi note: JsonType does not exist in Moshi and has been added to provide a
 * slightly nicer API to use than JsonAdapter.
 */
public interface JsonType<T> extends JsonView<T> {

  /**
   * Build and return the view given the DSL that specifies the properties to include.
   * <p>
   * Examples of json view DSL:
   * <pre>{@code
   *
   *   // only include the id and name properties
   *   (id, name)
   *
   *   // include billAddress which is a nested type
   *   (id, name, billingAddress(street, suburb))
   *
   *   // include billAddress with all it's properties
   *   (id, name, billingAddress(*))
   *
   *   (id, name, billingAddress(street, suburb), shippingAddress(*), contacts(email,lastName, firstName))
   *
   * }</pre>
   */
  JsonView<T> view(String dsl);

  /**
   * Return the list type for this JsonType.
   */
  JsonType<List<T>> list();

  /**
   * Prefer use of {@link JsonType#stream(JsonReader)} rather than using this stream type directly.
   * <p>
   * Generally, we should not use this type directly but instead create a  {@link JsonReader}
   * and use {@link JsonType#stream(JsonReader)} instead. Then we just use a try-with-resources on
   * the JsonReader and can additionally specify {@link JsonReader#streamArray(boolean)} option.
   * <p>
   * When using this Stream type directly, use a try-with-resources block with the Stream
   * to ensure that any underlying resources are closed.
   *
   * <pre>{@code
   *
   *  JsonType<Stream<MyBean>> type =  jsonb.type(MyBean.class).stream();
   *
   *  try (Stream<MyBean> asStream = type.fromJson(content)) {
   *    // use the stream
   *    ...
   *  }
   *
   * }</pre>
   *
   * @return The stream type for this base JsonType.
   * @see #stream(JsonReader)
   */
  JsonType<Stream<T>> stream();

  /**
   * Return the set type for this JsonType.
   */
  JsonType<Set<T>> set();

  /**
   * Return the map with this type as the value type and string keys.
   */
  JsonType<Map<String, T>> map();

  /**
   * Return the Optional type for this JsonType.
   */
  JsonType<Optional<T>> optional();

  /**
   * Read the return the value from the reader.
   */
  T fromJson(JsonReader reader);

  /**
   * Read the return the value from the json content.
   */
  T fromJson(String content);

  /**
   * Read the return the value from the json content.
   */
  T fromJson(byte[] content);

  /**
   * Read the return the value from the reader.
   */
  T fromJson(Reader reader);

  /**
   * Read the return the value from the inputStream.
   */
  T fromJson(InputStream inputStream);

  /**
   * Convert from 'object form' expecting {@code Map<String,Object>} for
   * 'json object' and expecting {@code Collection<?>} for 'json array'.
   *
   * @param value The json value that will be converted into T.
   * @return The value converted from 'object form'.
   */
  T fromObject(Object value);

  /**
   * Return as a Stream that will read the content as the stream is processed.
   * <p>
   * Use a try-with-resources block with the JsonReader to ensure the underlying
   * resources are closed.
   *
   * <pre>{@code
   *
   *  JsonType<MyBean> type =  jsonb.type(MyBean.class);
   *
   *  try (JsonReader reader = jsonb.reader(content)) {
   *
   *    Stream<MyBean> asStream = type.stream(reader);
   *    ...
   *  }
   *
   * }</pre>
   *
   * <h3>When using Jackson</h3>
   * <p>
   * When using Jackson-core as the underlying parser we should explicitly state that the content is either
   * an ARRAY (with '[' and ']' tokens or not (x-json-stream new line delimited, there are no '[' and ']' tokens).
   * <p>
   * When using the builtin avaje-jsonb parser, it automatically detects and handles both cases (with or without the
   * '[' and ']' tokens). With the Jackson-core parser we need to explicitly state if we are processing
   *
   * <pre>{@code
   *
   *  JsonType<MyBean> type =  jsonb.type(MyBean.class);
   *
   *  try (JsonReader reader = jsonb.reader(content)) {
   *    // when the content contains the ARRAY '[', ']' tokens set streamArray(true)
   *    Stream<MyBean> asStream = type.stream(reader.streamArray(true));
   *    ...
   *  }
   *
   * }</pre>
   */
  Stream<T> stream(JsonReader reader);

}
