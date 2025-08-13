package io.vertx.protobuf.json;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.protobuf.RecordVisitor;
import io.vertx.protobuf.schema.EnumType;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.well_known_types.MessageLiteral;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

public class JsonWriter implements RecordVisitor  {

  public static JsonObject encode(Consumer<RecordVisitor> consumer) {
    JsonWriter writer = new JsonWriter();
    consumer.accept(writer);
    return writer.stack.pop();
  }

  private final Deque<JsonObject> stack = new ArrayDeque<>();
  private ProtoReader structWriter;
  private int structDepth;

  @Override
  public void init(MessageType type) {
    stack.add(new JsonObject());
  }

  @Override
  public void destroy() {
  }

  @Override
  public void enter(Field field) {
    if (structWriter != null) {
      if (field.type() == MessageLiteral.Struct) {
        structDepth++;
      }
      structWriter.enter(field);
    } else if (field.type() == MessageLiteral.Struct) {
      structWriter = new ProtoReader();
      structWriter.init(MessageLiteral.Struct);
    } else {
      stack.add(new JsonObject());
    }
  }

  @Override
  public void leave(Field field) {
    if (field.type() == MessageLiteral.Struct) {
      if (structDepth-- == 0) {
        structWriter.destroy();
        JsonObject o = (JsonObject) structWriter.stack.pop();
        structWriter = null;
        put(field, o);
      } else {
        structWriter.leave(field);
      }
    } else if (structWriter != null) {
      structWriter.leave(field);
    } else {
      JsonObject obj = stack.pop();
      put(field, obj);
    }
  }

  private void put(Field field, Object value) {
    JsonObject object = stack.peek();
    if (field.isRepeated()) {
      JsonArray array = object.getJsonArray(field.jsonName());
      if (array == null) {
        array = new JsonArray();
        object.put(field.jsonName(), array);
      }
      array.add(value);
    } else {
      object.put(field.jsonName(), value);
    }
  }

  @Override
  public void visitInt32(Field field, int v) {
    assert structWriter == null;
    put(field, v);
  }

  @Override
  public void visitUInt32(Field field, int v) {
    assert structWriter == null;
    put(field, v);
  }

  @Override
  public void visitSInt32(Field field, int v) {
    assert structWriter == null;
    put(field, v);
  }

  @Override
  public void visitInt64(Field field, long v) {
    assert structWriter == null;
    put(field, v);
  }

  @Override
  public void visitUInt64(Field field, long v) {
    assert structWriter == null;
    put(field, v);
  }

  @Override
  public void visitSInt64(Field field, long v) {
    assert structWriter == null;
    put(field, v);
  }

  @Override
  public void visitEnum(Field field, int number) {
    if (structWriter != null) {
      structWriter.visitEnum(field, number);
    } else {
      put(field, ((EnumType)field.type()).nameOf(number));
    }
  }

  @Override
  public void visitBool(Field field, boolean v) {
    if (structWriter != null) {
      structWriter.visitBool(field, v);
    } else {
      put(field, v);
    }
  }

  @Override
  public void visitFixed32(Field field, int v) {
    assert structWriter == null;
    put(field, v);
  }

  @Override
  public void visitSFixed32(Field field, int v) {
    assert structWriter == null;
    put(field, v);
  }

  @Override
  public void visitFloat(Field field, float f) {
    assert structWriter == null;
    put(field, f);
  }

  @Override
  public void visitFixed64(Field field, long v) {
    assert structWriter == null;
    put(field, Long.toString(v));
  }

  @Override
  public void visitSFixed64(Field field, long v) {
    assert structWriter == null;
    put(field, v);
  }

  @Override
  public void visitDouble(Field field, double d) {
    if (structWriter != null) {
      structWriter.visitDouble(field, d);
    } else {
      put(field, d);
    }
  }

  @Override
  public void visitString(Field field, String s) {
    if (structWriter != null) {
      structWriter.visitString(field, s);
    } else {
      put(field, s);
    }
  }

  @Override
  public void visitBytes(Field field, byte[] bytes) {
    assert structWriter == null;
    put(field, bytes);
  }

  @Override
  public void enterPacked(Field field) {
  }

  @Override
  public void leavePacked(Field field) {
  }
}
