package io.vertx.grpc.plugin.schema;

public class FieldDeclaration {

  public final String identifier;
  public final String name;
  public final String jsonName;
  public final boolean map;
  public final boolean mapKey;
  public final boolean mapValue;
  public final boolean repeated;
  public final boolean packed;
  public final String messageTypeIdentifier;
  public final String messageName;
  public final int number;
  public final String typeExpr;

  public FieldDeclaration(String identifier, String name, boolean map, boolean mapKey, boolean mapValue, boolean repeated, boolean packed, String jsonName, String messageTypeIdentifier, int number, String messageName, String typeExpr) {
    this.identifier = identifier;
    this.name = name;
    this.jsonName = jsonName;
    this.messageTypeIdentifier = messageTypeIdentifier;
    this.messageName = messageName;
    this.map = map;
    this.mapKey = mapKey;
    this.mapValue = mapValue;
    this.repeated = repeated;
    this.packed = packed;
    this.number = number;
    this.typeExpr = typeExpr;
  }
}
