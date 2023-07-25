package io.avaje.jsonb.generator.models.valid;

import java.util.List;
import java.util.Optional;

import io.avaje.jsonb.Json;

@Json
public class TestClass {

  @Json.Alias({"something", "something2"})
  private String alias;

  private String s;

  private int i;

  private Integer integer;

  private char ch;

  private Character chara;

  private List<String> list;

  private Optional<String> op;

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

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

  public List<String> getList() {
    return list;
  }

  public void setList(List<String> list) {
    this.list = list;
  }

  public Optional<String> getOp() {
    return op;
  }

  public void setOp(Optional<String> op) {
    this.op = op;
  }
}
