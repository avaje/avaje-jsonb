package org.example.jmh;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.example.jmh.model.UriData;
import org.example.jmh.model.UrlData;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Test for serializing a pojo with 10 properties
 */
@BenchmarkMode(Mode.Throughput)
@Timeout(time = 20)
@State(Scope.Benchmark)
@Warmup(iterations = 3)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class UrlTest {

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final Jsonb jsonb = Jsonb.newBuilder().build();
  private static final JsonType<UrlData> jsonbUrlType = jsonb.type(UrlData.class);
  private static final JsonType<UriData> jsonbUriType = jsonb.type(UriData.class);

  private UrlData urlTestData;
  private String urlContent;
  private UriData uriTestData;
  private String uriContent;

  @Setup
  public void setup() throws MalformedURLException {
    urlTestData = new UrlData(new URL("https://foo.com"), new URL("https://bar.com") );
    urlContent = "{\"one\":\"https://foo.com\",\"two\":\"https://bar.com\"}";
    uriTestData = new UriData(URI.create("https://foo.com"), URI.create("https://bar.com") );
    uriContent = "{\"one\":\"https://foo.com\",\"two\":\"https://bar.com\"}";
  }

  @Benchmark
  public String url_toJson_objectMapper() {
    try {
      return mapper.writeValueAsString(urlTestData);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Benchmark
  public String url_toJson_jsonb() {
    return jsonbUrlType.toJson(urlTestData);
  }

  @Benchmark
  public UrlData url_fromJson_objectMapper() {
    try {
      return mapper.readValue(urlContent, UrlData.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Benchmark
  public UrlData url_fromJson_jsonb() {
    return jsonbUrlType.fromJson(urlContent);
  }


  @Benchmark
  public String uri_toJson_objectMapper() {
    try {
      return mapper.writeValueAsString(uriTestData);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Benchmark
  public String uri_toJson_jsonb() {
    return jsonbUriType.toJson(uriTestData);
  }

  @Benchmark
  public UriData uri_fromJson_objectMapper() {
    try {
      return mapper.readValue(uriContent, UriData.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Benchmark
  public UriData uri_fromJson_jsonb() {
    return jsonbUriType.fromJson(uriContent);
  }

  public static void main(String[] args) throws MalformedURLException {
    UrlTest test = new UrlTest();
    test.setup();

    String j1 = test.url_toJson_objectMapper();
    String j2 = test.url_toJson_jsonb();
    UrlData url1 = test.url_fromJson_objectMapper();
    UrlData url2 = test.url_fromJson_jsonb();

    String j3 = test.uri_toJson_objectMapper();
    String j4 = test.uri_toJson_jsonb();
    UriData uri1 = test.uri_fromJson_objectMapper();
    UriData uri2 = test.uri_fromJson_jsonb();

    System.out.println("done");
  }

}
