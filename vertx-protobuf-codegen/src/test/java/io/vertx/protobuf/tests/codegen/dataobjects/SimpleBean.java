package io.vertx.protobuf.tests.codegen.dataobjects;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.protobuf.codegen.annotations.ProtobufGen;

@DataObject
@ProtobufGen
public class SimpleBean {

  private String stringField;

  public String getStringField() {
    return stringField;
  }

  public SimpleBean setStringField(String stringField) {
    this.stringField = stringField;
    return this;
  }
}
