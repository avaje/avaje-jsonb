package io.avaje.jsonb.generator;

import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;

import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.time.Instant;

/**
 * Write the source code for the bean.
 */
class SimpleBeanWriter {

//  private static final String CODE_COMMENT = "/**\n * Generated source - dependency injection builder for %s.\n */";
//  private static final String CODE_COMMENT_FACTORY = "/**\n * Generated source - dependency injection factory for request scoped %s.\n */";
//  private static final String CODE_COMMENT_BUILD = "  /**\n   * Create and register %s.\n   */";

  private final BeanReader beanReader;
  private final ProcessingContext context;
  private final String originName;
  private final String shortName;
  private final String packageName;
  private Append writer;

  SimpleBeanWriter(BeanReader beanReader, ProcessingContext context) {
    this.beanReader = beanReader;
    this.context = context;
    TypeElement origin = beanReader.getBeanType();
    this.originName = origin.getQualifiedName().toString();
    this.shortName = origin.getSimpleName().toString();
    this.packageName = Util.packageOf(originName);
  }

  private Writer createFileWriter() throws IOException {
    JavaFileObject jfo = context.createWriter(originName + "JsonAdapter");
    return jfo.openWriter();
  }

  void write() throws IOException {
    writer = new Append(createFileWriter());
    writePackage();
    writeImports();
    writeClassStart();
    writeFields();
    writeConstructor();
    writeToJson();

    writeClassEnd();
    writer.close();
  }

  private void writeFields() {
    beanReader.writeFields(writer);
  }

  private void writeConstructor() {
    writer.append("  public %sJsonAdapter(Jsonb jsonb) {", shortName).eol();
    beanReader.writeConstructor(writer);
    writer.append("  }").eol();
  }

  private void writeToJson() {
    beanReader.writeToJson(writer);
  }

  private void writeImports() {
    beanReader.writeImports(writer);
  }

  private void writeClassEnd() {
    writer.append("}").eol();
  }

  private void writeClassStart() {
    writer.append("public class %sJsonAdapter ", shortName);
    //writer.append("implements ");
    writer.append(" {").eol().eol();
  }

  private void writePackage() {
    if (packageName != null) {
      writer.append("package %s;", packageName).eol().eol();
    }
  }
}
