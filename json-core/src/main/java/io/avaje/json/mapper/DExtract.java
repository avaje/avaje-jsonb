package io.avaje.json.mapper;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

final class DExtract implements JsonExtract {

  private static final Pattern PATH_PATTERN = Pattern.compile("\\.");

  private final Map<String, Object> map;

  DExtract(Map<String, Object> map) {
    this.map = map;
  }

  @SuppressWarnings("unchecked")
  private Object find(String path, Map<String, Object> map) {
    final String[] paths = PATH_PATTERN.split(path, 2);
    final Object child = map.get(paths[0]);
    if (child == null || paths.length == 1) {
      return child;
    }
    if (child instanceof Map) {
      return find(paths[1], (Map<String, Object>) child);
    }
    return null;
  }

  @Override
  public String extract(String path) {
    final Object node = find(path, map);
    if (node == null) {
      throw new IllegalArgumentException("Node not present for " + path);
    }
    return node.toString();
  }

  @Override
  public Optional<String> extractOrEmpty(String path) {
    final Object name = find(path, map);
    return name == null ? Optional.empty() : Optional.of(name.toString());
  }

  @Override
  public String extract(String path, String missingValue) {
    final Object name = find(path, map);
    return name == null ? missingValue : name.toString();
  }

  @Override
  public int extract(String path, int missingValue) {
    final Object node = find(path, map);
    return !(node instanceof Number)
      ? missingValue
      : ((Number) node).intValue();
  }

  @Override
  public long extract(String path, long missingValue) {
    final Object node = find(path, map);
    return !(node instanceof Number)
      ? missingValue
      : ((Number) node).longValue();
  }

  @Override
  public double extract(String path, double missingValue) {
    final Object node = find(path, map);
    return !(node instanceof Number)
      ? missingValue
      : ((Number) node).doubleValue();
  }

  @Override
  public boolean extract(String path, boolean missingValue) {
    final Object node = find(path, map);
    return !(node instanceof Boolean)
      ? missingValue
      : (Boolean) node;
  }
}
