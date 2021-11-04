package org.example.other.place;

public class MyOtherClass {

  int some;
  final String thing;

  public MyOtherClass(String thing) {
    this.thing = thing;
  }

  public int some() {
    return some;
  }

  public MyOtherClass some(int some) {
    this.some = some;
    return this;
  }

  public String thing() {
    return thing;
  }
}
