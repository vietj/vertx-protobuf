package io.vertx.protobuf;


import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;

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

  void visitDouble(Field field, double d);

  default void visitFixed32(Field field, int v) {
    throw new UnsupportedOperationException();
  }

  default void visitFixed64(Field field, long v) {
    throw new UnsupportedOperationException();
  }

  default void visitSFixed32(Field field, int v) {
    throw new UnsupportedOperationException();
  }

  default void visitSFixed64(Field field, long v) {
    throw new UnsupportedOperationException();
  }

  void enter(Field field);

  void leave(Field field);

  void destroy();

}
