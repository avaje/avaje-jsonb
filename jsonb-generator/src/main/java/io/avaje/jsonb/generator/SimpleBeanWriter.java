package io.avaje.jsonb.generator;

import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

class SimpleBeanWriter {

  private final BeanReader beanReader;
  private final ProcessingContext context;
  private final String shortName;
  private final String adapterPackage;
  private final String adapterFullName;

  private Append writer;

  SimpleBeanWriter(BeanReader beanReader, ProcessingContext context) {
    this.beanReader = beanReader;
    this.context = context;
    TypeElement origin = beanReader.getBeanType();
    String originName = origin.getQualifiedName().toString();
    this.shortName = origin.getSimpleName().toString();
    String originPackage = Util.packageOf(originName);
    this.adapterPackage = originPackage.equals("") ? "jsonb" : originPackage + ".jsonb";
    this.adapterFullName = adapterPackage + "." + shortName + "JsonAdapter";
  }

  String fullName() {
    return adapterFullName;
  }

  private Writer createFileWriter() throws IOException {
    JavaFileObject jfo = context.createWriter(adapterFullName);
    return jfo.openWriter();
  }

  void write() throws IOException {
    writer = new Append(createFileWriter());
    writePackage();
    writeImports();
    writeClassStart();
    writeFields();
    writeConstructor();
    writeToFromJson();
    writeClassEnd();
    writer.close();
  }

  private void writeConstructor() {
    writer.append("  public %sJsonAdapter(Jsonb jsonb) {", shortName).eol();
    beanReader.writeConstructor(writer);
    writer.append("  }").eol();
  }

  private void writeToFromJson() {
    beanReader.writeToJson(writer);
    beanReader.writeFromJson(writer);
  }

  private void writeClassEnd() {
    writer.append("}").eol();
  }

  private void writeClassStart() {
    writer.append("public class %sJsonAdapter extends JsonAdapter<%s> {", shortName, shortName).eol().eol();
  }

  private void writeFields() {
    beanReader.writeFields(writer);
  }

  private void writeImports() {
    beanReader.writeImports(writer);
  }

  private void writePackage() {
    writer.append("package %s;", adapterPackage).eol().eol();
  }
}
