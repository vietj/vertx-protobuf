package io.vertx.protobuf.schema;

import java.util.HashMap;
import java.util.Map;

public class DefaultMessageType implements MessageType {

  private final String name;
  private final Map<Integer, DefaultField> fields = new HashMap<>();

  public DefaultMessageType(String name) {
    this.name = name;
  }

  public String name() {
    return name;
  }

  @Override
  public TypeID id() {
    return TypeID.MESSAGE;
  }

  @Override
  public WireType wireType() {
    return WireType.LEN;
  }

  public DefaultField addField(int number, Type type) {
    DefaultField field = new DefaultField(this, number, type);
    fields.put(number, field);
    return field;
  }

  public DefaultField field(int number) {
    return fields.get(number);
  }

  @Override
  public Field field(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "MessageType[name=" + name + "]";
  }
}
