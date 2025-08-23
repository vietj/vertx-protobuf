package io.vertx.protobuf.tests.codegen.simple;

import io.vertx.protobuf.codegen.annotations.ProtoField;
import io.vertx.protobuf.codegen.annotations.ProtoMessage;

@ProtoMessage
public class SimpleBean {

  private String stringField;

  @ProtoField(number = 1, name = "string_field")
  public String getStringField() {
    return stringField;
  }

  public SimpleBean setStringField(String stringField) {
    this.stringField = stringField;
    return this;
  }
}
