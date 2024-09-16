package org.example.customer.inetaddress;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import static org.assertj.core.api.Assertions.assertThat;

class MyInetAddressTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<MyInetAddress> jsonType = jsonb.type(MyInetAddress.class);

  @Test
  void toJson_fromJson() throws IOException {

    MyInetAddress myInetAddress = new MyInetAddress(
      (Inet4Address) InetAddress.getByName("165.124.194.133"),
      (Inet6Address) InetAddress.getByName("1985:5b4d:9a9e:babc:5a1d:b44e:9942:07b0"),
      InetAddress.getByName("165.124.194.133"),
      InetAddress.getByName("1985:5b4d:9a9e:babc:5a1d:b44e:9942:07b0")
    );

    String asJson = jsonType.toJson(myInetAddress);
    MyInetAddress fromJson = jsonType.fromJson(asJson);

    assertThat(fromJson).isEqualTo(myInetAddress);
  }
}
