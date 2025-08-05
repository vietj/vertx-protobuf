package io.vertx.protobuf.schema;

class UnknownField implements Field, Type {
  final MessageType owner;
  final int number;
  final WireType wireType;

  UnknownField(MessageType owner, int number,WireType wireType) {
    this.owner = owner;
    this.number = number;
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
    switch (wireType) {
      case LEN:
        return TypeID.BYTES;
      case I32:
        return TypeID.FIXED32;
      case I64:
        return TypeID.FIXED64;
      case VARINT:
        return TypeID.INT64;
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public String jsonName() {
    return null;
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
  public boolean isRepeated() {
    return false;
  }

  @Override
  public int hashCode() {
    return number;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof Field) {
      Field that = (Field) obj;
      return number == that.number() && type().id() == that.type().id() && wireType == that.type().wireType();
    }
    return false;
  }
}
