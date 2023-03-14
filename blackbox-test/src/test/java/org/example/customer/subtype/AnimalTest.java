package org.example.customer.subtype;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnimalTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<Animal> animalJsonType = jsonb.type(Animal.class);

  @Test
  void toJson_fromJson_cat()  {
    Cat cat = new Cat();
    cat.name("PussInBoots");

    String asJson = animalJsonType.toJson(cat);
    assertThat(asJson).isEqualTo("{\"dtype\":\"Cat\",\"name\":\"PussInBoots\"}");

    Animal animal = animalJsonType.fromJson(asJson);
    assertThat(animal).isInstanceOf(Cat.class);
    Cat cat1 = (Cat) animal;
    assertThat(cat1.dtype().toString()).isEqualTo("Cat");
    cat1.dtype(null);
    assertThat(cat1.name()).isEqualTo("PussInBoots");
  }

  @Test
  void toJson_fromJson_dog()  {
    Dog dog = new Dog("Woof");
    dog.id(43);

    String asJson = animalJsonType.toJson(dog);
    assertThat(asJson).isEqualTo("{\"dtype\":\"Dog\",\"name\":\"Woof\",\"id\":43}");

    Animal animal = animalJsonType.fromJson(asJson);
    assertThat(animal).isInstanceOf(Dog.class);
    Dog dog1 = (Dog) animal;
    assertThat(dog1.name()).isEqualTo("Woof");
    assertThat(dog1.id()).isEqualTo(43);
  }
}
