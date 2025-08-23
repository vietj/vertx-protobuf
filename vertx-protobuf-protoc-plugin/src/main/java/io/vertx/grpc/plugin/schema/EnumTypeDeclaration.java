package io.vertx.grpc.plugin.schema;

import java.util.LinkedHashMap;
import java.util.Map;

public class EnumTypeDeclaration {

  public final String identifier;
  public final String name;
  public final Map<Integer, String> numberToIdentifier;

  public EnumTypeDeclaration(String identifier, String name) {
    this.identifier = identifier;
    this.name = name;
    this.numberToIdentifier = new LinkedHashMap<>();
  }
}
