package org.example.customer.iface.implementation;

import io.avaje.jsonb.Json;
import org.example.customer.iface.DIFace;
import org.example.customer.iface.EIFace;

@Json.Import(value = DIFace.class, implementation = MyDIFace.class)
@Json.Import(value = EIFace.class)
@Json
public record MyDIFace(String one, long two) implements DIFace {

  String banana() {
    return one;
  }
}
