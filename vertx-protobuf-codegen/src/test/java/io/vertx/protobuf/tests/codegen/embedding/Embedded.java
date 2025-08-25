package io.vertx.protobuf.tests.codegen.embedding;

import io.vertx.protobuf.lang.MessageBase;
import io.vertx.protobuf.lang.ProtoField;
import io.vertx.protobuf.lang.ProtoMessage;

@ProtoMessage
public class Embedded extends MessageBase {

  private String stringField;

  @ProtoField(number = 1, name = "string_field")
  public String getStringField() {
    return stringField;
  }

  public void setStringField(String s) {
    this.stringField = s;
  }

}
