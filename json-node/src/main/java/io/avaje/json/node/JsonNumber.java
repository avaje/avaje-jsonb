package io.avaje.json.node;

import io.avaje.json.JsonWriter;

import java.math.BigDecimal;

/**
 * JsonNode Number type.
 */
public /*sealed*/ interface JsonNumber extends JsonNode
  /*permits JsonInteger, JsonLong, JsonDouble, JsonDecimal*/ {

  /**
   * Return the int value for the number.
   */
  int intValue();

  /**
   * Return the long value for the number.
   */
  long longValue();

  /**
   * Return the double value for the number.
   */
  double doubleValue();

  /**
   * Return the decimal value for the number.
   */
  BigDecimal decimalValue();

  /**
   * Return the number.
   */
  Number numberValue();

  /**
   * Write the value of this node to the writer.
   */
  void toJson(JsonWriter writer);
}
