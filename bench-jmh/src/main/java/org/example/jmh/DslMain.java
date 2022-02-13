package org.example.jmh;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import org.example.jmh.model.SomePropertyData;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class DslMain {

  public static void main(String[] args) throws IOException {

    var testData = new SomePropertyData("firstNameProperty1", "lastNameProperty2", "anotherSimilarProperty3", "moreOrLessProperty4", "lastButNotLeastProperty5", "property6", "property7", "property8", "property9", "property10");

    DslJson<Object> dsl = new DslJson<>();

    ByteArrayOutputStream os = new ByteArrayOutputStream();

    //JsonWriter jsonWriter = dsl.newWriter();
    //byte[] bytes = jsonWriter.getByteBuffer();
    dsl.serialize(testData, os);

    String asJson = os.toString(StandardCharsets.UTF_8);


    StringReader se = new StringReader(asJson);
    ByteArrayInputStream is = new ByteArrayInputStream(asJson.getBytes(StandardCharsets.UTF_8));

    JsonReader.ReadObject<BigInteger> reader = dsl.tryFindReader(BigInteger.class);
    reader.read(null);
    SomePropertyData result = dsl.deserialize(SomePropertyData.class, is);
    System.out.println("here");

  }
}
