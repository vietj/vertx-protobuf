package io.vertx.protobuf.schema;

public interface Schema {
  MessageType of(String messageName);
  MessageType peek(String messageName);
}
