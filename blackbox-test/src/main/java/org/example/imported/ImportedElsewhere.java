package org.example.imported;

/**
 * This record is imported by the blackbox-test-cascade project
 *   and should be imported into a different package entirely
 */
public record ImportedElsewhere(
  String someField,
  int someOtherField,
  AcrossCascade acrossCascade
) {
}
