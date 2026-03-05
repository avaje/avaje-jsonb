package org.cascade.importing;

import io.avaje.jsonb.Json;
import org.example.imported.ImportedElsewhere;

/**
 * This class is used to help test that we can override the package of imported elements
 */
@Json.Import(value = {ImportedElsewhere.class}, destinationPackage = "org.cascade.broughtin")
public class ImportingFromBlackboxTest {
}
