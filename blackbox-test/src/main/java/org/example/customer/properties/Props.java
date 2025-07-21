package org.example.customer.properties;

import io.avaje.jsonb.Json;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Properties;

@Json
public record Props(Properties props) {}
