package io.vertx.protobuf.schema;

public class DefaultFieldBuilder {

  int number;
  String name;
  Type type;
  Boolean packed;
  boolean repeated;

  public DefaultFieldBuilder number(int number) {
    this.number = number;
    return this;
  }

  public DefaultFieldBuilder name(String name) {
    this.name = name;
    return this;
  }

  public DefaultFieldBuilder type(Type type) {
    this.type = type;
    return this;
  }

  public DefaultFieldBuilder packed(boolean packed) {
    this.packed = packed;
    return this;
  }

  public DefaultFieldBuilder repeated(boolean repeated) {
    this.repeated = repeated;
    return this;
  }
}
