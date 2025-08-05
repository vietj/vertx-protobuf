package io.vertx.protobuf.schema;

import java.util.Objects;

public class DefaultField implements Field {

  private final DefaultMessageType owner;
  private final int number;
  private final String name;
  private final Type type;

  DefaultField(DefaultMessageType owner, int number, String name, Type type) {
    this.owner = owner;
    this.number = number;
    this.name = name;
    this.type = type;
  }

  public MessageType owner() {
    return owner;
  }

  @Override
  public boolean isRepeated() {
    return false;
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
