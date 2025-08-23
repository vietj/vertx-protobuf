package test.proto;

import io.vertx.protobuf.schema.Schema;
import io.vertx.protobuf.schema.DefaultSchema;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.DefaultMessageType;
import io.vertx.protobuf.schema.ScalarType;
import io.vertx.protobuf.schema.EnumType;
import io.vertx.protobuf.schema.DefaultEnumType;
import io.vertx.protobuf.schema.Field;

public enum MessageLiteral implements MessageType {

    SimpleMessage("SimpleMessage");
  final java.util.Map<Integer, FieldLiteral> byNumber;
  final java.util.Map<String, FieldLiteral> byJsonName;
  final java.util.Map<String, FieldLiteral> byName;
  MessageLiteral(String name) {
    this.byNumber = new java.util.HashMap<>();
    this.byJsonName = new java.util.HashMap<>();
    this.byName = new java.util.HashMap<>();
  }
  public Field field(int number) {
    return byNumber.get(number);
  }
  public Field fieldByJsonName(String name) {
    return byJsonName.get(name);
  }
  public Field fieldByName(String name) {
    return byName.get(name);
  }
  static {
    MessageLiteral.SimpleMessage.byNumber.put(1, FieldLiteral.SimpleMessage_string_field);
    MessageLiteral.SimpleMessage.byJsonName.put("stringField", FieldLiteral.SimpleMessage_string_field);
    MessageLiteral.SimpleMessage.byName.put("string_field", FieldLiteral.SimpleMessage_string_field);
  }
}
