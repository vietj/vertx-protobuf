package io.vertx.protobuf.it;

import io.vertx.protobuf.schema.Schema;
import io.vertx.protobuf.schema.DefaultSchema;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.DefaultMessageType;
import io.vertx.protobuf.schema.ScalarType;
import io.vertx.protobuf.schema.EnumType;
import io.vertx.protobuf.schema.DefaultEnumType;
import io.vertx.protobuf.schema.Field;
import java.util.Map;
import java.util.HashMap;
import java.util.OptionalInt;

public enum EnumLiteral implements EnumType {

  ;
  private final Map<String, Integer> numberByName = new HashMap<>();
  private final Map<Integer, String> nameByNumber = new HashMap<>();
  EnumLiteral() {
  }
  public OptionalInt numberOf(String name) {
    Integer number = numberByName.get(name);
    return number == null ? OptionalInt.empty() : OptionalInt.of(number);
  }
  public String nameOf(int number) {
    return nameByNumber.get(number);
  }
  static {
  }
}
