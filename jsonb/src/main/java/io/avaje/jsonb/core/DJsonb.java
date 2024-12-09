package io.avaje.jsonb.core;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.PropertyNames;
import io.avaje.json.stream.*;
import io.avaje.jsonb.*;
import io.avaje.jsonb.AdapterFactory;
import io.avaje.jsonb.spi.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static io.avaje.jsonb.core.Util.*;
import static java.util.Objects.requireNonNull;

/**
 * Default implementation of Jsonb.
 */
final class DJsonb implements Jsonb {

  private final CoreAdapterBuilder builder;
  private final JsonStream io;
  private final Map<Type, DJsonType<?>> typeCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<ViewKey, JsonView<?>> viewCache = new ConcurrentHashMap<>();
  private final JsonType<Object> anyType;

  DJsonb(
      JsonStream adapter,
      List<AdapterFactory> factories,
      boolean serializeNulls,
      boolean serializeEmpty,
      boolean failOnUnknown,
      boolean mathAsString,
      BufferRecycleStrategy strategy) {

    this.builder = new CoreAdapterBuilder(this, factories, mathAsString);
    if (adapter != null) {
      this.io = adapter;
    } else {
      var adapterFactoryOptional = ExtensionLoader.adapterFactory();
      if (adapterFactoryOptional.isPresent()) {
        this.io = adapterFactoryOptional.get().create(serializeNulls, serializeEmpty, failOnUnknown);
      } else {
        this.io = JsonStream.builder()
          .serializeNulls(serializeNulls)
          .serializeEmpty(serializeEmpty)
          .failOnUnknown(failOnUnknown)
          .bufferRecycling(strategy)
          .build();
      }
    }
    this.anyType = type(Object.class);
  }

  @Override
  public String toJson(Object any) {
    return anyType.toJson(any);
  }

  @Override
  public String toJsonPretty(Object any) {
    return anyType.toJsonPretty(any);
  }

  @Override
  public byte[] toJsonBytes(Object any) {
    return anyType.toJsonBytes(any);
  }

  @Override
  public void toJson(Object any, Writer writer) {
    anyType.toJson(any, writer);
  }

  @Override
  public void toJson(Object any, OutputStream outputStream) {
    anyType.toJson(any, outputStream);
  }

  @Override
  public void toJson(Object any, JsonWriter jsonWriter) {
    anyType.toJson(any, jsonWriter);
  }

  @Override
  public PropertyNames properties(String... names) {
    return io.properties(names);
  }

  BufferedJsonWriter bufferedWriter() {
    return io.bufferedWriter();
  }

  BytesJsonWriter bufferedWriterAsBytes() {
    return io.bufferedWriterAsBytes();
  }

  @Override
  public JsonWriter writer(Writer writer) {
    return io.writer(writer);
  }

  @Override
  public JsonWriter writer(OutputStream outputStream) {
    return io.writer(outputStream);
  }

  @Override
  public JsonWriter writer(JsonOutput output) {
    return io.writer(output);
  }

  @Override
  public JsonReader reader(Reader reader) {
    return io.reader(reader);
  }

  @Override
  public JsonReader reader(InputStream inputStream) {
    return io.reader(inputStream);
  }

  @Override
  public JsonReader reader(String json) {
    return io.reader(json);
  }

  @Override
  public JsonReader reader(byte[] json) {
    return io.reader(json);
  }

  @Override
  public <T> JsonType<T> type(Class<T> cls) {
    return typeWithCache(cls);
  }

  @Override
  public <T> JsonType<T> type(Type type) {
    return typeWithCache(type);
  }

  @Override
  public <T> JsonType<T> typeOf(Object first) {
    requireNonNull(first);
    return type((Type) first.getClass());
  }

