package io.vertx.protobuf.schema;

import java.util.Collection;

public interface MessageType extends Type {
  @Override
  default TypeID id() {
    return TypeID.MESSAGE;
  }
  @Override
  default WireType wireType() {
    return WireType.LEN;
  }
  String name();
  default String packageName() {
    return null;
  }
  default String javaPackageName() {
    return null;
  }
  Field field(int number);
  default Collection<? extends Field> fields() {
    throw new UnsupportedOperationException();
  }
  default Field fieldByName(String name) {
    return null;
  }
  default Field fieldByJsonName(String jsonName) {
    return null;
  }
  default Field unknownField(int number, WireType wireType) {
    return new UnknownField(this, number, wireType);
  }
  default MessageType enclosingType() {
    return null;
  }
 }
