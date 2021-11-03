package io.avaje.jsonb.generator;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

class SimpleComponentWriter {

  private final ProcessingContext context;
  private Append writer;

  private String packageName;

  SimpleComponentWriter(ProcessingContext context) {
    this.context = context;
  }

  private Writer createFileWriter() throws IOException {
    JavaFileObject jfo = context.createWriter(packageName + ".JsonComponent");
    return jfo.openWriter();
  }

  void write() throws IOException {
    writer = new Append(createFileWriter());
    writePackage();
    writeImports();
    writeClassStart();
    writeToFromJson();
    writeClassEnd();
    writer.close();
  }


  private void writeToFromJson() {
    //beanReader.writeToJson(writer);
    //beanReader.writeFromJson(writer);
  }

  private void writeClassEnd() {
    writer.append("}").eol();
  }

  private void writeClassStart() {
    writer.append("public class JsonComponent { // implements Json.Component {").eol().eol();
  }

  private void writeImports() {
  }

  private void writePackage() {
    if (packageName != null) {
      writer.append("package %s;", packageName).eol().eol();
    }
  }
}