  @SuppressWarnings("unchecked")
  private <T> JsonType<T> typeWithCache(Type type) {
    return (JsonType<T>) typeCache.computeIfAbsent(type, _type -> new DJsonType<>(this, _type, adapter(_type)));
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> JsonAdapter<T> customAdapter(Class<? extends JsonAdapter<?>> cls) {
    return (JsonAdapter<T>) adapter(cls);
  }

  @Override
  public <T> JsonAdapter<T> adapter(Class<T> cls) {
    Type cacheKey = canonicalizeClass(requireNonNull(cls));
    JsonAdapter<T> result = builder.get(cacheKey);
    if (result != null) {
      return result;
    }
    return builder.build(cacheKey);
  }

  @Override
  public <T> JsonAdapter<T> adapter(Type type) {
    type = removeSubtypeWildcard(canonicalize(requireNonNull(type)));
    Object cacheKey = type;
    JsonAdapter<T> result = builder.get(cacheKey);
    if (result != null) {
      return result;
    }
    return builder.build(type, cacheKey);
  }

  @Override
  public JsonAdapter<String> rawAdapter() {
    return RawAdapter.STR;
  }

  JsonReader objectReader(Object value) {
    return new ObjectJsonReader(value);
  }

  @SuppressWarnings("unchecked")
  <T> JsonView<T> buildView(final String dsl, final JsonAdapter<T> adapter, final Type type) {
    final ViewKey key = new ViewKey(dsl, type);
    return (JsonView<T>) viewCache.computeIfAbsent(key, o -> {
      try {
        CoreViewBuilder viewBuilder = new CoreViewBuilder(ViewDsl.parse(dsl));
        adapter.viewBuild().build(viewBuilder);
        return viewBuilder.build(this);
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    });
  }

  static final class ViewKey {
    private final String dsl;
    private final Type type;

    ViewKey(String dsl, Type type) {
      this.dsl = dsl;
      this.type = type;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ViewKey viewKey = (ViewKey) o;
      return dsl.equals(viewKey.dsl) && type.equals(viewKey.type);
    }

    @Override
    public int hashCode() {
      return Objects.hash(dsl, type);
    }
  }

  /**
   * Implementation of Jsonb.Builder.
   */
  static final class DBuilder implements Jsonb.Builder {

    private final List<AdapterFactory> factories = new ArrayList<>();
    private boolean failOnUnknown;
    private boolean mathTypesAsString;
    private boolean serializeNulls;
    private boolean serializeEmpty = true;
    private JsonStream adapter;
    private BufferRecycleStrategy strategy = BufferRecycleStrategy.HYBRID_POOL;

    @Override
    public Builder serializeNulls(boolean serializeNulls) {
      this.serializeNulls = serializeNulls;
      return this;
    }

    @Override
    public Builder serializeEmpty(boolean serializeEmpty) {
      this.serializeEmpty = serializeEmpty;
      return this;
    }

    @Override
    public Builder failOnUnknown(boolean failOnUnknown) {
      this.failOnUnknown = failOnUnknown;
      return this;
    }

    @Override
    public Builder mathTypesAsString(boolean mathTypesAsString) {
      this.mathTypesAsString = mathTypesAsString;
      return this;
    }

    @Override
    public Builder bufferRecycling(BufferRecycleStrategy strategy) {
      this.strategy = strategy;
      return this;
    }

    @Override
    public Builder adapter(JsonStream streamAdapter) {
      this.adapter = streamAdapter;
      return this;
    }

    @Override
    public Builder add(Type type, AdapterBuilder builder) {
      return add(newAdapterFactory(type, builder));
    }

    @Override
    public <T> Builder add(Type type, JsonAdapter<T> jsonAdapter) {
      return add(newAdapterFactory(type, jsonAdapter));
    }

    @Override
    public <T> Builder add(Type type, Supplier<JsonAdapter<T>> jsonAdapter) {
      return add(newAdapterFactory(type, jsonAdapter));
    }

    @Override
    public Builder add(JsonbComponent component) {
      component.register(this);
      return this;
    }

    @Override
    public Builder add(AdapterFactory factory) {
      factories.add(factory);
      return this;
    }

    private void registerComponents() {
      // first register all user defined JsonbComponent
      for (JsonbComponent next : ExtensionLoader.userComponents()) {
        next.register(this);
      }
      for (GeneratedComponent next : ExtensionLoader.generatedComponents()) {
        next.register(this);
      }
    }

    @Override
    public DJsonb build() {
      registerComponents();
      return new DJsonb(adapter, factories, serializeNulls, serializeEmpty, failOnUnknown, mathTypesAsString, strategy);
    }

    static <T> AdapterFactory newAdapterFactory(Type type, JsonAdapter<T> jsonAdapter) {
      requireNonNull(type);
      requireNonNull(jsonAdapter);
      return (targetType, jsonb) -> simpleMatch(type, targetType) ? jsonAdapter : null;
    }

    static <T> AdapterFactory newAdapterFactory(Type type, Supplier<JsonAdapter<T>> jsonAdapter) {
      requireNonNull(type);
      requireNonNull(jsonAdapter);
      return (targetType, jsonb) -> simpleMatch(type, targetType) ? jsonAdapter.get() : null;
    }

    static AdapterFactory newAdapterFactory(Type type, AdapterBuilder builder) {
      requireNonNull(type);
      requireNonNull(builder);
      return (targetType, jsonb) -> simpleMatch(type, targetType) ? builder.build(jsonb).nullSafe() : null;
    }

    private static boolean simpleMatch(Type type, Type targetType) {
      return Util.typesMatch(type, targetType);
    }
  }
}
