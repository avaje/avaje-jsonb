package org.example.customer.subtype;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleTest {

  Jsonb jsonb = Jsonb.newBuilder().build();
  JsonType<Vehicle> vehicleJsonType = jsonb.type(Vehicle.class);

  @Test
  void truck_toJson()  {
    Truck truck = new Truck(42);
    truck.name("bigTruck");
    truck.capacity(95);

    String asJson = vehicleJsonType.toJson(truck);
    assertThat(asJson).isEqualTo("{\"@type\":\"TRUCK\",\"id\":42,\"name\":\"bigTruck\",\"capacity\":95}");
  }

  @Test
  void car_toJson()  {
    Car car = new Car();
    car.id(42);
    car.name("bigTruck");
    car.colour("blue");

    String asJson = vehicleJsonType.toJson(car);
    assertThat(asJson).isEqualTo("{\"@type\":\"Car\",\"id\":42,\"name\":\"bigTruck\",\"colour\":\"blue\"}");
  }

  @Test
  void truck_fromJson()  {

    Vehicle vehicle = vehicleJsonType.fromJson("{\"@type\":\"TRUCK\",\"id\":42,\"name\":\"bigTruck\",\"capacity\":95}");

    assertThat(vehicle).isInstanceOf(Truck.class);
    Truck truck1 = (Truck)vehicle;
    assertThat(truck1.id()).isEqualTo(42);
    assertThat(truck1.name()).isEqualTo("bigTruck");
    assertThat(truck1.capacity()).isEqualTo(95);
  }

  @Test
  void car_fromJson()  {

    Vehicle vehicle = vehicleJsonType.fromJson("{\"@type\":\"Car\",\"id\":42,\"name\":\"myCar\",\"colour\":\"blue\"}");

    assertThat(vehicle).isInstanceOf(Car.class);
    Car car = (Car)vehicle;
    assertThat(car.id()).isEqualTo(42);
    assertThat(car.name()).isEqualTo("myCar");
    assertThat(car.colour()).isEqualTo("blue");
  }
}
