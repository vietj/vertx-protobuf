package io.vertx.protobuf.schema;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class DefaultMessageType implements MessageType {

  private final String name;
  private final Map<Integer, DefaultField> fields = new HashMap<>();
  private final Map<String, DefaultField> byName = new HashMap<>();
  private final Map<String, DefaultField> byJsonName = new HashMap<>();

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

  public DefaultField addField(int number, String name, Type type) {
    String jsonName = Field.toJsonName(name);
    DefaultField field = new DefaultField(this, number, name, type);
    if (byName.containsKey(name)) {
      throw new IllegalStateException("Duplicate field " + name);
    }
    if (byJsonName.containsKey(jsonName)) {
      throw new IllegalStateException("Duplicate field " + name);
    }
    fields.put(number, field);
    byName.put(name, field);
    byJsonName.put(jsonName, field);
    return field;
  }

  public DefaultField addField(int number, Type type) {
    DefaultField field = new DefaultField(this, number, null, type);
    fields.put(number, field);
    return field;
  }

  public DefaultField field(int number) {
    return fields.get(number);
  }

  @Override
  public Field fieldByName(String jsonName) {
    return byName.get(jsonName);
  }

  @Override
  public Field fieldByJsonName(String jsonName) {
    return byJsonName.get(jsonName);
  }

  @Override
  public String toString() {
    return "MessageType[name=" + name + "]";
  }
}
