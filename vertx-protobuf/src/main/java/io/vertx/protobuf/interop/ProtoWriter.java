package io.vertx.protobuf.interop;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.protobuf.RecordVisitor;
import io.vertx.protobuf.well_known_types.FieldLiteral;
import io.vertx.protobuf.well_known_types.MessageLiteral;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;

public class ProtoWriter {

  public static void emit(Duration duration, RecordVisitor visitor) {
    visitor.init(MessageLiteral.Duration);
    visit(duration, visitor);
    visitor.destroy();
  }

  public static void visit(Duration duration, RecordVisitor visitor) {
    long seconds = duration.getSeconds();
    if (seconds != 0L) {
      visitor.visitInt64(FieldLiteral.Duration_seconds, seconds);
    }
    int nano = duration.getNano();
    if (nano != 0) {
      visitor.visitInt32(FieldLiteral.Duration_nanos, nano);
    }
  }

  public static void emit(OffsetDateTime timestamp, RecordVisitor visitor) {
    visitor.init(MessageLiteral.Timestamp);
    visit(timestamp, visitor);
    visitor.destroy();
  }

  public static void visit(OffsetDateTime timestamp, RecordVisitor visitor) {
    Instant instant = timestamp.toInstant();
    long seconds = instant.getEpochSecond();
    if (seconds != 0L) {
      visitor.visitInt64(FieldLiteral.Timestamp_seconds, seconds);
    }
    int nano = timestamp.getNano();
    if (nano != 0) {
      visitor.visitInt32(FieldLiteral.Timestamp_nanos, nano);
    }
  }

  public static void emit(JsonObject json, RecordVisitor visitor) {
    visitor.init(MessageLiteral.Struct);
    visit(json, visitor);
    visitor.destroy();
  }

  public static void visit(JsonObject json, RecordVisitor visitor) {
    Map<String, Object> map = json.getMap();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      visitor.enter(FieldLiteral.Struct_fields); // fields
      visitor.visitString(FieldLiteral.FieldsEntry_key, entry.getKey());
      visitor.enter(FieldLiteral.FieldsEntry_value);
      visitValueInternal(entry.getValue(), visitor);
      visitor.leave(FieldLiteral.FieldsEntry_value);
      visitor.leave(FieldLiteral.Struct_fields);
    }
  }

  public static void emit(JsonArray json, RecordVisitor visitor) {
    visitor.init(MessageLiteral.ListValue);
    visit(json, visitor);
    visitor.destroy();
  }

  public static void visit(JsonArray json, RecordVisitor visitor) {
    for (Object value : json.getList()) {
      visitor.enter(FieldLiteral.ListValue_values); // values
      visitValueInternal(value, visitor);
      visitor.leave(FieldLiteral.ListValue_values);
    }
  }

  private static void visitValueInternal(Object value, RecordVisitor visitor) {
    if (value == null) {
      visitor.visitVarInt32(FieldLiteral.Value_null_value, 0);
    } else if (value instanceof String) {
      visitor.visitString(FieldLiteral.Value_string_value, (String) value);
    } else if (value instanceof Boolean) {
      visitor.visitVarInt64(FieldLiteral.Value_bool_value, ((Boolean) value) ? 1 : 0);
    } else if (value instanceof Number) {
      visitor.visitDouble(FieldLiteral.Value_number_value, ((Number) value).doubleValue());
    } else if (value instanceof JsonObject) {
      visitor.enter(FieldLiteral.Value_struct_value);
      visit((JsonObject) value, visitor);
      visitor.leave(FieldLiteral.Value_struct_value);
    } else if (value instanceof JsonArray) {
      visitor.enter(FieldLiteral.Value_list_value);
      visit((JsonArray) value, visitor);
      visitor.leave(FieldLiteral.Value_list_value);
    } else {
      throw new UnsupportedOperationException("" + value);
    }
  }
}
