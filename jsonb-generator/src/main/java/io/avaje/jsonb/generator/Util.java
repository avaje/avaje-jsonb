package io.avaje.jsonb.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import static io.avaje.jsonb.generator.APContext.typeElement;
import static io.avaje.jsonb.generator.APContext.logError;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Util {

  // whitespace not in quotes
  private static final Pattern WHITE_SPACE_REGEX =
      Pattern.compile("\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
  // comma not in quotes
  private static final Pattern COMMA_PATTERN =
      Pattern.compile(", (?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)");

  static boolean validImportType(String type, String packageName) {
    return type.indexOf('.') > -1
      && !type.startsWith("java.lang.")
      && importDifferentPackage(type, packageName)
      || importJavaLangSubpackage(type);
  }

  private static boolean importDifferentPackage(String type, String packageName) {
    return type.replace(packageName + '.', "").indexOf('.') > 0;
  }

  private static boolean importJavaLangSubpackage(String type) {
    return type.startsWith("java.lang.") && importDifferentPackage(type, "java.lang");
  }

  public static String sanitizeImports(String type) {
    final int pos = type.indexOf("@");
    if (pos == -1) {
      return trimArrayBrackets(type);
    }
    final var start = pos == 0 ? type.substring(0, pos) : "";
    return start + trimArrayBrackets(type.substring(type.lastIndexOf(' ') + 1));
  }

  private static String trimArrayBrackets(String type) {
    return type.replaceAll("[^\\n\\r\\t $*_;\\w.]", "");
  }

  static String packageOf(String cls) {
    int pos = cls.lastIndexOf('.');
    return (pos == -1) ? "" : cls.substring(0, pos);
  }

  static String shortName(String fullType) {
    int p = fullType.lastIndexOf('.');
    if (p == -1) {
      return fullType;
    } else {
      return fullType.substring(p + 1);
    }
  }

  static String shortType(String fullType) {
    final int p = fullType.lastIndexOf('.');
    if (p == -1) {
      return fullType;
    } else if (fullType.startsWith("java")) {
      return fullType.substring(p + 1);
    } else {
      var result = "";
      var foundClass = false;
      for (final String part : fullType.split("\\.")) {
        char firstChar = part.charAt(0);
        if (foundClass
            || Character.isUpperCase(firstChar)
            || (!Character.isAlphabetic(firstChar) && Character.isJavaIdentifierStart(firstChar))) {
          foundClass = true;
          result += (result.isEmpty() ? "" : ".") + part;
        }
      }
      // when in doubt, do the basic thing
      if (result.isBlank()) {
        return fullType.substring(p + 1);
      }
      return result;
    }
  }

  /** Trim off annotations from the raw type if present. */
  public static String trimAnnotations(String input) {
    input = COMMA_PATTERN.matcher(input).replaceAll(",");
    return cutAnnotations(input);
  }

  private static String cutAnnotations(String input) {
    final int pos = input.indexOf("@");
    if (pos == -1) {
      return input;
    }

    final Matcher matcher = WHITE_SPACE_REGEX.matcher(input);
    int currentIndex = 0;
    if (matcher.find()) {
      currentIndex = matcher.start();
    }
    final var result = input.substring(0, pos) + input.substring(currentIndex + 1);
    return cutAnnotations(result);
  }

  /** Return the common parent package. */
  static String commonParent(String currentTop, String aPackage) {
    if (aPackage == null) return currentTop;
    if (currentTop == null) return packageOf(aPackage);
    if (aPackage.startsWith(currentTop)) {
      return currentTop;
    }
    int next;
    do {
      next = currentTop.lastIndexOf('.');
      if (next > -1) {
        currentTop = currentTop.substring(0, next);
        if (aPackage.startsWith(currentTop)) {
          return currentTop;
        }
      }
    } while (next > -1);

    return currentTop;
  }

  static String initCap(String input) {
    if (input.length() < 2) {
      return input.toUpperCase();
    } else {
      return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }
  }

  static List<String> escapeQuotes(List<String> all) {
    List<String> escaped = new ArrayList<>(all.size());
    for (String raw : all) {
      escaped.add(Util.escapeQuotes(raw));
    }
    return escaped;
  }

  static String escapeQuotes(String input) {
    return input.replaceAll("^\"|\"$", "\\\\\"");
  }

  static String initLower(String name) {
    StringBuilder sb = new StringBuilder(name.length());
    boolean toLower = true;
    for (char ch : name.toCharArray()) {
      if (Character.isUpperCase(ch)) {
        if (toLower) {
          sb.append(Character.toLowerCase(ch));
        } else {
          sb.append(ch);
        }
      } else {
        sb.append(ch);
        toLower = false;
      }
    }
    return sb.toString();
  }

  /** Return the base type given the JsonAdapter type. */
  static String baseTypeOfAdapter(String adapterFullName) {
    final var element = typeElement(adapterFullName);
    if (element == null) {
      throw new NullPointerException("Element not found for [" + adapterFullName + "]");
    }
    return baseTypeOfAdapter(element);
  }

  static String baseTypeOfAdapter(TypeElement element) {
    return element.getInterfaces().stream()
      .filter(t -> t.toString().contains("io.avaje.jsonb.JsonAdapter"))
      .findFirst()
      .map(Object::toString)
      .map(GenericType::parse)
      .map(GenericType::firstParamType)
      .map(Util::extractTypeWithNest)
      .orElseGet(() -> {
        logError(element, "Custom Adapters must implement JsonAdapter");
        return "Invalid";
      });
  }

  static String extractTypeWithNest(String fullType) {
    final int p = fullType.lastIndexOf('.');
    if (p == -1 || fullType.startsWith("java")) {
      return fullType;
    } else {
      final StringBuilder result = new StringBuilder();
      var foundClass = false;
      var firstClass = true;
      for (final String part : fullType.split("\\.")) {
        if (Character.isUpperCase(part.charAt(0))) {
          foundClass = true;
        }
        result.append(foundClass && !firstClass ? "$" : ".").append(part);
        if (foundClass) {
          firstClass = false;
        }
      }
      if (result.charAt(0) == '.') {
        result.deleteCharAt(0);
      }
      return result.toString();
    }
  }

  static boolean isPublic(Element element) {
    var mods = element.getModifiers();
    if (mods.contains(Modifier.PUBLIC)) {
      return true;
    }
    if (mods.contains(Modifier.PRIVATE) || mods.contains(Modifier.PROTECTED)) {
      return false;
    }
    boolean isImported = ProcessingContext.isImported(element);
    if (element instanceof VariableElement) {
      return !isImported && !mods.contains(Modifier.FINAL);
    }
    return !isImported;
  }

  static String valhalla() {
    try {
      if (Modifier.valueOf("VALUE") != null && APContext.previewEnabled()) return "value ";
    } catch (IllegalArgumentException e) {
      // no valhalla
    }
    return "";
  }
}
