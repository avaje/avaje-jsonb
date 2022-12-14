package org.example.customer.generics;

import io.avaje.jsonb.Json;

@Json
public class MyGenericHolder<T> {

  String title;
  String author;
  T document;

  public String getTitle() {
    return title;
  }

  public MyGenericHolder<T> setTitle(String title) {
    this.title = title;
    return this;
  }

  public String getAuthor() {
    return author;
  }

  public MyGenericHolder<T> setAuthor(String author) {
    this.author = author;
    return this;
  }

  public T getDocument() {
    return document;
  }

  public MyGenericHolder<T> setDocument(T document) {
    this.document = document;
    return this;
  }
}
