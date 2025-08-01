package io.vertx.protobuf.schema;

import java.util.HashSet;
import java.util.Set;

public class DefaultEnumType implements EnumType {

  private Set<Integer> values = new HashSet<>();

  @Override
  public TypeID id() {
    return TypeID.ENUM;
  }

  @Override
  public WireType wireType() {
    return WireType.VARINT;
  }
}
