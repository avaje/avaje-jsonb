package io.avaje.jsonb;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.PropertyNames;
import io.avaje.json.stream.*;
import io.avaje.jsonb.core.DefaultBootstrap;
import io.avaje.jsonb.spi.JsonStreamFactory;
import io.avaje.jsonb.spi.JsonbComponent;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.function.Supplier;

/**
 * Provides access to json adapters by type.
 *
 * <h4>Initialise with defaults</h3>
 *
 * <pre>{@code
 *   Jsonb jsonb = Jsonb.builder().build();
 * }</pre>
 *
 * <h4>Initialise with some configuration</h3>
 *
 * <pre>{@code
 *   Jsonb jsonb = Jsonb.builder()
 *     .serializeNulls(true)
 *     .serializeEmpty(true)
 *     .failOnUnknown(true)
 *     .build();
 * }</pre>
 *
 * <h4>Initialise using Jackson core with configuration</h3>
 * <p>
 * We need to include the dependency <code>io.avaje:avaje-jsonb-jackson</code> to do this.
 * This will use Jackson core JsonParser and JsonGenerator to do the underlying parsing and generation.
 * </p>
 * <pre>{@code
 *
 *   // create the Jackson JsonFactory
 *   JsonFactory customFactory = ...;
 *
 *   var jacksonAdapter = JacksonAdapter.builder()
 *     .serializeNulls(true)
 *     .jsonFactory(customFactory)
 *     .build();
 *
 *   Jsonb jsonb = Jsonb.builder()
 *     .adapter(jacksonAdapter)
 *     .build();
 *
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
 */
public interface Jsonb {

  /**
   * Create a new Jsonb.Builder to configure and build the Jsonb instance.
   * <p>
   * We can register JsonAdapter's to use for specific types before building and returning
   * the Jsonb instance to use.
   * <p>
   * Note that JsonAdapter's that are generated are automatically registered via service
   * loading so there is no need to explicitly register those generated JsonAdapters.
   *
   * <pre>{@code
   *
   *   Jsonb jsonb = Jsonb.builder()
   *     .serializeNulls(true)
   *     .serializeEmpty(true)
   *     .failOnUnknown(true)
   *     .build();
   *
   * }</pre>
   */
  static Builder builder() {
    return DefaultBootstrap.builder();
  }

  /**
   * Return json content for the given object.
   * <p>
   * This is a convenience method for {@code jsonb.type(Object.class).toJson(any) }
   *
   * @param any The object to return as json string
   * @return Return json content for the given object.
   */
  String toJson(Object any);

  /**
   * Return json content in pretty format for the given object.
   * <p>
   * This is a convenience method for {@code jsonb.type(Object.class).toJsonPretty(any) }
   *
   * @param any The object to return as json string in pretty format
   * @return Return json content in pretty format for the given object.
   */
  String toJsonPretty(Object any);

  /**
   * Return the value as json content in bytes form.
   * <p>
   * This is a convenience method for {@code jsonb.type(Object.class).toJsonBytes(any) }
   */
  byte[] toJsonBytes(Object any);

  /**
   * Write to the given writer.
   * <p>
   * This is a convenience method for {@code jsonb.type(Object.class).toJson(any, writer) }
   */
  void toJson(Object any, Writer writer);

  /**
   * Write to the given outputStream.
   * <p>
   * This is a convenience method for {@code jsonb.type(Object.class).toJsonBytes(any, outputStream) }
   */
  void toJson(Object any, OutputStream outputStream);

  /**
   * Write to the given writer.
   * <p>
   * This is a convenience method for {@code jsonb.type(Object.class).toJson(any, writer) }
   */
  void toJson(Object any, JsonWriter jsonWriter);

