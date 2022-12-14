package org.example.customer.generics;

import io.avaje.jsonb.Json;

import java.util.List;

@Json
public class MyGenericPageResult<T> {

  int page;
  int pageSize;
  int totalPageCount;
  List<? extends T> results;

  public int getPage() {
    return page;
  }

  public MyGenericPageResult<T> setPage(int page) {
    this.page = page;
    return this;
  }

  public int getPageSize() {
    return pageSize;
  }

  public MyGenericPageResult<T> setPageSize(int pageSize) {
    this.pageSize = pageSize;
    return this;
  }

  public int getTotalPageCount() {
    return totalPageCount;
  }

  public MyGenericPageResult<T> setTotalPageCount(int totalPageCount) {
    this.totalPageCount = totalPageCount;
    return this;
  }

  public List<? extends T> getResults() {
    return results;
  }

  public MyGenericPageResult<T> setResults(List<? extends T> results) {
    this.results = results;
    return this;
  }
}
