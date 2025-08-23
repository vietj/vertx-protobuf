package io.vertx.protobuf.schema;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

public class DefaultEnumType implements EnumType {

  private final String name;
  private final Map<String, Integer> numberByName = new HashMap<>();
  private final Map<Integer, String> nameByNumber = new HashMap<>();

  public DefaultEnumType(String name) {
    this.name = name;
  }

  public DefaultEnumType() {
    this.name = null;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public TypeID id() {
    return TypeID.ENUM;
  }

  public DefaultEnumType addValue(int number, String name) {
    numberByName.put(name, number);
    nameByNumber.put(number, name);
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

  @Override
  public String nameOf(int number) {
    return nameByNumber.get(number);
  }
}
