package io.avaje.jsonb.core;

import io.avaje.json.*;
import io.avaje.jsonb.*;
import io.avaje.json.stream.*;
import io.avaje.json.view.ViewBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.*;

final class CoreViewBuilder implements ViewBuilder {

  private final MethodHandles.Lookup lookup = MethodHandles.lookup();
  private final Deque<Items> stack = new ArrayDeque<>();
  private final ViewDsl viewDsl;
  private final Names names;
  private Items current;
  private Element resultElement;

  CoreViewBuilder(ViewDsl viewDsl) {
    this.viewDsl = viewDsl;
    this.names = new Names();
  }

  private CoreViewBuilder(ViewDsl viewDsl, Names names) {
    this.viewDsl = viewDsl;
    this.names = names;
  }

  @Override
  public MethodHandle method(Class<?> cls, String methodName, Class<?> returnType) {
    try {
      return lookup.findVirtual(cls, methodName, MethodType.methodType(returnType));
    } catch (Exception e) {
      throw new JsonException(e);
    }
  }

  @Override
  public MethodHandle field(Class<?> cls, String name) {
    try {
      Field field = cls.getDeclaredField(name);
      return lookup.unreflectGetter(field);
    } catch (Exception e) {
      throw new JsonException(e);
    }
  }

  private void push(String name, MethodHandle mh) {
    current = new Items(names, name, mh);
    stack.push(current);
  }

  private void pop() {
    Items items = stack.pop();
    Element element = items.build();
    if (stack.isEmpty()) {
      resultElement = element;
    } else {
      current = stack.peek();
      current.add(element);
    }
  }

  @Override
  public void add(String name, JsonAdapter<?> adapter, MethodHandle methodHandle) {
    if (viewDsl.contains(name)) {
      if (adapter.isViewBuilderAware()) {
        viewDsl.push(name);
        adapter.viewBuild().build(this, name, methodHandle);
        viewDsl.pop();
      } else {
        current.add(new Scalar(names.add(name), adapter, methodHandle));
      }
    }
  }

  @Override
  public void beginObject(String name, MethodHandle methodHandle) {
    push(name, methodHandle);
  }

  @Override
  public void endObject() {
    pop();
  }

