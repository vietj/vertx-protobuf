package io.vertx.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.schema.MessageType;

public interface UnknownRecordVisitor {

  default void visitUnknownLengthDelimited(MessageType messageType, int fieldNumber, Buffer payload) {
  }

  default void visitUnknownI32(MessageType messageType, int fieldNumber, int value) {
  }

  default void visitUnknownI64(MessageType messageType, int fieldNumber, long value) {
  }

  default void visitUnknownVarInt(MessageType messageType, int fieldNumber, long value) {
  }
}
