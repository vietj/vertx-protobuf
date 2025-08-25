package io.vertx.protobuf.it;
import io.vertx.protobuf.ProtoVisitor;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.Field;

public class ProtoWriter {
  public static void emit(io.vertx.protobuf.it.SimpleMessage value, ProtoVisitor visitor) {
    visitor.init(MessageLiteral.SimpleMessage);
    visit(value, visitor);
    visitor.destroy();
  }
  public static io.vertx.protobuf.ProtoStream streamOf(io.vertx.protobuf.it.SimpleMessage value) {
    return visitor -> {
    emit(value, visitor);
    };
  }
  public static void visit(io.vertx.protobuf.it.SimpleMessage value, ProtoVisitor visitor) {
    if (!value.getStringField().isEmpty()) {
      java.lang.String v = value.getStringField();
      visitor.visitString(FieldLiteral.SimpleMessage_string_field, v);
    }
    if (value.getLongField() != 0L) {
      java.lang.Long v = value.getLongField();
      visitor.visitInt64(FieldLiteral.SimpleMessage_long_field, v);
    }
    java.lang.Iterable<java.util.Map.Entry<io.vertx.protobuf.schema.Field, java.util.List<Object>>> unknownFields = value.unknownFields();
    if (unknownFields != null) {
      for (java.util.Map.Entry<io.vertx.protobuf.schema.Field, java.util.List<Object>> unknownField : unknownFields) {
        for (Object o : unknownField.getValue()) {
          io.vertx.protobuf.schema.Field field = unknownField.getKey();
          switch (field.type().wireType()) {
            case LEN:
              visitor.visitBytes(field, ((io.vertx.core.buffer.Buffer)o).getBytes());
              break;
            case I32:
              visitor.visitFixed32(field, (Integer)o);
              break;
            case I64:
              visitor.visitFixed64(field, (Long)o);
              break;
            case VARINT:
              visitor.visitInt64(field, (Long)o);
              break;
          }
        }
      }
    }
  }
}
