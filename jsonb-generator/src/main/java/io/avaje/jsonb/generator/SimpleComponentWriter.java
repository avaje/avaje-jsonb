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
  private JavaFileObject fileObject;

  SimpleComponentWriter(ProcessingContext context, ComponentMetaData metaData) {
    this.context = context;
    this.metaData = metaData;
  }

  void initialise() throws IOException {
    if (fileObject == null) {
      fileObject = context.createWriter(metaData.fullName());
    }
  }

  private Writer createFileWriter() throws IOException {
    return fileObject.openWriter();
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
    final List<String> strings = metaData.allFactories();
    for (final String adapterFullName : strings) {
      final String adapterShortName = Util.shortName(adapterFullName);
      writer.append("    builder.add(%s.Factory);", adapterShortName).eol();
    }
    for (String adapterFullName : metaData.all()) {
      String adapterShortName = Util.shortName(adapterFullName);
      String typeName = typeShortName(adapterShortName);
      writer.append("    builder.add(%s.class, %s::new);", typeName, adapterShortName).eol();
    }
    writer.append("  }").eol().eol();
  }

  private String typeShortName(String adapterShortName) {
    String typeName = adapterShortName.substring(0, adapterShortName.length() - 11);
    int pos = typeName.lastIndexOf('$');
    if (pos > -1) {
      return typeName.substring(pos + 1);
    }
    return typeName;
  }

  private void writeClassEnd() {
    writer.append("}").eol();
  }

  private void writeClassStart() {
    String fullName = metaData.fullName();
    String shortName = Util.shortName(fullName);
    writer.append("@Generated").eol();
    List<String> factories = metaData.allFactories();
    if (!factories.isEmpty()) {
      writer.append("@MetaData.Factory({");
      writeMetaDataEntry(factories);
      writer.append("})").eol();
    }
    writer.append("@MetaData({");
    List<String> all = metaData.all();
    writeMetaDataEntry(all);
    writer.append("})").eol();

    writer.append("public class %s implements Jsonb.GeneratedComponent {", shortName).eol().eol();
  }

  private void writeMetaDataEntry(List<String> entries) {
    for (int i = 0, size = entries.size(); i < size; i++) {
      if (i > 0) {
        writer.append(", ");
      }
      writer.append("%s.class", Util.shortName(entries.get(i)));
    }
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
