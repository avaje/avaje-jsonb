package io.avaje.jsonb.generator;

import java.util.Map;

final class SubTypeRequest {

  private final String typeVar;
  private final ClassReader beanReader;
  private final boolean useSwitch;
  private final boolean useEnum;
  private final Map<String, Integer> frequencyMap;
  private final Map<String, Boolean> commonFieldMap;

  SubTypeRequest(String typeVar, ClassReader beanReader, boolean useSwitch, boolean useEnum, Map<String, Integer> frequencyMap, Map<String, Boolean> commonFieldMap) {
    this.typeVar = typeVar;
    this.beanReader = beanReader;
    this.useSwitch = useSwitch;
    this.useEnum = useEnum;
    this.frequencyMap = frequencyMap;
    this.commonFieldMap = commonFieldMap;
  }

  String typeVar() {
    return typeVar;
  }

  ClassReader beanReader() {
    return beanReader;
  }

  boolean useSwitch() {
    return useSwitch;
  }

  boolean useEnum() {
    return useEnum;
  }

  boolean isCommonField(String paramName) {
    return Boolean.TRUE.equals(commonFieldMap.get(paramName));
  }

  String frequencySuffix(String constructParamName) {
    var frequency = frequencyMap.compute(constructParamName, (k, v) -> v == null ? 0 : v + 1);
    return frequency == 0 ? "" : frequency.toString();
  }
}
