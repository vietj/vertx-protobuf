package io.vertx.protobuf.schema;

public class DefaultMapField extends DefaultField implements MapField {

  private Field key;
  private Field value;

  public DefaultMapField(DefaultMessageType owner, int number, String name, String jsonName, boolean mapKey, boolean mapValue, boolean repeated, boolean packed, boolean optional, Type type) {
    super(owner, number, name, jsonName, mapKey, mapValue, repeated, packed, optional, type);
  }

  @Override
  public Field key() {
    Field f = key;
    if (f == null) {
      f = ((MessageType)type()).field(1);
      key = f;
    }
    return f;
  }

  @Override
  public Field value() {
    Field f = value;
    if (f == null) {
      f = ((MessageType)type()).field(2);
      value = f;
    }
    return f;
  }
}