  /**
   * Return the JsonType used to read and write json for the given class.
   *
   * <h3>fromJson() example</h3>
   * <pre>{@code
   *
   *   Customer customer = jsonb
   *     .type(Customer.class)
   *     .fromJson(jsonContent);
   *
   *
   *   // list
   *   List<Customer> customers = jsonb
   *     .type(Customer.class)
   *     .list()
   *     .fromJson(jsonContent);
   *
   * }</pre>
   *
   * <h3>toJson() example</h3>
   * <pre>{@code
   *
   *   Object anything = ...
   *   String jsonContent = jsonb.toJson(anything);
   *
   *   Customer customer = ...
   *
   *   // any type toJson()
   *   String jsonContent = jsonb.toJson(customer);
   *
   *   // or use .type(Customer.class) if we like
   *   String jsonContent = jsonb
   *     .type(Customer.class)
   *     .toJson(customer);
   *
   * }</pre>
   *
   * <h3>Using Object.class</h3>
   * <p>
   * We can use <code>type(Object.class)</code> when we don't know the specific type that is being
   * written toJson or read fromJson.
   * <p>
   *
   * <h3>Object toJson()</h3>
   * <pre>{@code
   *
   *   Object any = ...
   *
   *   String jsonContent = jsonb
   *     .type(Object.class)
   *     .toJson(any);
   *
   *   // which is the same as
   *   String jsonContent = jsonb.toJson(any);
   *
   * }</pre>
   * <p>
   * When using <code>Object.class</code> and writing <code>toJson()</code> then the underlying JsonAdapter
   * is determined dynamically based on the type of the object value passed in.
   * <p>
   * When using <code>Object.class</code> and reading <code>fromJson()</code> then the java types used in
   * the result are determined dynamically based on the json types being read and the resulting java types
   * are ArrayList, LinkedHashMap, String, boolean, and double.
   */
  <T> JsonType<T> type(Class<T> cls);

  /**
   * Return the JsonType used to read and write json for the given type.
   * <p>
   * We can use {@link Types} to obtain common generic types for List, Set, Map, Array etc.
   *
   * <h3>Example</h3>
   * <pre>{@code
   *
   *   JsonType<List<String>> listOfStringType = jsonb.type(Types.listOf(String.class))
   *
   *   JsonType<List<Customer>> listOfCustomerType = jsonb.type(Types.listOf(Customer.class))
   *
   *   JsonType<Map<String,Integer>> adapter = jsonb.type(Types.mapOf(Integer.class))
   *
   * }</pre>
   *
   * <h3>Using Object.class</h3>
   * <p>
   * We can use <code>type(Object.class)</code> when we don't know the specific type that is being
   * written toJson or read fromJson.
   *
   * <h3>Object toJson()</h3>
   * <pre>{@code
   *
   *   Object any = ...
   *
   *   String jsonContent = jsonb
   *     .type(Object.class)
   *     .toJson(any);
   *
   *   // the same as
   *   String jsonContent = jsonb.toJson(any);
   *
   * }</pre>
   * <p>
   * When using <code>Object.class</code> and writing <code>toJson()</code> then the underlying JsonAdapter
   * is determined dynamically based on the type of the object value passed in.
   * <p>
   * When using <code>Object.class</code> and reading <code>fromJson()</code> then the java types used in
   * the result are determined dynamically based on the json types being read and the resulting java types
   * are ArrayList, LinkedHashMap, String, boolean, and double.
   */
  <T> JsonType<T> type(Type type);

  /**
   * Return the JsonType for the given value using the class of the value being passed in.
   * <p>
   * This is a helper method that supports returning an inferred generic type.
   *
   * @param value The value of the given type
   * @param <T>   The inferred generic parameter type
   * @return JsonType for the given value
   */
  <T> JsonType<T> typeOf(Object value);

  /**
   * Return the JsonReader used to read the given json content.
   */
  JsonReader reader(String json);

  /**
   * Return the JsonReader used to read the given json content in bytes.
   */
  JsonReader reader(byte[] jsonBytes);

  /**
   * Return the JsonReader used to read the json content from the given reader.
   */
  JsonReader reader(Reader reader);

  /**
   * Return the JsonReader used to read the json content from the given inputStream.
   */
  JsonReader reader(InputStream inputStream);

  /**
   * Return the JsonWriter used to write json to the given writer.
   */
  JsonWriter writer(Writer writer);

