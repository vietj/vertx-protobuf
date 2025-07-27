package io.vertx.protobuf;


import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.ScalarType;

public interface Visitor {

  void init(MessageType type);

  void visitVarInt32(Field field, int v);

  default void visitVarInt64(Field field, long v) {
    throw new UnsupportedOperationException();
  }

  void visitString(Field field, String s);

  default void visitBytes(Field field, byte[] bytes) {
    throw new UnsupportedOperationException(getClass().getName() + " implement me");
  }

  default void visitI64(Field field, long value) {
    switch (field.type.id()) {
      case DOUBLE:
        visitDouble(field, Double.longBitsToDouble(value));
        break;
      case FIXED64:
        visitFixed64(field, value);
        break;
      case SFIXED64:
        visitSFixed64(field, value);
        break;
      default:
        throw new AssertionError();
    }
  }

  default void visitDouble(Field field, double d) {
    visitI64(field, Double.doubleToRawLongBits(d));
  }

  default void visitFixed64(Field field, long v) {
    visitI64(field, v);
  }

  default void visitSFixed64(Field field, long v) {
    visitI64(field, v);
  }

  default void visitFixed32(Field field, int v) {
    throw new UnsupportedOperationException();
  }

  default void visitFloat(Field field, float f) {
    throw new UnsupportedOperationException();
  }



  default void visitSFixed32(Field field, int v) {
    throw new UnsupportedOperationException();
  }

  void enter(Field field);

  void leave(Field field);

  void destroy();

}