  @Override
  public void addArray(String name, JsonAdapter<?> adapter, MethodHandle methodHandle) {
    try {
      CoreViewBuilder nested = new CoreViewBuilder(viewDsl, names);
      adapter.viewBuild().build(nested);
      JsonView<Object> nestedView = nested.build();
      if (name == null) {
        if (current != null) {
          throw new IllegalStateException();
        }
        resultElement = new CollectionElement(nestedView);
      } else {
        current.add(new NestedCollection(nestedView, names.add(name), methodHandle));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  <T> JsonView<T> build(DJsonb jsonb) {
    return new DView<>(jsonb, resultElement, jsonb.properties(names.properties()));
  }

  /**
   * Build internal nested view.
   */
  <T> JsonView<T> build() {
    return new DView<>(resultElement);
  }

  interface Element {

    void write(JsonWriter writer, Object object) throws IOException;
  }

  static final class Names {

    private final List<String> names = new ArrayList<>();
    private int namePosition = -1;

    int add(String name) {
      names.add(name);
      return ++namePosition;
    }

    String[] properties() {
      return names.toArray(new String[0]);
    }
  }

  static final class Items {
    private final Names names;
    private final String name;
    private final MethodHandle methodHandle;
    private final List<Element> items = new ArrayList<>();

    Items(Names names, String name, MethodHandle mh) {
      this.names = names;
      this.name = name;
      this.methodHandle = mh;
    }

    void add(Element element) {
      items.add(element);
    }

    Element build() {
      if (name == null) {
        return new ObjectElement(items);
      } else {
        return new NestedObject(items, names.add(name), methodHandle);
      }
    }
  }

  private static final class DView<T> implements JsonView<T> {

    private final DJsonb jsonb;
    private final PropertyNames properties;
    private final Element element;

    /**
     * Create top level view.
     */
    DView(DJsonb jsonb, Element element, PropertyNames properties) {
      this.jsonb = jsonb;
      this.element = element;
      this.properties = properties;
    }

    /**
     * Create nested view.
     */
    DView(Element element) {
      this.element = element;
      this.jsonb = null;
      this.properties = null;
    }

    @Override
    public String toJson(T value) {
      try (BufferedJsonWriter writer = jsonb.bufferedWriter()) {
        toJson(value, writer);
        return writer.result();
      }
    }

    @Override
    public String toJsonPretty(T value) {
      try (BufferedJsonWriter writer = jsonb.bufferedWriter()) {
        writer.pretty(true);
        toJson(value, writer);
        return writer.result();
      }
    }

    @Override
    public byte[] toJsonBytes(T value) {
      try (BytesJsonWriter writer = jsonb.bufferedWriterAsBytes()) {
        toJson(value, writer);
        return writer.result();
      }
    }

    @Override
    public void toJson(T value, JsonWriter writer) {
      if (properties != null) {
        writer.allNames(properties);
      }
      try {
        element.write(writer, value);
      } catch (IOException e) {
        throw new JsonIoException(e);
      }
    }

    @Override
    public void toJson(T value, Writer writer) {
      try (JsonWriter jsonWriter = jsonb.writer(writer)) {
        toJson(value, jsonWriter);
      }
    }

    @Override
    public void toJson(T value, OutputStream outputStream) {
      try (JsonWriter writer = jsonb.writer(outputStream)) {
        toJson(value, writer);
      }
    }

    @Override
    public void toJson(T value, JsonOutput output) {
      try (JsonWriter writer = jsonb.writer(output)) {
        toJson(value, writer);
      }
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static final class Scalar implements Element {

    private final int namePosition;
    private final JsonAdapter adapter;
    private final MethodHandle methodHandle;

    Scalar(int namePosition, JsonAdapter adapter, MethodHandle methodHandle) {
      this.namePosition = namePosition;
      this.adapter = adapter;
      this.methodHandle = methodHandle;
    }

    @Override
    public void write(JsonWriter writer, Object object) {
      try {
        writer.name(namePosition);
        adapter.toJson(writer, methodHandle.invoke(object));
      } catch (Throwable e) {
        throw JsonException.of(e);
      }
    }
  }

  private static final class ObjectElement implements Element {

    private final Element[] elements;

    ObjectElement(List<Element> elements) {
      this.elements = elements.toArray(new Element[0]);
    }

    @Override
    public void write(JsonWriter writer, Object object) {
      try {
        writer.beginObject();
        for (final Element element : elements) {
          element.write(writer, object);
        }
        writer.endObject();
      } catch (IOException e) {
        throw new JsonIoException(e);
      }
    }
  }

  private static final class NestedObject implements Element {

    private final int namePosition;
    private final MethodHandle methodHandle;
    private final Element[] elements;

    NestedObject(List<Element> elements, int namePosition, MethodHandle methodHandle) {
      this.namePosition = namePosition;
      this.methodHandle = methodHandle;
      this.elements = elements.toArray(new Element[0]);
    }

    @Override
    public void write(JsonWriter writer, Object object) {
      try {
        writer.name(namePosition);
        writer.beginObject();
        final Object nested = methodHandle.invoke(object);
        for (final Element element : elements) {
          element.write(writer, nested);
        }
        writer.endObject();
      } catch (Throwable e) {
        throw JsonException.of(e);
      }
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static final class CollectionElement implements Element {

    private final JsonView child;

    CollectionElement(JsonView child) {
      this.child = child;
    }

    @Override
    public void write(JsonWriter writer, Object object) {
      final Collection<?> collection = (Collection<?>) object;
      if (collection.isEmpty()) {
        writer.emptyArray();
      } else {
        writer.beginArray();
        for (final Object value : collection) {
          child.toJson(value, writer);
        }
        writer.endArray();
      }
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static final class NestedCollection implements Element {

    private final JsonView child;
    private final int namePosition;
    private final MethodHandle methodHandle;

    NestedCollection(JsonView child, int namePosition, MethodHandle methodHandle) {
      this.child = child;
      this.namePosition = namePosition;
      this.methodHandle = methodHandle;
    }

    @Override
    public void write(JsonWriter writer, Object object) {
      try {
        writer.name(namePosition);
        final Collection<?> collection = (Collection<?>) methodHandle.invoke(object);
        if (collection.isEmpty()) {
          writer.emptyArray();
        } else {
          writer.beginArray();
          for (final Object value : collection) {
            child.toJson(value, writer);
          }
          writer.endArray();
        }
      } catch (Throwable e) {
        throw JsonException.of(e);
      }
    }
  }
}
