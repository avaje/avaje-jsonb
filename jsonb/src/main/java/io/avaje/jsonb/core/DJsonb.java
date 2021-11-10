package io.avaje.jsonb.core;

import io.avaje.jsonb.*;
import io.avaje.jsonb.jackson.JacksonAdapter;
import io.avaje.jsonb.spi.BufferedJsonWriter;
import io.avaje.jsonb.spi.IOAdapter;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.avaje.jsonb.core.Util.*;
import static java.util.Objects.requireNonNull;

/**
 * Default implementation of Jsonb.
 */
class DJsonb implements Jsonb {

  private final CoreAdapterBuilder builder;
  private final IOAdapter io;
  private final Map<Type, DJsonType<?>> typeCache = new ConcurrentHashMap<>();

  DJsonb(List<JsonAdapter.Factory> factories, boolean failOnUnknown) {
    this.builder = new CoreAdapterBuilder(this, factories);
    this.io = new JacksonAdapter(failOnUnknown); //TODO: Service load the ioAdapter implementation
  }

  BufferedJsonWriter bufferedWriter() throws IOException {
    return io.bufferedWriter();
  }

  @Override
  public JsonWriter writer(Writer writer) throws IOException {
    return io.writer(writer);
  }

  @Override
  public JsonWriter writer(OutputStream outputStream) throws IOException {
    return io.writer(outputStream);
  }

  @Override
  public JsonReader reader(Reader reader) throws IOException {
    return io.reader(reader);
  }

  @Override
  public JsonReader reader(InputStream inputStream) throws IOException {
    return io.reader(inputStream);
  }

  @Override
  public JsonReader reader(String json) throws IOException {
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

  @SuppressWarnings("unchecked")
  private <T> JsonType<T> typeWithCache(Type type) {
    return (JsonType<T>) typeCache.computeIfAbsent(type, _type -> new DJsonType<>(this, _type, adapter(_type)));
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
    return adapter(type, Collections.emptySet());
  }

  @Override
  public <T> JsonAdapter<T> adapter(Type type, Set<? extends Annotation> annotations) {
    type = removeSubtypeWildcard(canonicalize(requireNonNull(type)));
    Object cacheKey = cacheKey(type, requireNonNull(annotations));
    JsonAdapter<T> result = builder.get(cacheKey);
    if (result != null) {
      return result;
    }
    return builder.build(type, annotations, cacheKey);
  }

  /**
   * Returns an opaque object that's equal if the type and annotations are equal.
   */
  private Object cacheKey(Type type, Set<? extends Annotation> annotations) {
    if (annotations.isEmpty()) {
      return type;
    }
    return Arrays.asList(type, annotations);
  }

  JsonReader objectReader(Object value) {
    return new ObjectJsonReader(value);
  }

  /**
   * Implementation of Jsonb.Builder.
   */
  static final class DBuilder implements Jsonb.Builder {

    private final List<JsonAdapter.Factory> factories = new ArrayList<>();
    private boolean failOnUnknown;

    @Override
    public Builder failOnUnknown(boolean failOnUnknown) {
      this.failOnUnknown = failOnUnknown;
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
    public <T> Builder add(Type type, Class<? extends Annotation> annotation, JsonAdapter<T> jsonAdapter) {
      return add(newAdapterFactory(type, annotation, jsonAdapter));
    }

    @Override
    public Builder add(JsonAdapter.Factory factory) {
      factories.add(factory);
      return this;
    }

    private void registerComponents() {
      ServiceLoader<Component> load = ServiceLoader.load(Component.class);
      for (Component next : load) {
        next.register(this);
      }
    }

    @Override
    public DJsonb build() {
      registerComponents();
      return new DJsonb(factories, failOnUnknown);
    }

    static <T> JsonAdapter.Factory newAdapterFactory(Type type, JsonAdapter<T> jsonAdapter) {
      requireNonNull(type);
      requireNonNull(jsonAdapter);
      return (targetType, annotations, jsonb) -> simpleMatch(annotations, type, targetType) ? jsonAdapter : null;
    }

    static <T> JsonAdapter.Factory newAdapterFactory(Type type, AdapterBuilder builder) {
      requireNonNull(type);
      requireNonNull(builder);
      return (targetType, annotations, jsonb) -> simpleMatch(annotations, type, targetType) ? builder.build(jsonb) : null;
    }

    static <T> JsonAdapter.Factory newAdapterFactory(Type type, Class<? extends Annotation> annotation, JsonAdapter<T> jsonAdapter) {
      requireNonNull(type);
      requireNonNull(annotation);
      requireNonNull(jsonAdapter);
      if (!annotation.isAnnotationPresent(JsonQualifier.class)) {
        throw new IllegalArgumentException(annotation + " does not have @JsonQualifier");
      }
      if (annotation.getDeclaredMethods().length > 0) {
        throw new IllegalArgumentException("Use JsonAdapter.Factory for annotations with elements");
      }

      return (targetType, annotations, moshi) -> {
        if (isMatch(type, annotation, targetType, annotations)) {
          return jsonAdapter;
        }
        return null;
      };
    }

    private static boolean isMatch(Type type, Class<? extends Annotation> annotation, Type targetType, Set<? extends Annotation> annotations) {
      return Util.typesMatch(type, targetType)
        && annotations.size() == 1
        && Util.isAnnotationPresent(annotations, annotation);
    }

  }

  private static boolean simpleMatch(Set<? extends Annotation> annotations2, Type type2, Type targetType2) {
    return annotations2.isEmpty() && Util.typesMatch(type2, targetType2);
  }
}
