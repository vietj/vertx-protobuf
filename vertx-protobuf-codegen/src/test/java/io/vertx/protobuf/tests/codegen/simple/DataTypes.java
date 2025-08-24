package io.vertx.protobuf.tests.codegen.simple;

import io.vertx.protobuf.annotations.ProtoField;
import io.vertx.protobuf.annotations.ProtoMessage;

import java.util.concurrent.TimeUnit;

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

  @ProtoField(number = 3, name = "boolean_field")
  public Boolean getBooleanField() {
    throw new UnsupportedOperationException();
  }

  @ProtoField(number = 4, name = "enum_field")
  public TestEnum getEnumField() {
    throw new UnsupportedOperationException();
  }
}
