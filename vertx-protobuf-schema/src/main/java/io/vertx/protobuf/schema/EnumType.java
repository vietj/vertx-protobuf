package io.vertx.protobuf.schema;

import java.util.OptionalInt;

public interface EnumType extends Type {

  @Override
  default TypeID id() {
    return TypeID.ENUM;
  }
  @Override
  default WireType wireType() {
    return WireType.VARINT;
  }

  String name();

  OptionalInt numberOf(String name);

  String nameOf(int number);

}
