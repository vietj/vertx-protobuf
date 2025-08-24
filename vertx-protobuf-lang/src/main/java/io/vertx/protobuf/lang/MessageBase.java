package io.vertx.protobuf.lang;

import io.vertx.protobuf.schema.Field;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class MessageBase {

  private Map<Field, List<Object>> unknownFields;

  public List<Object> unknownField(Field field) {
    if (unknownFields == null) {
      unknownFields = new LinkedHashMap<>();
    }
    return unknownFields.computeIfAbsent(field, f -> new ArrayList<>());
  }

  public Iterable<Map.Entry<Field, List<Object>>> unknownFields() {
    Map<Field, List<Object>> unknownFields = this.unknownFields;
    return unknownFields != null ? unknownFields.entrySet() : null;
  }
}
