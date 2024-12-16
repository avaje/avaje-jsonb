package io.avaje.json.node;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class SerializeTest {

  @Test
  void test() throws IOException, ClassNotFoundException {

    Boolean.valueOf(true);
    final var source = JsonArray.create()
      .add("foo")
      .add(JsonObject.create()
        .add("aStr", "a")
        .add("aBool", true)
        .add("aInt", 42)
        .add("aLong", 420L)
        .add("aDouble", JsonDouble.of(4204.3D))
        .add("aDecimal", JsonDecimal.of(BigDecimal.TEN))
      );

    var baos = new ByteArrayOutputStream();
    try (var oos = new ObjectOutputStream(baos)) {
      oos.writeObject(source);
    }

    var ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
    var readO = ois.readObject();

    assertThat(readO).isInstanceOf(JsonArray.class);
    assertThat(readO).isEqualTo(source);
  }
}
