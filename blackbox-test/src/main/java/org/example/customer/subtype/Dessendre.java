package org.example.customer.subtype;

import org.example.customer.subtype.Dessendre.Alicia;
import org.example.customer.subtype.Dessendre.Curator;
import org.example.customer.subtype.Dessendre.Paintress;

import io.avaje.jsonb.Json.SubType;

@io.avaje.jsonb.Json
@SubType(type = Alicia.class, name = "Maelle")
@SubType(type = Paintress.class, name = "Aline")
@SubType(type = Curator.class, name = "Renoir")
public sealed interface Dessendre {
  public record Alicia() implements Dessendre {}

  public record Paintress() implements Dessendre {}

  public record Curator() implements Dessendre {}
}
