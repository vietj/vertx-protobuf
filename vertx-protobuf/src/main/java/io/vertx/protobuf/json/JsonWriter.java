package io.vertx.protobuf.json;

import io.vertx.core.json.EncodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.protobuf.RecordVisitor;
import io.vertx.protobuf.interop.ProtoReader;
import io.vertx.protobuf.schema.EnumType;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.well_known_types.FieldLiteral;
import io.vertx.protobuf.well_known_types.MessageLiteral;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

public class JsonWriter implements RecordVisitor  {

  public static JsonObject encode(Consumer<RecordVisitor> consumer) {
    JsonWriter writer = new JsonWriter();
    consumer.accept(writer);
    return (JsonObject) writer.stack.pop();
  }

  private final Deque<Object> stack = new ArrayDeque<>();
  private ProtoReader structWriter;
  private long durationSeconds;
  private int durationNanos;
  private long timestampSeconds;
  private int timestampNanos;
  private double doubleValue;
  private float floatValue;
  private boolean booleanValue;
  private long intValue;
  private long longValue;
  private String stringValue;
  private byte[] bytesValue;

  @Override
  public void init(MessageType type) {
    stack.push(new JsonObject());
  }

  @Override
  public void destroy() {
  }

  @Override
  public void enter(Field field) {
    if (structWriter != null) {
      structWriter.enter(field);
    } else if (field.type() instanceof MessageLiteral) {
      switch ((MessageLiteral)field.type()) {
        case Duration:
          durationSeconds = 0L;
          durationNanos = 0;
          break;
        case Struct:
        case Value:
        case ListValue:
        case Timestamp:
        case DoubleValue:
        case FloatValue:
        case Int64Value:
        case UInt64Value:
        case Int32Value:
        case UInt32Value:
        case BoolValue:
        case StringValue:
        case BytesValue:
          structWriter = new ProtoReader();
          structWriter.init((MessageType) field.type());
          break;
        default:
          throw new UnsupportedOperationException();
      }
    } else if (field.isMap()) {
      stack.push(new Entry());
    } else {
      stack.push(new JsonObject());
    }
  }

  private static class Entry {
    Object key;
    Object value;
  }

  @Override
  public void leave(Field field) {
    if (field.type() == MessageLiteral.Duration) {
      if (!JsonReader.isValidDuration(durationSeconds, durationNanos)) {
        throw new EncodeException();
      }
      BigDecimal bd = new BigDecimal(durationSeconds).add(BigDecimal.valueOf(durationNanos, 9));
      String t = bd.toPlainString() + "s";
      put(field, t);
    } else if (field.type() == MessageLiteral.Timestamp) {
      structWriter.destroy();
      OffsetDateTime o = (OffsetDateTime) structWriter.pop();
      structWriter = null;
      String t = o.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
      put(field, t);
    } else if (
      (field.type() == MessageLiteral.Value && structWriter.rootType == MessageLiteral.Value && structWriter.depth() == 1) || // HACK !!! FIXME!!!!!!!
      field.type() == MessageLiteral.DoubleValue ||
      field.type() == MessageLiteral.FloatValue ||
      field.type() == MessageLiteral.Int64Value ||
      field.type() == MessageLiteral.UInt64Value ||
      field.type() == MessageLiteral.Int32Value ||
      field.type() == MessageLiteral.UInt32Value ||
      field.type() == MessageLiteral.BoolValue ||
      field.type() == MessageLiteral.StringValue ||
      field.type() == MessageLiteral.BytesValue
    ) {
      structWriter.destroy();
      Object value = structWriter.pop();
      structWriter = null;
      put(field, value);
    } else if (field.type() == MessageLiteral.Struct && structWriter.rootType == MessageLiteral.Struct && structWriter.depth() == 1) {
      // CONVOLUTED
      structWriter.destroy();
      JsonObject o = (JsonObject) structWriter.pop();
      structWriter = null;
      put(field, o);
    } else if (field.type() == MessageLiteral.ListValue && structWriter.rootType == MessageLiteral.ListValue && structWriter.depth() == 1) {
      // CONVOLUTED
      structWriter.destroy();
      JsonArray o = (JsonArray) structWriter.pop();
      structWriter = null;
      put(field, o);
    } else if (structWriter != null) {
      structWriter.leave(field);
    } else if (field.isMap()) {
      Entry entry = (Entry) stack.pop();
      JsonObject obj = (JsonObject) stack.peek();
      JsonObject blah = obj.getJsonObject(field.jsonName());
      if (blah == null) {
        blah = new JsonObject();
        obj.put(field.jsonName(), blah);
      }
      blah.put("" + entry.key, entry.value);
    } else {
      JsonObject obj = (JsonObject) stack.pop();
      put(field, obj);
    }
  }

  private void put(Field field, Object value) {
    if (field.isMapKey()) {
      Entry entry = (Entry) stack.peek();
      entry.key = value;
    } else if (field.isMapValue()) {
      Entry entry = (Entry) stack.peek();
      entry.value = value;
    } else {
      JsonObject object = (JsonObject) stack.peek();
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
  }

  @Override
  public void visitInt32(Field field, int v) {
    if (structWriter != null) {
      structWriter.visitInt32(field, v);
    } else if (field instanceof FieldLiteral) {
      switch ((FieldLiteral)field) {
        case Duration_nanos:
          durationNanos = v;
          break;
        default:
          throw new UnsupportedOperationException();
      }
    } else {
      put(field, v);
    }
  }

  @Override
  public void visitUInt32(Field field, int v) {
    if (structWriter != null) {
      structWriter.visitUInt32(field, v);
    } else {
      put(field, writeUInt32(v));
    }
  }

  @Override
  public void visitSInt32(Field field, int v) {
    assert structWriter == null;
    put(field, v);
  }

  @Override
  public void visitInt64(Field field, long v) {
    if (structWriter != null) {
      structWriter.visitInt64(field, v);
    } else if (field instanceof FieldLiteral) {
      switch ((FieldLiteral)field) {
        case Duration_seconds:
          durationSeconds = v;
          break;
        default:
          throw new UnsupportedOperationException();
      }
    } else {
      put(field, v);
    }
  }

  @Override
  public void visitUInt64(Field field, long v) {
    if (structWriter != null) {
      structWriter.visitUInt64(field, v);
    } else {
      put(field, writeUInt64(v));
    }
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
    put(field, (long)v & 0xFFFFFFFFL);
  }

  @Override
  public void visitSFixed32(Field field, int v) {
    assert structWriter == null;
    put(field, v);
  }

  @Override
  public void visitFloat(Field field, float f) {
    if (structWriter != null) {
      structWriter.visitFloat(field, f);
    } else {
      put(field, f);
    }
  }

  @Override
  public void visitFixed64(Field field, long v) {
    assert structWriter == null;
    put(field, writeUInt64(v));
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
    if (structWriter != null) {
      structWriter.visitBytes(field, bytes);
    } else {
      put(field, bytes);
    }
  }

  @Override
  public void enterPacked(Field field) {
  }

  @Override
  public void leavePacked(Field field) {
  }

  private static String writeUInt32(final int value) {
    if (value >= 0) {
      return Integer.toString(value);
    } else {
      return Long.toString(value & 0xFFFFFFFFL);
    }
  }
  private static String writeUInt64(long value) {
    if (value >= 0) {
      return Long.toString(value);
    } else {
      return BigInteger.valueOf(value & Long.MAX_VALUE).setBit(Long.SIZE - 1).toString();
    }
  }
}
