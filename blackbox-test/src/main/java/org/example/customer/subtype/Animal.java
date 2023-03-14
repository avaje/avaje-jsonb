package org.example.customer.subtype;

import io.avaje.jsonb.Json;

@Json(typeProperty = "dtype")
@Json.SubType(type = Cat.class)
@Json.SubType(type = Dog.class)
@Json.SubType(type = Fish.class)
public interface Animal {

  String name();
  enum AnimalEnum{
	  Cat,
	  Dog,
	  Fish
  }
}
