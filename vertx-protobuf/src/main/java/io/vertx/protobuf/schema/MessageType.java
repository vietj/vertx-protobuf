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
  default Field unknownField(int number, TypeID typeId, WireType wireType) {
    return new UnknownField(this, number, typeId, wireType);
  }
 }
