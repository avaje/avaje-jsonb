package org.cascade.importing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ImportingToDifferentPackageTest {

  @Test
  void adaptorCreatedInCorrectLocation() {
    assertDoesNotThrow(() -> Class.forName("org.cascade.broughtin.ImportedElsewhereJsonAdapter"));
    assertDoesNotThrow(() -> Class.forName("org.cascade.broughtin.AcrossCascadeJsonAdapter"));
    assertThrows(ClassNotFoundException.class, () -> Class.forName("org.example.imported.ImportedElsewhereJsonAdapter"));
    assertThrows(ClassNotFoundException.class, () -> Class.forName("org.example.imported.AcrossCascadeJsonAdapter"));
    assertThrows(ClassNotFoundException.class, () -> Class.forName("custom.jsonb.AcrossCascadeJsonAdapter"));
    assertThrows(ClassNotFoundException.class, () -> Class.forName("org.cascade.importing.ImportedElsewhereJsonAdapter"));
    assertThrows(ClassNotFoundException.class, () -> Class.forName("org.cascade.importing.ImportingFromBlackboxTestJsonAdapter"));
  }

}
