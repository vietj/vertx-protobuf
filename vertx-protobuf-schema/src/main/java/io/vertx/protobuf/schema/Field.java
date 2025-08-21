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
  String name();
  String jsonName();
  default boolean isUnknown() {
    return false;
  }
}
