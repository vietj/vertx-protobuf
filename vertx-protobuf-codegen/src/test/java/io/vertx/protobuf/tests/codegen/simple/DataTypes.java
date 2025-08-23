package io.vertx.protobuf.tests.codegen.simple;

import io.vertx.protobuf.annotations.ProtoField;
import io.vertx.protobuf.annotations.ProtoMessage;

@ProtoMessage
public class DataTypes {

  @ProtoField(number = 1, name = "string_field")
  public String getStringField() {
    throw new UnsupportedOperationException();
  }

  @ProtoField(number = 2, name = "long_field")
  public Long getLongField() {
    throw new UnsupportedOperationException();
  }
}
