package io.avaje.jsonb.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class JsonbProcessorTest {

  @AfterEach
  void deleteGeneratedFiles() throws IOException {
    try {
      Paths.get("io.avaje.jsonb.Jsonb$GeneratedComponent").toAbsolutePath().toFile().delete();
      Files.walk(Paths.get("io").toAbsolutePath())
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    } catch (final Exception e) {
    }
  }

  @Test
  void testGeneration() throws Exception {
    final String source =
        Paths.get("src/test/java/io/avaje/jsonb/generator/models/valid")
            .toAbsolutePath()
            .toString();

    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    final StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);

    manager.setLocation(StandardLocation.SOURCE_PATH, List.of(new File(source)));

    final Set<Kind> fileKinds = Set.of(Kind.SOURCE);

    final Iterable<JavaFileObject> files =
        manager.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);

    final CompilationTask task =
        compiler.getTask(
            new PrintWriter(System.out),
            null,
            null,
            List.of("--release=" + Integer.getInteger("java.specification.version")),
            null,
            files);
    task.setProcessors(List.of(new JsonbProcessor()));

    assertThat(task.call()).isTrue();
  }

  @Disabled
  @Test
  void testImportFail() throws Exception {

    final Iterable<JavaFileObject> files = getInvalidSourceFile("InvalidImport");

    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    final CompilationTask task =
        compiler.getTask(
            new PrintWriter(System.out),
            null,
            null,
            List.of("--release=" + Integer.getInteger("java.specification.version")),
            null,
            files);
    task.setProcessors(List.of(new JsonbProcessor()));

    assertThat(task.call()).isFalse();
    Files.walk(Paths.get("java").toAbsolutePath())
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
  }

  private Iterable<JavaFileObject> getInvalidSourceFile(String name) throws Exception {
    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    final StandardJavaFileManager files = compiler.getStandardFileManager(null, null, null);

    files.setLocation(
        StandardLocation.SOURCE_PATH,
        List.of(
            new File(
                Paths.get("src/test/java/io/avaje/jsonb/generator/models/invalid")
                    .toAbsolutePath()
                    .toString())));

    final Set<Kind> fileKinds = Set.of(Kind.SOURCE);
    final Iterable<JavaFileObject> jfos =
        files.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);

    for (final JavaFileObject jfo : jfos) {
      if (jfo.getName().contains(name)) return Set.of(jfo);
    }

    return null;
  }
}
