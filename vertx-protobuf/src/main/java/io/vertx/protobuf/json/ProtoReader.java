package io.vertx.protobuf.json;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.protobuf.RecordVisitor;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.well_known_types.FieldLiteral;
import io.vertx.protobuf.well_known_types.MessageLiteral;

import java.util.ArrayDeque;
import java.util.Deque;

public class ProtoReader implements RecordVisitor {

  final Deque<Object> stack ;

  public ProtoReader() {
    this(new ArrayDeque<>());
  }

  public ProtoReader(Deque<Object> stack) {
    this.stack = stack;
  }

  @Override
  public void init(MessageType type) {
    if (type == MessageLiteral.Struct) {
      stack.push(new JsonObject());
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public void visitBool(Field field, boolean v) {
    append(v);
  }

  @Override
  public void visitEnum(Field field, int number) {
    append(null);
  }

  private void append(Object value) {
    Object container = stack.peek();
    if (container instanceof Entry) {
      ((Entry)container).value = value;
    } else {
      ((JsonArray)container).add(value);
    }
  }

  @Override
  public void visitString(Field field, String s) {
    switch (field.number()) {
      case 1:
        // FieldsEntry
        ((Entry)stack.peek()).key = s;
        break;
      case 3:
        append(s);
      break;
    }
  }

  @Override
  public void visitDouble(Field field, double d) {
    append(d);
  }

  @Override
  public void enterRepetition(Field field) {
  }

  @Override
  public void enter(Field field) {
    FieldLiteral fl = (FieldLiteral) field;
    switch (fl) {
      case Value_struct_value:
        stack.push(new JsonObject());
        break;
      case Value_list_value:
        stack.push(new JsonArray());
        break;
      case Struct_fields:
        stack.push(new Entry());
        break;
      case FieldsEntry_value:
      case ListValue_values:
        break;
      default:
        throw new UnsupportedOperationException(fl.name());
    }
  }

  private static class Entry {
    String key;
    Object value;
  }

  @Override
  public void leaveRepetition(Field field) {

  }

  @Override
  public void leave(Field field) {
    Entry entry;
    FieldLiteral fl = (FieldLiteral) field;
    switch (fl) {
      case Value_struct_value:
        append(stack.pop());
        break;
      case Value_list_value:
        append(stack.pop());
        break;
      case Struct_fields:
        entry = (Entry) stack.pop();
        ((JsonObject)stack.peek()).put(entry.key, entry.value);
        break;
      case FieldsEntry_value:
      case ListValue_values:
        break;
      default:
        throw new UnsupportedOperationException(fl.name());
    }
  }

  @Override
  public void destroy() {
  }

  @Override
  public void visitBytes(Field field, byte[] bytes) {
    throw new UnsupportedOperationException();
  }
}
