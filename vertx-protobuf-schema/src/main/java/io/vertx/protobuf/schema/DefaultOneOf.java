package io.vertx.protobuf.schema;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultOneOf implements OneOf {

  final DefaultMessageType owner;
  final String name;
  final Map<Integer, Field> fields = new LinkedHashMap<>();

  DefaultOneOf(DefaultMessageType owner, String name) {
    this.owner = owner;
    this.name = name;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Iterable<Field> fields() {
    return fields.values();
  }

  public DefaultOneOf add(DefaultField field) {
    if (field.owner() != owner) {
      throw new IllegalArgumentException();
    }
    if (fields.containsKey(field.number())) {
      throw new IllegalArgumentException();
    }
    if (field.oneOf() != null) {
      throw new IllegalArgumentException();
    }
    field.oneOf(this);
    fields.put(field.number(), field);
    return this;
  }
}
