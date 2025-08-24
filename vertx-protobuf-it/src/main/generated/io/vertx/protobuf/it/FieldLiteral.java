package io.vertx.protobuf.it;

import io.vertx.protobuf.schema.Schema;
import io.vertx.protobuf.schema.DefaultSchema;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.DefaultMessageType;
import io.vertx.protobuf.schema.ScalarType;
import io.vertx.protobuf.schema.EnumType;
import io.vertx.protobuf.schema.DefaultEnumType;
import io.vertx.protobuf.schema.Field;

public enum FieldLiteral implements Field {

  SimpleMessage_string_field(1, false, false, false, false, false, "string_field", "stringField");
  private MessageLiteral owner;
  private io.vertx.protobuf.schema.Type type;
  private final int number;
  private final boolean map;
  private final boolean mapKey;
  private final boolean mapValue;
  private final boolean repeated;
  private final boolean packed;
  private final String name;
  private final String jsonName;
  FieldLiteral(int number, boolean map, boolean mapKey, boolean mapValue, boolean repeated, boolean packed, String name, String jsonName) {
    this.number = number;
    this.map = map;
    this.mapKey = mapKey;
    this.mapValue = mapValue;
    this.repeated = repeated;
    this.packed = packed;
    this.name = name;
    this.jsonName = jsonName;
  }
  public MessageType owner() {
    return owner;
  }
  public int number() {
    return number;
  }
  public String jsonName() {
    return jsonName;
  }
  public boolean isMap() {
    return map;
  }
  public boolean isMapKey() {
    return mapKey;
  }
  public boolean isMapValue() {
    return mapValue;
  }
  public boolean isRepeated() {
    return repeated;
  }
  public boolean isPacked() {
    return packed;
  }
  public io.vertx.protobuf.schema.Type type() {
    return type;
  }
  static {
    FieldLiteral.SimpleMessage_string_field.owner = MessageLiteral.SimpleMessage;
    FieldLiteral.SimpleMessage_string_field.type = ScalarType.STRING;
  }
}
