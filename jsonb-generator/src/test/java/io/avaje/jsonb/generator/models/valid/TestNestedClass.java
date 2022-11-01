package io.avaje.jsonb.generator.models.valid;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TestNestedClass {

  @NotBlank public String s;

  public int i;

  @NotNull public Integer integer;

  public char ch;

  public Character chara;

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
}
