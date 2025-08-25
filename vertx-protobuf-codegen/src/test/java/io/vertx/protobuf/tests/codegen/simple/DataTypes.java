package io.vertx.protobuf.tests.codegen.simple;

import io.vertx.protobuf.lang.MessageBase;
import io.vertx.protobuf.lang.ProtoField;
import io.vertx.protobuf.lang.ProtoMessage;

@ProtoMessage
public class DataTypes extends MessageBase {

  private String stringField;
  private TestEnum enumField;

  @ProtoField(number = 1, name = "string_field")
  public String getStringField() {
    return stringField;
  }

  public void setStringField(String s) {
    this.stringField = s;
  }

  @ProtoField(number = 2, name = "long_field")
  public Long getLongField() {
    return 0L;
  }

  public void setLongField(Long s) {
    throw new UnsupportedOperationException();
  }

  @ProtoField(number = 3, name = "boolean_field")
  public Boolean getBooleanField() {
    return Boolean.FALSE;
  }

  public void setBooleanField(Boolean b) {
    throw new UnsupportedOperationException();
  }

  @ProtoField(number = 4, name = "enum_field")
  public TestEnum getEnumField() {
    return enumField;
  }

  public void setEnumField(TestEnum e) {
    this.enumField = e;
  }
}
