package org.example.customer.views;

import io.avaje.jsonb.JsonView;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ViewTest {

  Jsonb jsonb = Jsonb.newBuilder().build();

  @Test
  void jsonView_sameInstance() {
    JsonView<VCustomer> v0 = jsonb.type(VCustomer.class).view("(id, name, billingAddress(street), contacts(id, lastName))");
    JsonView<VCustomer> v1 = jsonb.type(VCustomer.class).view("(id, name, billingAddress(street), contacts(id, lastName))");

    assertThat(v0).isSameAs(v1);
  }

  @Test
  void jsonView_differentInstance_byDsl() {
    JsonView<VCustomer> v0 = jsonb.type(VCustomer.class).view("(id, name)");
    JsonView<VCustomer> v1 = jsonb.type(VCustomer.class).view("(id, name  )");
    assertThat(v0).isNotSameAs(v1);
  }

  @Test
  void jsonView_differentInstance_byType() {
    JsonView<?> v0 = jsonb.type(VCustomer.class).view("(id, name)");
    JsonView<?> v1 = jsonb.type(VContact.class).view("(id, name)");
    assertThat(v0).isNotSameAs(v1);
  }

  @Test
  void jsonView() {

    JsonView<VCustomer> customerJsonView = jsonb.type(VCustomer.class).view("(id, name, billingAddress(street), contacts(id, lastName))");

    VAddress billingAddress = new VAddress().street("my street").suburb("my suburb");
    VCustomer customer = new VCustomer().id(42).name("rob").whenCreated(Instant.now()).billingAddress(billingAddress);
    customer.contacts().add(new VContact(7L, "fo", "nar"));
    customer.contacts().add(new VContact(8L, "ba", "zar"));

    String asJson = customerJsonView.toJson(customer);
    assertThat(asJson).isEqualTo("{\"id\":42,\"name\":\"rob\",\"billingAddress\":{\"street\":\"my street\"},\"contacts\":[{\"id\":7,\"lastName\":\"nar\"},{\"id\":8,\"lastName\":\"zar\"}]}");
  }

  @Test
  void jsonView_list() {

    JsonView<List<VCustomer>> customerJsonView = jsonb.type(VCustomer.class).list().view("(id, name, contacts(*))");

    VAddress billingAddress = new VAddress().street("my street").suburb("my suburb");
    VCustomer customer0 = new VCustomer().id(42).name("rob").whenCreated(Instant.now()).billingAddress(billingAddress);
    VCustomer customer1 = new VCustomer().id(43).name("bob").whenCreated(Instant.now()).billingAddress(billingAddress);
    customer0.contacts().add(new VContact(7L, "fo", "nar"));
    customer0.contacts().add(new VContact(8L, "ba", "zar"));

    List<VCustomer> customers = new ArrayList<>();
    customers.add(customer0);
    customers.add(customer1);

    String asJson = customerJsonView.toJson(customers);
    assertThat(asJson).isEqualTo("[{\"id\":42,\"name\":\"rob\",\"contacts\":[{\"id\":7,\"firstName\":\"fo\",\"lastName\":\"nar\"},{\"id\":8,\"firstName\":\"ba\",\"lastName\":\"zar\"}]},{\"id\":43,\"name\":\"bob\"}]");
  }
}
