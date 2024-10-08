package io.avaje.jsonb.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import io.avaje.jsonb.JsonIoException;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.spi.BufferedJsonWriter;
import io.avaje.jsonb.spi.BytesJsonWriter;
import io.avaje.jsonb.spi.JsonStreamAdapter;
import io.avaje.jsonb.spi.PropertyNames;
import io.avaje.jsonb.stream.JsonOutput;

import java.io.*;

/**
 * Jackson Core implementation of JsonStreamAdapter.
 * <p>
 * This uses the JsonParser and JsonGenerator of Jackson core to do the underlying parsing and
 * generation.
 *
 * <h3>Explicitly initialise</h3>
 * <p>
 * We can use it explicitly using code like:
 *
 * <pre>{@code
 *
 * JsonFactory customFactory = ...;
 *
 * var jacksonAdapter = JacksonAdapter.builder()
 *   .serializeNulls(true)
 *   .jsonFactory(customFactory)
 *   .build();
 *
 * Jsonb jsonb = Jsonb.builder()
 *   .adapter(jacksonAdapter)
 *   .build();
 *
 * }</pre>
 *
 * <h3>Service loading initialise</h3>
 * <p>
 * Including the <code>io.avaje:avaje-jsonb-jackson</em> dependency in the classpath means that this
 * JacksonAdapter will be automatically used via service loading.
 * <p>
 * In this automatic service loading case the defaults are:
 * <ul>
 *   <li>new JsonFactory()</li>
 *   <li>serializeNulls false</li>
 *   <li>serializeEmpty false</li>
 *   <li>failOnUnknown false</li>
 * </ul>
 */
public class JacksonAdapter implements JsonStreamAdapter {

  /**
   * Used to build JacksonAdapter with custom settings.
   */
  public static class Builder {

    private JsonFactory jsonFactory;
    private boolean serializeNulls;
    private boolean serializeEmpty;
    private boolean failOnUnknown;

    /**
     * Set the Jackson JsonFactory to use.
     */
    public Builder jsonFactory(JsonFactory jsonFactory) {
      this.jsonFactory = jsonFactory;
      return this;
    }

    /**
     * Set to true to serialize nulls. Defaults to false.
     */
    public Builder serializeNulls(boolean serializeNulls) {
      this.serializeNulls = serializeNulls;
      return this;
    }

    /**
     * Set to true to serialize empty collections. Defaults to false.
     */
    public Builder serializeEmpty(boolean serializeEmpty) {
      this.serializeEmpty = serializeEmpty;
      return this;
    }

    /**
     * Set to true to fail on unknown properties. Defaults to false.
     */
    public Builder failOnUnknown(boolean failOnUnknown) {
      this.failOnUnknown = failOnUnknown;
      return this;
    }

    /**
     * Build and return the JacksonAdapter.
     */
    public JacksonAdapter build() {
      if (jsonFactory == null) {
        jsonFactory = JsonFactory.builder()
          .enable(StreamReadFeature.USE_FAST_BIG_NUMBER_PARSER)
          .enable(StreamReadFeature.USE_FAST_DOUBLE_PARSER)
          .build();
      }
      return new JacksonAdapter(serializeNulls, serializeEmpty, failOnUnknown, jsonFactory);
    }
  }

  private final JsonFactory jsonFactory;
  private final boolean serializeNulls;
  private final boolean serializeEmpty;
  private final boolean failOnUnknown;

  /**
   * Create with the given default configuration.
   */
  public JacksonAdapter() {
    this(false, false, false, new JsonFactory());
  }

  public JacksonAdapter(JsonFactory factory) {
    this(false, false, false, factory);
  }

  /**
   * Create with the given default configuration.
   */
  public JacksonAdapter(boolean serializeNulls, boolean serializeEmpty, boolean failOnUnknown) {
    this(serializeNulls, serializeEmpty, failOnUnknown, new JsonFactory());
  }

  /**
   * Create additionally providing the jsonFactory.
   */
  public JacksonAdapter(boolean serializeNulls, boolean serializeEmpty, boolean failOnUnknown, JsonFactory jsonFactory) {
    this.serializeNulls = serializeNulls;
    this.serializeEmpty = serializeEmpty;
    this.failOnUnknown = failOnUnknown;
    this.jsonFactory = jsonFactory;
  }

  /**
   * Return a new builder to create a JacksonAdapter.
   *
   * <pre>{@code
   *
   * JsonFactory customFactory = ...;
   *
   * var jacksonAdapter = JacksonAdapter.builder()
   *   .serializeNulls(true)
   *   .jsonFactory(customFactory)
   *   .build();
   *
   * Jsonb jsonb = Jsonb.builder()
   *   .adapter(jacksonAdapter)
   *   .build();
   *
   * }</pre>
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public PropertyNames properties(String... names) {
    return new JacksonNames(names);
  }

  @Override
  public JsonReader reader(String json) {
    try {
      return new JacksonReader(jsonFactory.createParser(json), failOnUnknown);
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public JsonReader reader(byte[] json) {
    try {
      return new JacksonReader(jsonFactory.createParser(json), failOnUnknown);
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public JsonReader reader(Reader reader) {
    try {
      return new JacksonReader(jsonFactory.createParser(reader), failOnUnknown);
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public JsonReader reader(InputStream inputStream) {
    try {
      return new JacksonReader(jsonFactory.createParser(inputStream), failOnUnknown);
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public JsonWriter writer(Writer writer) {
    try {
      return new JacksonWriter(jsonFactory.createGenerator(writer), serializeNulls, serializeEmpty);
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public JsonWriter writer(OutputStream outputStream) {
    try {
      return new JacksonWriter(jsonFactory.createGenerator(outputStream), serializeNulls, serializeEmpty);
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public JsonWriter writer(JsonOutput output) {
    try {
      // just unwrap the underlying OutputStream, not supporting JsonOutput per say
      return new JacksonWriter(jsonFactory.createGenerator(output.unwrapOutputStream()), serializeNulls, serializeEmpty);
    } catch (IOException e) {
      throw new JsonIoException(e);
    }
  }

  @Override
  public BufferedJsonWriter bufferedWriter() {
    SegmentedStringWriter buffer = new SegmentedStringWriter(jsonFactory._getBufferRecycler());
    return new JacksonWriteBuffer(writer(buffer), buffer);
  }

  @Override
  public BytesJsonWriter bufferedWriterAsBytes() {
    ByteArrayBuilder buffer = new ByteArrayBuilder(jsonFactory._getBufferRecycler());
    return new JacksonWriteAsBytes(writer(buffer), buffer);
  }
}
