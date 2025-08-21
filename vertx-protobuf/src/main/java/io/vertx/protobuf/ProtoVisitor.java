package io.vertx.protobuf;


import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;

public interface ProtoVisitor {

  void init(MessageType type);

  void destroy();

  void visitInt32(Field field, int v);

  void visitUInt32(Field field, int v);

  void visitSInt32(Field field, int v);

  void visitEnum(Field field, int number);

  void visitInt64(Field field, long v);

  void visitUInt64(Field field, long v);

  void visitSInt64(Field field, long v);

  void visitBool(Field field, boolean v);

  void visitDouble(Field field, double d);

  void visitFixed64(Field field, long v);

  void visitSFixed64(Field field, long v);

  void visitFloat(Field field, float f);

  void visitFixed32(Field field, int v);

  void visitSFixed32(Field field, int v);

  // LEN

  void enter(Field field);

  void leave(Field field);

  void visitString(Field field, String s);

  void visitBytes(Field field, byte[] bytes);

  void enterPacked(Field field);

  void leavePacked(Field field);


}
