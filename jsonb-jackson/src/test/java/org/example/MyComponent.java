package org.example;

import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.spi.GeneratedComponent;
import io.avaje.jsonb.spi.Generated;
import io.avaje.jsonb.spi.MetaData;

@Generated
@MetaData({CustomerJsonAdapter.class, ContactJsonAdapter.class})
public class MyComponent implements GeneratedComponent {

  @Override
  public void register(Jsonb.Builder builder) {
    builder.add(Customer.class, CustomerJsonAdapter::new);
    builder.add(Contact.class, ContactJsonAdapter::new);
    builder.add(Address.class, AddressJsonAdapter::new);
    builder.add(StreamBasicTest.MyBasic.class, MyBasicJsonAdapter::new);
  }
}
