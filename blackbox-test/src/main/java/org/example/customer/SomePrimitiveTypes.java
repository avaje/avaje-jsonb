package org.example.customer;

import io.avaje.jsonb.Json;

@Json
public class SomePrimitiveTypes {

  private final int f0;
  private final long f1;
  private final boolean f2;
  private final double f3;
  private final char f4;
  private final byte f5;
  private final short f6;

  private Integer g0;
  private Long g1;
  private Boolean g2;
  private Double g3;
  private Character g4;
  private Byte g5;
  private Short g6;

  public SomePrimitiveTypes(int f0, long f1, boolean f2, double f3, char f4, byte f5, short f6) {
    this.f0 = f0;
    this.f1 = f1;
    this.f2 = f2;
    this.f3 = f3;
    this.f4 = f4;
    this.f5 = f5;
    this.f6 = f6;
  }

  public int getF0() {
    return f0;
  }

  public long getF1() {
    return f1;
  }

  public boolean isF2() {
    return f2;
  }

  public double getF3() {
    return f3;
  }

  public char getF4() {
    return f4;
  }

  public byte getF5() {
    return f5;
  }

  public short getF6() {
    return f6;
  }

  public Integer getG0() {
    return g0;
  }

  public void setG0(Integer g0) {
    this.g0 = g0;
  }

  public Long getG1() {
    return g1;
  }

  public void setG1(Long g1) {
    this.g1 = g1;
  }

  public Boolean getG2() {
    return g2;
  }

  public void setG2(Boolean g2) {
    this.g2 = g2;
  }

  public Double getG3() {
    return g3;
  }

  public void setG3(Double g3) {
    this.g3 = g3;
  }

  public Character getG4() {
    return g4;
  }

  public void setG4(Character g4) {
    this.g4 = g4;
  }

  public Byte getG5() {
    return g5;
  }

  public void setG5(Byte g5) {
    this.g5 = g5;
  }

  public Short getG6() {
    return g6;
  }

  public void setG6(Short g6) {
    this.g6 = g6;
  }
}
