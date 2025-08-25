package io.vertx.protobuf.tests.codegen.embedding;

import io.vertx.protobuf.lang.MessageBase;
import io.vertx.protobuf.lang.ProtoField;
import io.vertx.protobuf.lang.ProtoMessage;

@ProtoMessage
public class Embedding extends MessageBase {

  private String stringField;
  private Embedded embeddedField;

  @ProtoField(number = 1, name = "string_field")
  public String getStringField() {
    return stringField;
  }

  public void setStringField(String s) {
    this.stringField = s;
  }

  @ProtoField(number = 2, name = "embedded_field")
  public Embedded getEmbeddedField() {
    return embeddedField;
  }

  public void setEmbeddedField(Embedded embeddedField) {
    this.embeddedField = embeddedField;
  }
}
