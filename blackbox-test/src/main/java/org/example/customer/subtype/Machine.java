package org.example.customer.subtype;

import org.example.customer.subtype.Machine.V1;
import org.example.customer.subtype.Machine.V2;

import io.avaje.jsonb.Json;

@Json
@Json.SubType(type = V1.class)
@Json.SubType(type = V2.class)
public class Machine {
  public String name;

  @Json.Property("rank")
  public String style() {
    return "ULTRAKILL";
  }

  @Json.Property("bloodType")
  public void bloodType(String bloodType) {
    name = bloodType;
  }

  public static class V1 extends Machine {
    public String arm = "Feedbacker";

    @Json.Property("altFire")
    public void altFire(String altFire) {
      name = altFire;
    }
  }

  public static class V2 extends Machine {
    public String arm = "Knuckleblaster";

    @Json.Property("altFire")
    public void altFire(String altFire) {
      name = altFire;
    }
  }
}
