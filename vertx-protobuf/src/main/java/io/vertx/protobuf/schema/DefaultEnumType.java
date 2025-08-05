package io.vertx.protobuf.schema;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

public class DefaultEnumType implements EnumType {

  private final Map<String, Integer> numberByName = new HashMap<>();

  @Override
  public TypeID id() {
    return TypeID.ENUM;
  }

  public DefaultEnumType addValue(int number, String name) {
    numberByName.put(name, number);
    return this;
  }

  @Override
  public WireType wireType() {
    return WireType.VARINT;
  }

  @Override
  public OptionalInt numberOf(String name) {
    Integer number = numberByName.get(name);
    return number != null ? OptionalInt.of(number) : OptionalInt.empty();
  }
}
