# Polymorphic Types

Handle polymorphic JSON with inheritance.

## Type Discriminator

Use a discriminator field to identify the actual type:

```java
public abstract class Animal {
  @JsonProperty("@type")
  public String type;
}

public class Dog extends Animal {
  public String breed;
}

public class Cat extends Animal {
  public String color;
}
```

Configure the mapper:

```java
Jsonb jsonb = Jsonb.builder()
  .polymorphic(Animal.class, "type")
  .add("dog", Dog.class)
  .add("cat", Cat.class)
  .build();
```

## Next Steps

- Learn [file chaining](file-chaining.md)
