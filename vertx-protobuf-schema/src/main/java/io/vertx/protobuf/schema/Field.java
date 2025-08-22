package io.vertx.protobuf.schema;

public interface Field {

  MessageType owner();
  int number();
  Type type();
  default boolean isOptional() {
    return false;
  }
  boolean isRepeated();
  default boolean isMap() {
    return false;
  }
  boolean isMapKey();
  boolean isMapValue();
  boolean isPacked();
  String name();
  String jsonName();
  default boolean isUnknown() {
    return false;
  }
  default OneOf oneOf() {
    return null;
  }
}
