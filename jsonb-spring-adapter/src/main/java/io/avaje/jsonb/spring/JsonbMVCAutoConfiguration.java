package io.avaje.jsonb.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.avaje.jsonb.Jsonb;

@Configuration
@ConditionalOnClass(name = "org.springframework.http.converter.GenericHttpMessageConverter")
public class JsonbMVCAutoConfiguration {

  @Bean
  JsonbHttpMessageConverter jsonbConverter(Jsonb jsonb) {
    return new JsonbHttpMessageConverter(jsonb);
  }
}
