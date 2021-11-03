package io.avaje.jsonb.generator;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

class SimpleComponentWriter {

  private final ProcessingContext context;
  private final ComponentMetaData metaData;
  private Append writer;

  //private String packageName;

  SimpleComponentWriter(ProcessingContext context, ComponentMetaData metaData) {
    this.context = context;
    this.metaData = metaData;
  }

  private Writer createFileWriter() throws IOException {
    String fullName = metaData.fullName();
    JavaFileObject jfo = context.createWriter(fullName);
    return jfo.openWriter();
  }

  void write() throws IOException {
    writer = new Append(createFileWriter());
    writePackage();
    writeDebug();
    writeImports();
    writeClassStart();
    writeRegister();
    writeClassEnd();
    writer.close();
  }

  private void writeDebug() {
    List<String> all = metaData.all();
    for (String s : all) {
      writer.append("// %s", s).eol();
    }
    writer.eol();
  }

  private void writeRegister() {
    writer.append("  @Override").eol();
    writer.append("  public void register(Jsonb.Builder builder) {").eol();
    writer.append("    builder.add(Customer.class, CustomerJsonAdapter::new);").eol();
    writer.append("    builder.add(Contact.class, ContactJsonAdapter::new);").eol();
    writer.append("  }").eol();
  }

  private void writeClassEnd() {
    writer.append("}").eol();
  }

  private void writeClassStart() {
    String fullName = metaData.fullName();
    String shortName = Util.shortName(fullName);
    writer.append("@Generated").eol();
    writer.append("@MetaData({");
    List<String> all = metaData.all();
    for (int i = 0, size = all.size(); i < size; i++) {
      if (i > 0) {
        writer.append(", ");
      }
      writer.append("%s.class", Util.shortName(all.get(i)));
    }
    writer.append("})").eol();

    writer.append("public class %s implements Jsonb.Component {", shortName).eol().eol();
  }

  private final Set<String> importTypes = new TreeSet<>();

  private void writeImports() {
    importTypes.add(Constants.JSONB);
    importTypes.add(Constants.JSONB_SPI);
    importTypes.addAll(metaData.all());

    for (String importType : importTypes) {
      if (Util.validImportType(importType)) {
        writer.append("import %s;", importType).eol();
      }
    }
    writer.eol();
  }

  private void writePackage() {
    String packageName = metaData.packageName();
    if (packageName != null && !packageName.isEmpty()) {
      writer.append("package %s;", packageName).eol().eol();
    }
  }
}
