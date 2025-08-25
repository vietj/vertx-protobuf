package io.vertx.protobuf.it;

import io.vertx.protobuf.lang.MessageBase;
import io.vertx.protobuf.lang.ProtoField;
import io.vertx.protobuf.lang.ProtoMessage;

@ProtoMessage
public class SimpleMessage extends MessageBase {

  private String stringField;
  private long longField;

  @ProtoField(number = 1, name = "string_field")
  public String getStringField() {
    return stringField;
  }

  public void setStringField(String stringField) {
    this.stringField = stringField;
  }

  @ProtoField(number = 2, name = "long_field")
  public long getLongField() {
    return longField;
  }

  public void setLongField(long stringField) {
    this.longField = longField;
  }
}
