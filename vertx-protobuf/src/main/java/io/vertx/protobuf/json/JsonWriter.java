package io.vertx.protobuf.json;

import com.fasterxml.jackson.core.JsonGenerator;
import io.vertx.core.json.EncodeException;
import io.vertx.protobuf.RecordVisitor;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;

import java.io.IOException;

public class JsonWriter implements RecordVisitor {

  private final JsonGenerator generator;

  public JsonWriter(JsonGenerator generator) {
    this.generator = generator;
  }

  @Override
  public void init(MessageType type) {
    try {
      generator.writeStartObject();
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitInt32(Field field, int v) {
    try {
      generator.writeFieldName(field.jsonName());
      generator.writeNumber(v);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitUInt32(Field field, int v) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visitSInt32(Field field, int v) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visitEnum(Field field, int number) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visitInt64(Field field, long v) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visitUInt64(Field field, long v) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visitSInt64(Field field, long v) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visitBool(Field field, boolean v) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visitDouble(Field field, double d) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visitFixed64(Field field, long v) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visitSFixed64(Field field, long v) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visitFloat(Field field, float f) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visitFixed32(Field field, int v) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visitSFixed32(Field field, int v) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void destroy() {
    try {
      generator.writeEndObject();
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void enterRepetition(Field field) {
    try {
      generator.writeFieldName(field.jsonName());
      generator.writeStartArray();
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void enter(Field field) {
    if (field.type() instanceof MessageType) {
      try {
        generator.writeStartObject();
      } catch (IOException e) {
        throw new EncodeException(e.getMessage());
      }
    }
  }

  @Override
  public void leaveRepetition(Field field) {
    try {
      generator.writeEndArray();
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void leave(Field field) {
    if (field.type() instanceof MessageType) {
      try {
        generator.writeEndObject();
      } catch (IOException e) {
        throw new EncodeException(e.getMessage());
      }
    }
  }

  @Override
  public void visitString(Field field, String s) {
    try {
      // Need to check whether in
      generator.writeFieldName(field.jsonName());
      generator.writeString(s);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitBytes(Field field, byte[] bytes) {
    throw new UnsupportedOperationException();
  }
}
