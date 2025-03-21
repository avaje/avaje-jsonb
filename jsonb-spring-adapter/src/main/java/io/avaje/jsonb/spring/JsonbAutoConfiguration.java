package io.avaje.jsonb.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.avaje.jsonb.Jsonb;

/**
 * Autoconfiguration of Avaje Jsonb.
 */
@Configuration
public class JsonbAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  Jsonb jsonb(
      @Value("${jsonb.deserialize.failOnUnknown:false}") boolean failUnknown,
      @Value("${jsonb.deserialize.failOnNullPrimitives:false}") boolean failOnNullPrimitives,
      @Value("${jsonb.serialize.mathTypesAsString:false}") boolean mathTypesAsString,
      @Value("${jsonb.serialize.empty:true}") boolean serializeEmpty,
      @Value("${jsonb.serialize.nulls:false}") boolean serializeNulls) {

    return Jsonb.builder()
        .failOnUnknown(failUnknown)
        .failOnNullPrimitives(failOnNullPrimitives)
        .mathTypesAsString(mathTypesAsString)
        .serializeEmpty(serializeEmpty)
        .serializeNulls(serializeNulls)
        .build();
  }
}
