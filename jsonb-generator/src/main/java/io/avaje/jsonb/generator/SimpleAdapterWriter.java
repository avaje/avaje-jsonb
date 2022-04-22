package io.avaje.jsonb.generator;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

class SimpleAdapterWriter {

  private final BeanReader beanReader;
  private final ProcessingContext context;
  private final String adapterShortName;
  private final String adapterPackage;
  private final String adapterFullName;

  private Append writer;

  SimpleAdapterWriter(BeanReader beanReader, ProcessingContext context) {
    this.beanReader = beanReader;
    this.context = context;
    AdapterName adapterName = new AdapterName(beanReader.getBeanType());
    this.adapterShortName = adapterName.shortName();
    this.adapterPackage = adapterName.adapterPackage();
    this.adapterFullName = adapterName.fullName();
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
    writer.append("  public %sJsonAdapter(Jsonb jsonb) {", adapterShortName).eol();
    beanReader.writeConstructor(writer);
    writer.append("  }").eol();
  }

  private void writeToFromJson() {
    beanReader.writeViewSupport(writer);
    beanReader.writeToJson(writer);
    beanReader.writeFromJson(writer);
  }

  private void writeClassEnd() {
    writer.append("}").eol();
  }

  private void writeClassStart() {
    writer.append("@Generated").eol();
    writer.append("public final class %sJsonAdapter extends JsonAdapter<%s> ", adapterShortName, beanReader.shortName());
    if (!beanReader.hasSubtypes()) {
      writer.append("implements ViewBuilderAware ");
    }
    writer.append("{").eol().eol();
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
