package io.vertx.protobuf.schema;

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
  Field field(int number);
 }
