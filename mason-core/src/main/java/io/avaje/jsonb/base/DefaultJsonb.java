package io.avaje.jsonb.base;

import io.avaje.jsonb.*;
import io.avaje.jsonb.jackson.JacksonAdapter;
import io.avaje.jsonb.spi.IOAdapter;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

import static io.avaje.jsonb.base.Util.*;
import static java.util.Objects.requireNonNull;

/**
 * Default implementation of Jsonb.
 */
class DefaultJsonb implements Jsonb {

  private final DefaultAdapterBuilder builder;
  private final IOAdapter ioAdapter;

  DefaultJsonb(List<JsonAdapter.Factory> factories) {
    this.builder = new DefaultAdapterBuilder(this, factories);
    this.ioAdapter = new JacksonAdapter();
  }

  @Override
  public JsonWriter writer(Writer writer) throws IOException {
    return ioAdapter.writer(writer);
  }

  @Override
  public JsonWriter writer(OutputStream outputStream) throws IOException {
    return ioAdapter.writer(outputStream);
  }

  @Override
  public JsonReader reader(Reader reader) throws IOException {
    return ioAdapter.reader(reader);
  }

  @Override
  public JsonReader reader(InputStream inputStream) throws IOException {
    return ioAdapter.reader(inputStream);
  }

  @Override
  public JsonReader reader(String json) throws IOException {
    return ioAdapter.reader(json);
  }

  @Override
  public <T> JsonType<T> type(Class<T> cls) {
    return new SimpleJsonType<>(this, cls, adapter(cls));
  }

  @Override
  public <T> JsonType<T> type(Type type) {
    return new SimpleJsonType<>(this, type, adapter(type));
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

  <T> JsonType<List<T>> listOf(Type key, JsonAdapter<T> adapter) {
    Type listKey = Types.listOf(key);
    JsonAdapter<List<T>> listAdapter = builder.listOf(listKey, adapter);
    return new SimpleJsonType<>(this, listKey, listAdapter);
  }

  /**
   * Implementation of Jsonb.Builder.
   */
  static final class DBuilder implements Jsonb.Builder {

    final List<JsonAdapter.Factory> factories = new ArrayList<>();

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
//      for (Component component : ) {
//        component.register(this);
//      }
    }

    @Override
    public DefaultJsonb build() {
      registerComponents();
      return new DefaultJsonb(this.factories);
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
