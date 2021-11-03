package io.avaje.jsonb.generator;

import io.avaje.jsonb.spi.MetaData;

import javax.annotation.processing.FilerException;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.FileNotFoundException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class ComponentReader {

  private final ProcessingContext ctx;
  private final ComponentMetaData componentMetaData;

  ComponentReader(ProcessingContext ctx, ComponentMetaData metaData) {
    this.ctx = ctx;
    this.componentMetaData = metaData;
  }

  void read() {
    String factory = loadMetaInfServices();
    if (factory != null) {
      TypeElement moduleType = ctx.element(factory);
      if (moduleType != null) {
        componentMetaData.setFullName(factory);
        MetaData metaData = moduleType.getAnnotation(MetaData.class);
        Class<?>[] value = metaData.value();
        ctx.logError("got:" + Arrays.toString(value));
        for (Class<?> aClass : value) {
          componentMetaData.add(aClass.getCanonicalName());
        }
      }
    }
  }

  private String loadMetaInfServices() {
    final List<String> lines = loadMetaInf();
    return lines.isEmpty() ? null : lines.get(0);
  }

  private List<String> loadMetaInf() {
    try {
      FileObject fileObject = ctx.env()
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
      ctx.logDebug("FilerException reading services file");

    } catch (Exception e) {
      e.printStackTrace();
      ctx.logWarn("Error reading services file: " + e.getMessage());
    }
    return Collections.emptyList();
  }

}
