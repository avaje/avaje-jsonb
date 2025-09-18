package io.avaje.jsonb.generator.models.valid.naming;

import io.avaje.jsonb.Json;

@Json
public class PackageNaming {
  private final Package queryPackage;

  public PackageNaming(Package queryPackage) {
    this.queryPackage = queryPackage;
  }

  public Package getQueryPackage() {
    return queryPackage;
  }

  public static class Package {
    private final String ecosystem;

    public Package(String ecosystem) {
      this.ecosystem = ecosystem;
    }

    public String getEcosystem() {
      return ecosystem;
    }
  }
}
