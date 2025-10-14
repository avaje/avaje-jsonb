package org.example.customer.creator;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.Json.Alias;
import io.avaje.jsonb.Json.Creator;
import io.avaje.jsonb.Json.Property;

@Json
public class Quaternion {

  // Scalar r in versor form
  @Property("W")
  private final double m_w;

  // Vector v in versor form
  @Property("X")
  private final double m_x;

  @Property("Y")
  private final double m_y;

  @Property("Z")
  private final double m_z;

  /** Constructs a quaternion with a default angle of 0 degrees. */
  public Quaternion() {
    m_w = 1.0;
    m_x = 0.0;
    m_y = 0.0;
    m_z = 0.0;
  }

  /**
   * Constructs a quaternion with the given components.
   *
   * @param w W component of the quaternion.
   * @param x X component of the quaternion.
   * @param y Y component of the quaternion.
   * @param z Z component of the quaternion.
   */
  @Creator
  public Quaternion(
      @Alias("W") double w,
      @Alias("X") double x,
      @Alias("Y") double y,
      @Alias("Z") double z) {
    m_w = w;
    m_x = x;
    m_y = y;
    m_z = z;
  }

  /**
   * Returns W component of the quaternion.
   *
   * @return W component of the quaternion.
   */
  public double getW() {
    return m_w;
  }

  /**
   * Returns X component of the quaternion.
   *
   * @return X component of the quaternion.
   */
  public double getX() {
    return m_x;
  }

  /**
   * Returns Y component of the quaternion.
   *
   * @return Y component of the quaternion.
   */
  public double getY() {
    return m_y;
  }

  /**
   * Returns Z component of the quaternion.
   *
   * @return Z component of the quaternion.
   */
  public double getZ() {
    return m_z;
  }
}
