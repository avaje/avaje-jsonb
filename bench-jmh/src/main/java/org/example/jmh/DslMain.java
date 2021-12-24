package org.example.jmh;

import com.dslplatform.json.DslJson;
import org.example.jmh.model.SomePropertyData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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




  }
}
