package io.vertx.protobuf.schema;

public class DefaultFieldBuilder {

  int number;
  String name;
  String jsonName;
  Type type;
  Boolean packed;
  boolean repeated;
  boolean map;
  boolean mapKey;
  boolean mapValue;

  public DefaultFieldBuilder number(int number) {
    this.number = number;
    return this;
  }

  public DefaultFieldBuilder name(String name) {
    this.name = name;
    return this;
  }

  public DefaultFieldBuilder jsonName(String jsonName) {
    this.jsonName = jsonName;
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

  public DefaultFieldBuilder map(boolean map) {
    this.map = map;
    return this;
  }

  public DefaultFieldBuilder mapKey(boolean mapKey) {
    this.mapKey = mapKey;
    return this;
  }

  public DefaultFieldBuilder mapValue(boolean mapValue) {
    this.mapValue = mapValue;
    return this;
  }
}
