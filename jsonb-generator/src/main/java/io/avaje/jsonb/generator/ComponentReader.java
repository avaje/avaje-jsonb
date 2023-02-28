package io.avaje.jsonb.generator;

import static io.avaje.jsonb.generator.ProcessingContext.*;
import javax.annotation.processing.FilerException;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.FileNotFoundException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class ComponentReader {
	
  private final ComponentMetaData componentMetaData;

  ComponentReader(ComponentMetaData metaData) {
    this.componentMetaData = metaData;
  }

  void read() {
    String componentFullName = loadMetaInfServices();
    if (componentFullName != null) {
      TypeElement moduleType = element(componentFullName);
      if (moduleType != null) {
        componentMetaData.setFullName(componentFullName);
        readMetaData(moduleType);
      }
    }
  }

  /**
   * Read the existing JsonAdapters from the MetaData annotation of the generated component.
   */
  private void readMetaData(TypeElement moduleType) {
    for (final AnnotationMirror annotationMirror : moduleType.getAnnotationMirrors()) {

      final MetaDataPrism metaData = MetaDataPrism.getInstance(annotationMirror);
      final FactoryPrism metaDataFactory = FactoryPrism.getInstance(annotationMirror);

      if (metaData != null) {

        metaData.value().stream()
            .map(TypeMirror::toString)
            .forEach(componentMetaData::add);

      } else if (metaDataFactory != null) {

        metaDataFactory.value().stream()
            .map(TypeMirror::toString)
            .forEach(componentMetaData::add);
      }
    }
  }

  private String loadMetaInfServices() {
    final List<String> lines = loadMetaInf();
    return lines.isEmpty() ? null : lines.get(0);
  }

  private List<String> loadMetaInf() {
    try {
      FileObject fileObject = env()
        .getFiler()
        .getResource(StandardLocation.CLASS_OUTPUT, "", Constants.META_INF_COMPONENT);

      if (fileObject != null) {
        List<String> lines = new ArrayList<>();
        Reader reader = fileObject.openReader(true);
        LineNumberReader lineReader = new LineNumberReader(reader);
        String line;
        while ((line = lineReader.readLine()) != null) {
          line = line.trim();
          if (!line.isEmpty()) {
            lines.add(line);
          }
        }
        return lines;
      }

    } catch (FileNotFoundException | NoSuchFileException e) {
      // logDebug("no services file yet");

    } catch (FilerException e) {
      logDebug("FilerException reading services file");

    } catch (Exception e) {
      e.printStackTrace();
      logWarn("Error reading services file: " + e.getMessage());
    }
    return Collections.emptyList();
  }
}
