package io.avaje.jsonb.spring;

import java.io.IOException;
import java.lang.reflect.Type;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import io.avaje.jsonb.Jsonb;

/**
 * Avaje Jsonb message converter.
 */
public class JsonbHttpMessageConverter extends AbstractGenericHttpMessageConverter<Object>
    implements GenericHttpMessageConverter<Object> {

  private final Jsonb serializer;

  public JsonbHttpMessageConverter(Jsonb serializer) {
    super(MediaType.APPLICATION_JSON);
    this.serializer = serializer;
  }

  @Override
  protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
      throws IOException, HttpMessageNotReadableException {
    return serializer.type(clazz).fromJson(inputMessage.getBody());
  }

  @Override
  public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
      throws IOException, HttpMessageNotReadableException {
    return serializer.type(type).fromJson(inputMessage.getBody());
  }

  @Override
  protected void writeInternal(Object instance, Type type, HttpOutputMessage outputMessage)
      throws IOException, HttpMessageNotWritableException {
    serializer.type(type).toJson(instance, outputMessage.getBody());
  }
}
