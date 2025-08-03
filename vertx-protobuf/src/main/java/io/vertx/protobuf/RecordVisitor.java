package io.vertx.protobuf;


import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;

import static io.vertx.protobuf.ProtobufReader.decodeSInt32;
import static io.vertx.protobuf.ProtobufReader.decodeSInt64;
import static io.vertx.protobuf.ProtobufWriter.encodeSInt32;
import static io.vertx.protobuf.ProtobufWriter.encodeSInt64;

public interface RecordVisitor {

  void init(MessageType type);

  void destroy();

  default void visitVarInt32(Field field, int v) {
    switch (field.type().id()) {
      case INT32:
        visitInt32(field, v);
        break;
      case UINT32:
        visitUInt32(field, v);
        break;
      case SINT32:
        visitSInt32(field, decodeSInt32(v));
        break;
      case BOOL:
        visitBool(field, v != 0);
        break;
      case ENUM:
        visitEnum(field, v);
        break;
      default:
        throw new IllegalArgumentException();
    }
  }

  default void visitInt32(Field field, int v) {
    visitVarInt32(field, v);
  }

  default void visitUInt32(Field field, int v) {
    visitVarInt32(field, v);
  }

  default void visitSInt32(Field field, int v) {
    visitVarInt32(field, encodeSInt32(v));
  }

  default void visitBool(Field field, boolean v) {
    visitVarInt32(field, v ? 1 : 0);
  }

  default void visitEnum(Field field, int number) {
    visitInt32(field, number);
  }

  //

  default void visitVarInt64(Field field, long v) {
    switch (field.type().id()) {
      case INT64:
        visitInt64(field, v);
        break;
      case UINT64:
        visitUInt64(field, v);
        break;
      case SINT64:
        visitSInt64(field, decodeSInt64(v));
        break;
      default:
        throw new IllegalArgumentException();
    }
  }

  default void visitInt64(Field field, long v) {
    visitVarInt64(field, v);
  }

  default void visitUInt64(Field field, long v) {
    visitVarInt64(field, v);
  }

  default void visitSInt64(Field field, long v) {
    visitVarInt64(field, encodeSInt64(v));
  }

  // I64

  default void visitI64(Field field, long value) {
    switch (field.type().id()) {
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

  // I32

  default void visitI32(Field field, int value) {
    switch (field.type().id()) {
      case FLOAT:
        visitFloat(field, Float.intBitsToFloat(value));
        break;
      case FIXED32:
        visitFixed32(field, value);
        break;
      case SFIXED32:
        visitSFixed32(field, value);
        break;
      default:
        throw new AssertionError();
    }
  }

  default void visitFloat(Field field, float f) {
    visitI32(field, Float.floatToRawIntBits(f));
  }

  default void visitFixed32(Field field, int v) {
    visitI32(field, v);
  }

  default void visitSFixed32(Field field, int v) {
    visitI32(field, v);
  }

  // LEN

  void enter(Field field);

  void leave(Field field);

  void visitString(Field field, String s);

  void visitBytes(Field field, byte[] bytes);

}
