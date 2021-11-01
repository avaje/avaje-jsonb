package org.example;

import io.avaje.mason.Jsonb;

public class MyComponent implements Jsonb.Component {

  @Override
  public void register(Jsonb.Builder builder) {
    builder.add(Customer.class, CustomerJsonAdapter::new);
    builder.add(Contact.class, ContactJsonAdapter::new);
  }
}
