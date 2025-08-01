package io.vertx.protobuf.schema;

public class DefaultField implements Field {

  private final MessageType owner;
  private final int number;
  private final Type type;

  public DefaultField(MessageType owner, int number, Type type) {
    this.owner = owner;
    this.number = number;
    this.type = type;
  }

  public MessageType owner() {
    return owner;
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
