package io.avaje.jsonb.jackson;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

/**
 * Stack based on ArrayList.
 */
final class ArrayStack<E> {

  private final List<E> list;

  /**
   * Creates an empty Stack.
   */
  ArrayStack() {
    this.list = new ArrayList<>();
  }

  @Override
  public String toString() {
    return list.toString();
  }

  /**
   * Pushes an item onto the top of this stack.
   */
  void push(E item) {
    list.add(item);
  }

  /**
   * Removes the object at the top of this stack or null if it's empty.
   */
  E pop() {
    int len = list.size();
    return len == 0 ? null : list.remove(len - 1);
  }

}
