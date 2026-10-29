package io.avaje.jsonb.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import static io.avaje.jsonb.generator.APContext.typeElement;
import static io.avaje.jsonb.generator.APContext.logError;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
    return packageName.isBlank() || type.replace(packageName + '.', "").indexOf('.') > 0;
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

  static String shortName(String fullType) {
    int p = fullType.lastIndexOf('.');
    if (p == -1) {
      return fullType;
    }
    return fullType.substring(p + 1);
  }

  /** Returns the type identifier up to and including the first class */
  static String baseType(String fullType) {
    final int p = fullType.lastIndexOf('.');
    if (p == -1 || fullType.startsWith("java")) {
      return fullType;
    }
    var result = "";
      for (final String part : fullType.split("\\.")) {
        result += (result.isEmpty() ? "" : ".") + part;
        char firstChar = part.charAt(0);
        if (Character.isUpperCase(firstChar)
            || (!Character.isAlphabetic(firstChar) && Character.isJavaIdentifierStart(firstChar))) {
          break;
        }
      }
      // when in doubt, do the basic thing
      if (result.isBlank()) {
        return fullType;
      }
      return result;
  }

  /** Returns the type identifier starting from the first class */
  static String shortType(String fullType) {
    final int p = fullType.lastIndexOf('.');
    if (p == -1){
      return fullType;
    }
    if (fullType.startsWith("java")){
      return fullType.substring(p + 1);
    }
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

  /** Return the hash of the json property name, this must match JsonReader.nameHash(). */
  static int fieldNameHash(String name) {
    int hash = 0x811c9dc5;
    for (byte b : unescapeJava(name).getBytes(StandardCharsets.UTF_8)) {
      hash ^= b;
      hash *= 0x1000193;
    }
    return hash;
  }

  private static String unescapeJava(String literal) {
    if (literal.indexOf('\\') == -1) {
      return literal;
    }
    final StringBuilder sb = new StringBuilder(literal.length());
    for (int i = 0; i < literal.length(); i++) {
      char ch = literal.charAt(i);
      if (ch == '\\' && i + 1 < literal.length()) {
        final char next = literal.charAt(++i);
        switch (next) {
          case 'n':
            ch = '\n';
            break;
          case 't':
            ch = '\t';
            break;
          case 'r':
            ch = '\r';
            break;
          case 'b':
            ch = '\b';
            break;
          case 'f':
            ch = '\f';
            break;
          default:
            ch = next;
        }
      }
      sb.append(ch);
    }
    return sb.toString();
  }

  /** Return an upper snake case constant name for the given json property name. */
  static String constantName(String name) {
    final StringBuilder sb = new StringBuilder(name.length() + 4);
    char prev = 0;
    for (int i = 0; i < name.length(); i++) {
      final char ch = name.charAt(i);
      if (!Character.isJavaIdentifierPart(ch) || ch == '$') {
        sb.append('_');
      } else {
        if (Character.isUpperCase(ch) && (Character.isLowerCase(prev) || Character.isDigit(prev))) {
          sb.append('_');
        }
        sb.append(Character.toUpperCase(ch));
      }
      prev = ch;
    }
    if (sb.length() == 0 || !Character.isJavaIdentifierStart(sb.charAt(0)) || "_".equals(sb.toString())) {
      sb.insert(0, "F_");
    }
    return sb.toString();
  }

  static String initCap(String input) {
    if (input.length() < 2) {
      return input.toUpperCase();
    }
    return Character.toUpperCase(input.charAt(0)) + input.substring(1);
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
      .filter(t -> t.toString().contains("io.avaje.json.JsonAdapter"))
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
    }
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
