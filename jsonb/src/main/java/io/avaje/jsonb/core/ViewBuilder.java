package io.avaje.jsonb.core;

import io.avaje.jsonb.JsonAdapter;
import io.avaje.jsonb.JsonException;
import io.avaje.jsonb.JsonView;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.spi.BufferedJsonWriter;
import io.avaje.jsonb.spi.ViewBuilderAware;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

final class ViewBuilder implements io.avaje.jsonb.spi.ViewBuilder {

  private final MethodHandles.Lookup lookup = MethodHandles.lookup();
  private final Stack<Items> stack = new Stack<>();
  private final ViewDsl viewDsl;
  private Items current;
  private Element resultElement;

  ViewBuilder(ViewDsl viewDsl) {
    this.viewDsl = viewDsl;
  }

  @Override
  public MethodHandle method(Class<?> cls, String methodName, Class<?> returnType) {
    try {
      return lookup.findVirtual(cls, methodName, MethodType.methodType(returnType));
    } catch (Exception e) {
      throw new JsonException(e);
    }
  }

  private void push(String name, MethodHandle mh) {
    current = new Items(name, mh);
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
    try {
      if (viewDsl.contains(name)) {
        if (adapter.isViewBuilderAware()) {
          ViewBuilderAware nested = adapter.viewBuild();
          viewDsl.push(name);
          nested.build(this, name, methodHandle);
          viewDsl.pop();
        } else {
          current.add(new Scalar(name, adapter, methodHandle));
        }
      }
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new IllegalStateException(e);
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
      ViewBuilder nested = new ViewBuilder(viewDsl);
      adapter.viewBuild().build(nested);
      JsonView<Object> nestedView = nested.build();
      if (name == null) {
        if (current != null) {
          throw new IllegalStateException();
        }
        resultElement = new CollectionElement(nestedView);
      } else {
        current.add(new NestedCollection(nestedView, name, methodHandle));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  <T> JsonView<T> build(DJsonb jsonb) {
    return new DView<>(jsonb, resultElement);
  }

  <T> JsonView<T> build() {
    return new DView<>(null, resultElement);
  }

  interface Element {

    void write(JsonWriter writer, Object object) throws IOException;
  }

  static final class Items {
    private final String name;
    private final MethodHandle methodHandle;
    private final List<Element> items = new ArrayList<>();

    Items(String name, MethodHandle mh) {
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
        return new NestedObject(items, name, methodHandle);
      }
    }
  }

  private static final class DView<T> implements JsonView<T> {

    private final DJsonb jsonb;
    private final Element element;

    DView(DJsonb jsonb, Element element) {
      this.jsonb = jsonb;
      this.element = element;
    }

    @Override
    public String toJson(T value) {
      try {
        BufferedJsonWriter bufferedJsonWriter = jsonb.bufferedWriter();
        toJson(bufferedJsonWriter, value);
        return bufferedJsonWriter.result();
      } catch (Throwable e) {
        throw JsonException.of(e);
      }
    }

    @Override
    public void toJson(JsonWriter writer, T value) {
      try {
        element.write(writer, value);
      } catch (Throwable e) {
        throw JsonException.of(e);
      }
    }

    @Override
    public void toJson(Writer writer, T value) {
      try {
        element.write(jsonb.writer(writer), value);
      } catch (Throwable e) {
        throw JsonException.of(e);
      }
    }

    @Override
    public void toJson(OutputStream outputStream, T value) {
      try {
        element.write(jsonb.writer(outputStream), value);
      } catch (Throwable e) {
        throw JsonException.of(e);
      }
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static final class Scalar implements Element {

    private final String name;
    private final JsonAdapter adapter;
    private final MethodHandle methodHandle;

    Scalar(String name, JsonAdapter adapter, MethodHandle methodHandle) {
      this.name = name;
      this.adapter = adapter;
      this.methodHandle = methodHandle;
    }

    @Override
    public void write(JsonWriter writer, Object object) {
      try {
        writer.name(name);
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
      } catch (Throwable e) {
        throw JsonException.of(e);
      }
    }
  }

  private static final class NestedObject implements Element {

    private final String name;
    private final MethodHandle methodHandle;
    private final Element[] elements;

    NestedObject(List<Element> elements, String name, MethodHandle methodHandle) {
      this.name = name;
      this.methodHandle = methodHandle;
      this.elements = elements.toArray(new Element[0]);
    }

    @Override
    public void write(JsonWriter writer, Object object) {
      try {
        writer.name(name);
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
      try {
        final Collection<?> collection = (Collection<?>) object;
        if (collection.isEmpty()) {
          writer.emptyArray();
        } else {
          writer.beginArray();
          for (final Object value : collection) {
            child.toJson(writer, value);
          }
          writer.endArray();
        }
      } catch (Throwable e) {
        throw JsonException.of(e);
      }
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static final class NestedCollection implements Element {

    private final JsonView child;
    private final String name;
    private final MethodHandle methodHandle;

    NestedCollection(JsonView child, String name, MethodHandle methodHandle) {
      this.child = child;
      this.name = name;
      this.methodHandle = methodHandle;
    }

    @Override
    public void write(JsonWriter writer, Object object) {
      try {
        writer.name(name);
        final Collection<?> collection = (Collection<?>) methodHandle.invoke(object);
        if (collection.isEmpty()) {
          writer.emptyArray();
        } else {
          writer.beginArray();
          for (final Object value : collection) {
            child.toJson(writer, value);
          }
          writer.endArray();
        }
      } catch (Throwable e) {
        throw JsonException.of(e);
      }
    }
  }
}
