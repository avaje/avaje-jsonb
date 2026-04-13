# Polymorphic Types

Handle polymorphic JSON with inheritance.

## Type Discriminator

Use `@Json.SubType` on the base type to declare concrete subtypes:

```java
import io.avaje.jsonb.Json;

@Json(typeProperty = "type")
@Json.SubType(type = Dog.class, name = "dog")
@Json.SubType(type = Cat.class, name = "cat")
public abstract class Animal {
  public String name;
}

@Json
public class Dog extends Animal {
  public String breed;
}

@Json
public class Cat extends Animal {
  public String color;
}
```

Serialize and deserialize via the base type:

```java
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

Jsonb jsonb = Jsonb.instance();
JsonType<Animal> animalType = jsonb.type(Animal.class);

Animal dog = animalType.fromJson("{\"type\":\"dog\",\"name\":\"Fido\",\"breed\":\"Labrador\"}");
String json = animalType.toJson(dog);
```

## Next Steps

- Learn [file chaining](file-chaining.md)
