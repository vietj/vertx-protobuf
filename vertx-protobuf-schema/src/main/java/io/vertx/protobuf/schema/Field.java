package io.vertx.protobuf.schema;

public interface Field {

  MessageType owner();
  int number();
  Type type();
  boolean isRepeated();
  boolean isMap();
  boolean isMapKey();
  boolean isMapValue();
  boolean isPacked();
  String jsonName();
  default boolean isUnknown() {
    return false;
  }
}
