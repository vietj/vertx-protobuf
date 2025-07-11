package io.vertx.protobuf.json;

import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.protobuf.Visitor;

import java.util.Map;

public class StructWriter {

  public static Buffer encode(Struct struct) {
    return ProtobufWriter.encode(visitor -> {
      visitor.init(SchemaLiterals.Struct.TYPE);
      emit(struct, visitor);
      visitor.destroy();
    });
  }

  public static byte[] encodeToByteArray(Struct struct) {
    return ProtobufWriter.encodeToByteArray(visitor -> {
      visitor.init(SchemaLiterals.Struct.TYPE);
      emit(struct, visitor);
      visitor.destroy();
    });
  }

  private static void emit(Struct struct, Visitor visitor) {
    Map<String, Value> map = struct.getFieldsMap();
    for (String key : map.keySet()) {
      visitor.enter(SchemaLiterals.Struct.fields);
      visitor.visitString(SchemaLiterals.FieldsEntry.key, key);
      visitor.enter(SchemaLiterals.FieldsEntry.value);
      emit(map.get(key), visitor);
      visitor.leave(SchemaLiterals.FieldsEntry.value);
      visitor.leave(SchemaLiterals.Struct.fields);
    }
  }

  private static void emit(ListValue list, Visitor visitor) {
    list.getValuesList().forEach(value -> {
      visitor.enter(SchemaLiterals.ListValue.values);
      emit(value, visitor);
      visitor.leave(SchemaLiterals.ListValue.values);
    });
  }

  private static void emit(Value value, Visitor visitor) {
    if (value.hasNullValue()) {
      visitor.visitVarInt32(SchemaLiterals.Value.null_value, 0);
    } else if (value.hasNumberValue()) {
      visitor.visitDouble(SchemaLiterals.Value.number_value, value.getNumberValue());
    } else if (value.hasStringValue()) {
      visitor.visitString(SchemaLiterals.Value.string_value, value.getStringValue());
    } else if (value.hasBoolValue()) {
      visitor.visitVarInt32(SchemaLiterals.Value.bool_value, value.getBoolValue() ? 1 : 0);
    } else if (value.hasStructValue()) {
      visitor.enter(SchemaLiterals.Value.struct_value);
      emit(value.getStructValue(), visitor);
      visitor.leave(SchemaLiterals.Value.struct_value);
    } else if (value.hasListValue()) {
      visitor.enter(SchemaLiterals.Value.list_value);
      emit(value.getListValue(), visitor);
      visitor.leave(SchemaLiterals.Value.list_value);
    }
  }
}
