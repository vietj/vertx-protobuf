package io.vertx.grpc.plugin.schema;

public class MessageTypeDeclaration {

  public final String identifier;
  public final String name;

  public MessageTypeDeclaration(String identifier, String name) {
    this.identifier = identifier;
    this.name = name;
  }
}
