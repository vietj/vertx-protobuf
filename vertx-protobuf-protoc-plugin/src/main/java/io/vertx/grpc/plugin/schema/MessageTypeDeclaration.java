package io.vertx.grpc.plugin.schema;

public class MessageTypeDeclaration {

  public final String identifier;
  public final String name;
  public final String className;

  public MessageTypeDeclaration(String identifier, String name, String className) {
    this.identifier = identifier;
    this.name = name;
    this.className = className;
  }
}
