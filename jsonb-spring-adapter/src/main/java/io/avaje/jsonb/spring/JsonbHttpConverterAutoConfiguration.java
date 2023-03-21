package io.avaje.jsonb.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.avaje.jsonb.Jsonb;

/**
 * Autoconfigure use of Avaje Jsonb for message conversion.
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.http.converter.GenericHttpMessageConverter")
public class JsonbHttpConverterAutoConfiguration {

  @Bean
  JsonbHttpMessageConverter jsonbConverter(Jsonb jsonb) {
    return new JsonbHttpMessageConverter(jsonb);
  }
}
