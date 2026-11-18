package io.avaje.jsonb.core;

import io.avaje.json.*;
import io.avaje.jsonb.*;
import io.avaje.json.stream.*;
import io.avaje.json.view.ViewBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.*;
import java.util.function.UnaryOperator;

final class CoreViewBuilder implements ViewBuilder {

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

  private void push(String name, UnaryOperator<Object> accessor) {
    current = new Items(names, name, accessor);
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
  public void add(String name, JsonAdapter<?> adapter, UnaryOperator<Object> accessor) {
    if (viewDsl.contains(name)) {
      if (adapter.isViewBuilderAware()) {
        viewDsl.push(name);
        adapter.viewBuild().build(this, name, accessor);
        viewDsl.pop();
      } else {
        current.add(new Scalar(names.add(name), adapter, accessor));
      }
    }
  }

  @Override
  public void beginObject(String name, UnaryOperator<Object> accessor) {
    push(name, accessor);
  }

  @Override
  public void endObject() {
    pop();
  }

  @Override
  public void addArray(String name, JsonAdapter<?> adapter, UnaryOperator<Object> accessor) {
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
        current.add(new NestedCollection(nestedView, names.add(name), accessor));
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
    private final UnaryOperator<Object> accessor;
    private final List<Element> items = new ArrayList<>();

    Items(Names names, String name, UnaryOperator<Object> accessor) {
      this.names = names;
      this.name = name;
      this.accessor = accessor;
    }

    void add(Element element) {
      items.add(element);
    }

    Element build() {
      if (name == null) {
        return new ObjectElement(items);
      } else {
        return new NestedObject(items, names.add(name), accessor);
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
    private final UnaryOperator<Object> accessor;

    Scalar(int namePosition, JsonAdapter adapter, UnaryOperator<Object> accessor) {
      this.namePosition = namePosition;
      this.adapter = adapter;
      this.accessor = accessor;
    }

    @Override
    public void write(JsonWriter writer, Object object) {
      writer.name(namePosition);
      adapter.toJson(writer, accessor.apply(object));
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
    private final UnaryOperator<Object> accessor;
    private final Element[] elements;

    NestedObject(List<Element> elements, int namePosition, UnaryOperator<Object> accessor) {
      this.namePosition = namePosition;
      this.accessor = accessor;
      this.elements = elements.toArray(new Element[0]);
    }

    @Override
    public void write(JsonWriter writer, Object object) {
      try {
        writer.name(namePosition);
        writer.beginObject();
        final var nested = accessor.apply(object);
        for (final var element : elements) {
          element.write(writer, nested);
        }
        writer.endObject();
      } catch (IOException e) {
        throw new JsonIoException(e);
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
      final var collection = (Collection<?>) object;
      if (collection.isEmpty()) {
        writer.emptyArray();
      } else {
        writer.beginArray();
        for (final var value : collection) {
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
    private final UnaryOperator<Object> accessor;

    NestedCollection(JsonView child, int namePosition, UnaryOperator<Object> accessor) {
      this.child = child;
      this.namePosition = namePosition;
      this.accessor = accessor;
    }

    @Override
    public void write(JsonWriter writer, Object object) {
      writer.name(namePosition);
      final var collection = (Collection<?>) accessor.apply(object);
      if (collection.isEmpty()) {
        writer.emptyArray();
      } else {
        writer.beginArray();
        for (final var value : collection) {
          child.toJson(value, writer);
        }
        writer.endArray();
      }
    }
  }
}
