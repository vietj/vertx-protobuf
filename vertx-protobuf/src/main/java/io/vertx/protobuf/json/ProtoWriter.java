package io.vertx.protobuf.json;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.protobuf.RecordVisitor;

import java.util.Map;

public class ProtoWriter {

  public static void emit(JsonObject json, RecordVisitor visitor) {
    visitor.init(SchemaLiterals.Struct.TYPE);
    visit(json, visitor);
    visitor.destroy();
  }

  public static void visit(JsonObject json, RecordVisitor visitor) {
    Map<String, Object> map = json.getMap();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      visitor.enter(SchemaLiterals.Struct.fields); // fields
//      visitor.enter(SchemaLiterals.FieldsEntry.key);
      visitor.visitString(SchemaLiterals.FieldsEntry.key, entry.getKey());
//      visitor.leave(SchemaLiterals.FieldsEntry.key);
      visitor.enter(SchemaLiterals.FieldsEntry.value);
      visitValueInternal(entry.getValue(), visitor);
      visitor.leave(SchemaLiterals.FieldsEntry.value);
      visitor.leave(SchemaLiterals.Struct.fields);
    }
  }

  public static void emit(JsonArray json, RecordVisitor visitor) {
    visitor.init(SchemaLiterals.ListValue.TYPE);
    visit(json, visitor);
    visitor.destroy();
  }

  public static void visit(JsonArray json, RecordVisitor visitor) {
    for (Object value : json.getList()) {
      visitor.enter(SchemaLiterals.ListValue.values); // values
      visitValueInternal(value, visitor);
      visitor.leave(SchemaLiterals.ListValue.values);
    }
  }

  private static void visitValueInternal(Object value, RecordVisitor visitor) {
    if (value == null) {
      visitor.visitVarInt32(SchemaLiterals.Value.null_value, 0);
    } else if (value instanceof String) {
//      visitor.enter(SchemaLiterals.Value.string_value);
      visitor.visitString(SchemaLiterals.Value.string_value, (String) value);
//      visitor.leave(SchemaLiterals.Value.string_value);
    } else if (value instanceof Boolean) {
      visitor.visitVarInt64(SchemaLiterals.Value.bool_value, ((Boolean) value) ? 1 : 0);
    } else if (value instanceof Number) {
      visitor.visitDouble(SchemaLiterals.Value.number_value, ((Number) value).doubleValue());
    } else if (value instanceof JsonObject) {
      visitor.enter(SchemaLiterals.Value.struct_value);
      visit((JsonObject) value, visitor);
      visitor.leave(SchemaLiterals.Value.struct_value);
    } else if (value instanceof JsonArray) {
      visitor.enter(SchemaLiterals.Value.list_value);
      visit((JsonArray) value, visitor);
      visitor.leave(SchemaLiterals.Value.list_value);
    } else {
      throw new UnsupportedOperationException("" + value);
    }
  }
}
