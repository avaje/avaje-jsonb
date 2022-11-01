package io.avaje.jsonb.generator.models.valid;

import java.util.List;

import io.avaje.jsonb.Json;

@Json
public class TestClass {

  private String s;

  private int i;

  private Integer integer;

  private char ch;

  private Character chara;

  private List<String> list;

  private TestNestedClass nested;

  public String getS() {
    return s;
  }

  public void setS(String s) {
    this.s = s;
  }

  public int getI() {
    return i;
  }

  public void setI(int i) {
    this.i = i;
  }

  public Integer getInteger() {
    return integer;
  }

  public void setInteger(Integer integer) {
    this.integer = integer;
  }

  public char getCh() {
    return ch;
  }

  public void setCh(char ch) {
    this.ch = ch;
  }

  public Character getChara() {
    return chara;
  }

  public void setChara(Character chara) {
    this.chara = chara;
  }

  public TestNestedClass getNested() {
    return nested;
  }

  public void setNested(TestNestedClass nested) {
    this.nested = nested;
  }

public List<String> getList(){return list;}

public void setList(List<String> list){this.list = list;}
}
