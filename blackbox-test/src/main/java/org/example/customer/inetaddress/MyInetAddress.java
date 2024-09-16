package org.example.customer.inetaddress;

import io.avaje.jsonb.Json;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

@Json
public record MyInetAddress(
  Inet4Address ipv4,
  Inet6Address ipv6,
  InetAddress ipv4Generic,
  InetAddress ipv6Generic
) {
}
