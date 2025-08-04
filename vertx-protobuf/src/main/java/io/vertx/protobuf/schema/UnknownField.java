package io.vertx.protobuf.schema;

class UnknownField implements Field, Type {
  final MessageType owner;
  final int number;
  final TypeID typeId;
  final WireType wireType;

  UnknownField(MessageType owner, int number,TypeID typeId, WireType wireType) {
    this.owner = owner;
    this.number = number;
    this.typeId = typeId;
    this.wireType = wireType;
  }

  @Override
  public MessageType owner() {
    return owner;
  }

  @Override
  public int number() {
    return number;
  }

  @Override
  public Type type() {
    return this;
  }

  @Override
  public TypeID id() {
    return typeId;
  }

  @Override
  public WireType wireType() {
    return wireType;
  }

  @Override
  public boolean isUnknown() {
    return true;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof Field) {
      Field that = (Field) obj;
      return number == that.number() && typeId == that.type().id() && wireType == that.type().wireType();
    }
    return false;
  }
}
