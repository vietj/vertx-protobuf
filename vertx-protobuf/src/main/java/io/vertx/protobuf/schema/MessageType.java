package io.vertx.protobuf.schema;

public interface MessageType extends Type {
  String name();
  Field field(int number);
 }