  /**
   * Return the JsonWriter used to write json to the given outputStream.
   */
  JsonWriter writer(OutputStream outputStream);

  /**
   * Return the JsonWriter used to write json to the given output.
   */
  JsonWriter writer(JsonOutput output);

  /**
   * Return the property names as PropertyNames.
   * <p>
   * Provides the option of optimising the writing of json for property names
   * by having them already escaped and encoded rather than as plain strings.
   */
  PropertyNames properties(String... names);

  /**
   * Return the JsonAdapter used to read and write json for the given class.
   *
   * <p>JsonAdapter is generally used by generated serialization code. Application code should use
   * {@link Jsonb#type(Class)} and {@link JsonType} instead.
   */
  <T> JsonAdapter<T> adapter(Class<T> cls);

  /**
   * Return the JsonAdapter used to read and write json for the given type.
   *
   * <p>JsonAdapter is generally used by generated serialization code. Application code should use
   * {@link Jsonb#type(Type)} and {@link JsonType} instead.
   */
  <T> JsonAdapter<T> adapter(Type type);

  /**
   * Return a registered non-global custom adapter
   *
   * <p>JsonAdapter is generally used by generated serialization code. Application code should use
   * {@link Jsonb#type(Type)} and {@link JsonType} instead.
   *
   * @param clazz the class of the custom adapter to retrieve
   * @return the custom adapter used to read and write json for the given type.
   */
  <T> JsonAdapter<T> customAdapter(Class<? extends JsonAdapter<?>> clazz);

  /**
   * Raw JsonAdapter for raw json content.
   */
  JsonAdapter<String> rawAdapter();

  /**
   * Build the Jsonb instance adding JsonAdapter, Factory or AdapterBuilder.
   */
  interface Builder {

    /**
     * Set to serialise null values or not.
     * <p>
     * Default is to not serialise nulls.
     */
    Builder serializeNulls(boolean serializeNulls);

    /**
     * Set to serialise empty collections or not.
     * <p>
     * Default is to serialise empty collections.
     */
    Builder serializeEmpty(boolean serializeEmpty);

    /**
     * Set failOnUnknown to true such that an exception is thrown when unknown
     * properties are read in the json content.
     */
    Builder failOnUnknown(boolean failOnUnknown);

    /**
     * Set to true for BigDecimal and BigInteger to serialise as String values rather than number values.
     */
    Builder mathTypesAsString(boolean mathTypesAsString);

    /**
     * Determines how byte buffers are recycled
     */
    Builder bufferRecycling(BufferRecycleStrategy strategy);

    /**
     * Explicitly set the adapter to use.
     * <p>
     * When not set the JsonStreamAdapter is service loaded using {@link JsonStreamFactory}
     * with a fallback default of using the built-in implementation.
     *
     * @param streamAdapter The underlying adapter to use when generating and parsing
     */
    Builder adapter(JsonStream streamAdapter);

    /**
     * Add a JsonAdapter to use for the given type.
     */
    <T> Builder add(Type type, JsonAdapter<T> jsonAdapter);

    /**
     * Add a Supplier which provides a JsonAdapter to use for the given type.
     */
    <T> Builder add(Type type, Supplier<JsonAdapter<T>> builder);

    /**
     * Add a AdapterBuilder which provides a JsonAdapter to use for the given type.
     */
    Builder add(Type type, AdapterBuilder builder);

    /**
     * Add a Component which can provide multiple JsonAdapters and or configuration.
     */
    Builder add(JsonbComponent component);

    /**
     * Add a JsonAdapter.Factory which provides JsonAdapters to use.
     */
    Builder add(AdapterFactory factory);

    /**
     * Build and return the Jsonb instance with all the given adapters and factories registered.
     */
    Jsonb build();
  }

  /**
   * Function to build a JsonAdapter that needs Jsonb.
   */
  @FunctionalInterface
  interface AdapterBuilder {

    /**
     * Create a JsonAdapter given the Jsonb instance.
     */
    JsonAdapter<?> build(Jsonb jsonb);
  }
}
