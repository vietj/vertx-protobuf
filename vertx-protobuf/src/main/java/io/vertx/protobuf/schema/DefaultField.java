package io.vertx.protobuf.schema;

public class DefaultField implements Field {

  private final DefaultMessageType owner;
  private final int number;
  private final String name;
  private final String jsonName;
  private final boolean map;
  private final boolean repeated;
  private final boolean packed;
  private final Type type;

  DefaultField(DefaultMessageType owner, int number, String name, String jsonName, boolean map, boolean repeated, boolean packed, Type type) {
    this.owner = owner;
    this.number = number;
    this.jsonName = jsonName;
    this.name = name;
    this.repeated = repeated;
    this.map = map;
    this.packed = packed;
    this.type = type;
  }

  public MessageType owner() {
    return owner;
  }

  @Override
  public String jsonName() {
    return jsonName;
  }

  @Override
  public boolean isPacked() {
    return packed;
  }

  @Override
  public boolean isMap() {
    return map;
  }

  @Override
  public boolean isRepeated() {
    return repeated;
  }

  public int number() {
    return number;
  }

  public Type type() {
    return type;
  }

  public String toString() {
    return "Field[number=" + number + ",type=" + type + ",owner=" + owner.name() + "]";
  }
}
