package io.avaje.jsonb.generator;

import static io.avaje.jsonb.generator.APContext.createSourceFile;

import java.io.IOException;
import java.io.Writer;

import javax.tools.JavaFileObject;

final class SimpleAdapterWriter {

  private final BeanReader beanReader;
  private final String adapterShortName;
  private final String adapterPackage;
  private final String adapterFullName;
  private final int genericParamsCount;

  private Append writer;

  SimpleAdapterWriter(BeanReader beanReader) {
    this.beanReader = beanReader;
    final AdapterName adapterName = new AdapterName(beanReader.beanType());
    this.adapterShortName = adapterName.shortName();
    this.adapterPackage = adapterName.adapterPackage();
    this.adapterFullName = adapterName.fullName();
    this.genericParamsCount = beanReader.genericTypeParamsCount();
  }

  String fullName() {
    return adapterFullName;
  }

  private Writer createFileWriter() throws IOException {
    final JavaFileObject jfo = createSourceFile(adapterFullName);
    return jfo.openWriter();
  }

  boolean hasGenericFactory() {
    return genericParamsCount > 0;
  }

  void write() throws IOException {
    writer = new Append(createFileWriter());
    writePackage();
    writeImports();
    writeClassStart();
    writeFactory();
    writeFields();
    writeConstructor();
    writeToFromJson();
    writeClassEnd();
    writer.close();
  }

  private void writeFactory() {
    if (genericParamsCount > 0) {
      String typeName = adapterShortName;
      final int nestedIndex = adapterShortName.indexOf("$");
      if (nestedIndex != -1) {
        typeName = typeName.substring(nestedIndex + 1);
      }
      writer.append("  public static final JsonAdapter.Factory FACTORY = (type, jsonb) -> {").eol();
      writer.append("    if (Types.isGenericTypeOf(type, %s.class)) {", typeName).eol();
      writer.append("      Type[] args = Types.typeArguments(type);").eol();
      writer.append("      return new %sJsonAdapter(jsonb", adapterShortName);
      for (int i = 0; i < genericParamsCount; i++) {
        writer.append(", args[%d]", i);
      }
      writer.append(");").eol();
      writer.append("    }").eol();
      writer.append("    return null;").eol();
      writer.append("  };").eol().eol().eol();
    }
  }

  private void writeConstructor() {
    writer.append("  public %sJsonAdapter(Jsonb jsonb", adapterShortName);
    for (int i = 0; i < genericParamsCount; i++) {
      writer.append(", Type param%d", i);
    }
    writer.append(") {", adapterShortName).eol();
    beanReader.writeConstructor(writer);
    writer.append("  }").eol();

    if (genericParamsCount > 0) {
      writer.eol();
      writer.append("  /**").eol();
      writer.append("   * Construct using Object for generic type parameters.").eol();
      writer.append("   */").eol();
      writer.append("  public %sJsonAdapter(Jsonb jsonb) {", adapterShortName).eol();
      writer.append("    this(jsonb");
      for (int i = 0; i < genericParamsCount; i++) {
        writer.append(", Object.class");
      }
      writer.append(");").eol();
      writer.append("  }").eol();
    }
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
    if (genericParamsCount > 0) {
      writer.append("@SuppressWarnings({\"unchecked\", \"rawtypes\"})").eol();
    }
    writer.append("@Generated(\"io.avaje.jsonb.generator\")").eol();
    writer.append("public final %sclass %sJsonAdapter implements JsonAdapter<%s>", Util.valhalla(), adapterShortName, beanReader.shortName());
    if (beanReader.supportsViewBuilder()) {
      writer.append(", ViewBuilderAware ");
    }
    writer.append("{").eol().eol();
  }

  private void writeFields() {
    beanReader.writeFields(writer);
  }

  private void writeImports() {
    beanReader.writeImports(writer, adapterPackage);
  }

  private void writePackage() {
    writer.append("package %s;", adapterPackage).eol().eol();
  }
}
