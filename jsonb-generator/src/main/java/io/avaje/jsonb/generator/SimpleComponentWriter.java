package io.avaje.jsonb.generator;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

class SimpleComponentWriter {

  private final ProcessingContext context;
  private final ComponentMetaData metaData;
  private final Set<String> importTypes = new TreeSet<>();
  private Append writer;

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
    writeImports();
    writeClassStart();
    writeRegister();
    writeClassEnd();
    writer.close();
  }

  void writeMetaInf() throws IOException {
    FileObject fileObject = context.createMetaInfWriterFor(Constants.META_INF_COMPONENT);
    if (fileObject != null) {
      Writer writer = fileObject.openWriter();
      writer.write(metaData.fullName());
      writer.close();
    }
  }

  private void writeRegister() {
    writer.append("  @Override").eol();
    writer.append("  public void register(Jsonb.Builder builder) {").eol();
    for (String adapterFullName : metaData.all()) {
      String shortName = Util.shortName(adapterFullName);
      String typeName = shortName.substring(0, shortName.length() - 11);
      writer.append("    builder.add(%s.class, %s::new);", typeName, shortName).eol();
    }
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


  private void writeImports() {
    importTypes.add(Constants.JSONB);
    importTypes.add(Constants.JSONB_SPI);
    importTypes.addAll(metaData.allImports());

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
