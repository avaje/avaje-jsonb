package io.avaje.jsonb.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class JsonbProcessorTest {

  Iterable<JavaFileObject> walkSourceFiles(File source) throws IOException {
    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    final StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);

    manager.setLocation(StandardLocation.SOURCE_PATH, List.of(source));

    final Set<Kind> fileKinds = Set.of(Kind.SOURCE);

    return manager.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);
  }

  CompilationTask generationTask(Iterable<JavaFileObject> files) {
    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    final CompilationTask task = compiler.getTask(
        new PrintWriter(System.out),
        null,
        null,
        List.of(
            "--release=" + Integer.getInteger("java.specification.version"),
            "-s",
            "target/generated-test-sources/test-annotations"),
        null,
        files);
    task.setProcessors(List.of(new JsonbProcessor()));

    return task;
  }

  @Test
  void testGeneration() throws Exception {
    final File validSource =
        Paths.get("src/test/java/io/avaje/jsonb/generator/models/valid")
            .toAbsolutePath()
            .toFile();

    final Iterable<JavaFileObject> files = walkSourceFiles(validSource);

    final CompilationTask task = generationTask(files);

    assertThat(task.call()).isTrue();
  }

  @Disabled
  @Test
  void testImportFail() throws Exception {
    final File invalidSource = Paths.get("src/test/java/io/avaje/jsonb/generator/models/invalid")
                    .toAbsolutePath()
                    .toFile();

    final Iterable<JavaFileObject> files = walkSourceFiles(invalidSource);

    // Check each invalid file individually since they are fail-fast.
    for (var file : files) {
      final CompilationTask task = generationTask(List.of(file));

      assertThat(task.call()).isFalse();
    }
  }
}
