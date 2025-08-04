package io.vertx.protobuf.schema;

public interface Field {
  MessageType owner();
  int number();
  Type type();
  default boolean isUnknown() {
    return false;
  }
}
