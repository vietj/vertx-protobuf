package io.vertx.protobuf.it;

import io.vertx.protobuf.lang.ProtoField;
import io.vertx.protobuf.lang.ProtoMessage;

@ProtoMessage
public class SimpleMessage {

  private String stringField;

  @ProtoField(number = 1, name = "string_field")
  public String getStringField() {
    return stringField;
  }

  public SimpleMessage setStringField(String stringField) {
    this.stringField = stringField;
    return this;
  }
}
