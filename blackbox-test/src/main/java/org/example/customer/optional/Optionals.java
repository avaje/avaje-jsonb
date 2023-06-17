package org.example.customer.optional;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import io.avaje.jsonb.Json;

@Json
public class Optionals {

  Optional<String> stringyString = Optional.empty();
  OptionalInt intOp = OptionalInt.empty();
  OptionalDouble doubleOp = OptionalDouble.empty();
  OptionalLong longOp = OptionalLong.empty();

  public Optional<String> getStringyString() {
    return stringyString;
  }

  public void setStringyString(Optional<String> stringyString) {
    this.stringyString = stringyString;
  }

  public void setStringyString(String stringyString) {
    this.stringyString = Optional.ofNullable(stringyString);
  }

  public OptionalInt getIntOp() {
    return intOp;
  }

  public void setIntOp(OptionalInt intOp) {
    this.intOp = intOp;
  }

  public void setIntOp(int intOp) {
    this.intOp = OptionalInt.of(intOp);
  }

  public OptionalDouble getDoubleOp() {
    return doubleOp;
  }

  public void setDoubleOp(OptionalDouble doubleOp) {
    this.doubleOp = doubleOp;
  }

  public void setDoubleOp(double doubleOp) {
    this.doubleOp = OptionalDouble.of(doubleOp);
  }

  public OptionalLong getLongOp() {
    return longOp;
  }

  public void setLongOp(OptionalLong longOp) {
    this.longOp = longOp;
  }

  public void setLongOp(long longOp) {
    this.longOp = OptionalLong.of(longOp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(doubleOp, intOp, longOp, stringyString);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if ((obj == null) || (getClass() != obj.getClass())) return false;
    final Optionals other = (Optionals) obj;
    return Objects.equals(doubleOp, other.doubleOp)
        && Objects.equals(intOp, other.intOp)
        && Objects.equals(longOp, other.longOp)
        && Objects.equals(stringyString, other.stringyString);
  }
}
