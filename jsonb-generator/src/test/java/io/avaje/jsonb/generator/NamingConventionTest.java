package io.avaje.jsonb.generator;

import io.avaje.jsonb.Json;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NamingConventionTest {

  @Test
  void match_from() {
    NamingConvention match = NamingConvention.of(Json.Naming.Match);

    assertEquals("abcde", match.from("abcde"));
    assertEquals("aURL", match.from("aURL"));
    assertEquals("myURL", match.from("myURL"));
  }

  @Test
  void lowerHyphen_from() {
    NamingConvention match = NamingConvention.of(Json.Naming.LowerHyphen);

    assertEquals("ab-cde", match.from("abCde"));
    assertEquals("ab-cd-e", match.from("abCdE"));
    assertEquals("ab-cde", match.from("abCDe"));
    assertEquals("a-url", match.from("aURL"));
    assertEquals("my-url", match.from("myURL"));
  }

  @Test
  void lowerUnderscore_from() {
    NamingConvention match = NamingConvention.of(Json.Naming.LowerUnderscore);

    assertEquals("ab_cde", match.from("abCde"));
    assertEquals("ab_cd_e", match.from("abCdE"));
    assertEquals("ab_cde", match.from("abCDe"));
    assertEquals("a_url", match.from("aURL"));
    assertEquals("my_url", match.from("myURL"));
  }

  @Test
  void lowerSpace_from() {
    NamingConvention match = NamingConvention.of(Json.Naming.LowerSpace);

    assertEquals("ab cde", match.from("abCde"));
    assertEquals("ab cd e", match.from("abCdE"));
    assertEquals("ab cde", match.from("abCDe"));
    assertEquals("a url", match.from("aURL"));
    assertEquals("my url", match.from("myURL"));
  }

  @Test
  void upperSpace_from() {
    NamingConvention match = NamingConvention.of(Json.Naming.UpperSpace);

    assertEquals("ABCDE", match.from("abcde"));
    assertEquals("AB CDE", match.from("abCde"));
    assertEquals("AB CD E", match.from("abCdE"));
    assertEquals("AB CDE", match.from("abCDe"));
    assertEquals("A URL", match.from("aURL"));
    assertEquals("MY URL", match.from("myURL"));
  }

  @Test
  void upperUnderscore_from() {
    NamingConvention match = NamingConvention.of(Json.Naming.UpperUnderscore);

    assertEquals("ABCDE", match.from("abcde"));
    assertEquals("AB_CDE", match.from("abCde"));
    assertEquals("AB_CD_E", match.from("abCdE"));
    assertEquals("AB_CDE", match.from("abCDe"));
    assertEquals("A_URL", match.from("aURL"));
    assertEquals("MY_URL", match.from("myURL"));
  }

  @Test
  void upperHyphen_from() {
    NamingConvention match = NamingConvention.of(Json.Naming.UpperHyphen);

    assertEquals("ABCDE", match.from("abcde"));
    assertEquals("AB-CDE", match.from("abCde"));
    assertEquals("AB-CD-E", match.from("abCdE"));
    assertEquals("AB-CDE", match.from("abCDe"));
    assertEquals("A-URL", match.from("aURL"));
    assertEquals("MY-URL", match.from("myURL"));
  }

  @Test
  void upperCamel_from() {
    NamingConvention match = NamingConvention.of(Json.Naming.UpperCamel);

    assertEquals("ABCDE", match.from("abcde"));
    assertEquals("AbCde", match.from("abCde"));
    assertEquals("AbCdE", match.from("abCdE"));
    assertEquals("AbCDe", match.from("abCDe"));
    assertEquals("ABcDe", match.from("aBcDe"));
    assertEquals("ABCde", match.from("aBCde"));
    assertEquals("AURL", match.from("aURL"));
    assertEquals("MyURL", match.from("myURL"));
  }

}
