package org.example.customer.iface;

import io.avaje.jsonb.Json;

@Json
public interface BIFace {

  String getOne();

  boolean isTwo();

  String getThree();

  String four();

}
