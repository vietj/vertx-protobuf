package io.vertx.protobuf.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import io.vertx.core.json.EncodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.JacksonCodec;
import io.vertx.protobuf.RecordVisitor;
import io.vertx.protobuf.schema.EnumType;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.well_known_types.MessageLiteral;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

public class JsonWriter implements RecordVisitor {

  public static String encode(Consumer<RecordVisitor> consumer) {
    StringWriter out = new StringWriter();
    JsonGenerator generator;
    try {
      generator = JsonFactory.builder().build().createGenerator(out);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
    try {
      JsonWriter writer = new JsonWriter(generator);
      consumer.accept(writer);
    } finally {
      try {
        generator.close();
      } catch (IOException ignore) {
      }
      try {
        out.close();
      } catch (IOException ignore) {
      }
    }
    return out.toString();
  }

  // true : json object / false : json array
  private Deque<Boolean> stack = new ArrayDeque<>();
  private final JsonGenerator generator;
  private ProtoReader structWriter;
  private int structDepth;

  public JsonWriter(JsonGenerator generator) {
    this.generator = generator;
  }

  @Override
  public void init(MessageType type) {
    try {
      stack.push(true);
      generator.writeStartObject();
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitInt32(Field field, int v) {
    assert structWriter == null;
    try {
      ensureStructure(field);
      generator.writeNumber(v);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitUInt32(Field field, int v) {
    assert structWriter == null;
    try {
      ensureStructure(field);
      generator.writeNumber(v);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitSInt32(Field field, int v) {
    assert structWriter == null;
    try {
      ensureStructure(field);
      generator.writeNumber(v);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitEnum(Field field, int number) {
    if (structWriter != null) {
      structWriter.visitEnum(field, number);
    } else {
      try {
        ensureStructure(field);
        EnumType type = (EnumType) field.type();
        generator.writeString(type.nameOf(number));
      } catch (IOException e) {
        throw new EncodeException(e.getMessage());
      }
    }
  }

  @Override
  public void visitInt64(Field field, long v) {
    assert structWriter == null;
    try {
      ensureStructure(field);
      generator.writeString("" + v);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitUInt64(Field field, long v) {
    assert structWriter == null;
    try {
      ensureStructure(field);
      generator.writeString("" + v);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitSInt64(Field field, long v) {
    assert structWriter == null;
    try {
      ensureStructure(field);
      generator.writeString("" + v);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitBool(Field field, boolean v) {
    if (structWriter != null) {
      structWriter.visitBool(field, v);
    } else {
      try {
        ensureStructure(field);
        generator.writeBoolean(v);
      } catch (IOException e) {
        throw new EncodeException(e.getMessage());
      }
    }
  }

  @Override
  public void visitDouble(Field field, double d) {
    if (structWriter != null) {
      structWriter.visitDouble(field, d);
    } else {
      try {
        ensureStructure(field);
        generator.writeString("" + d);
      } catch (IOException e) {
        throw new EncodeException(e.getMessage());
      }
    }
  }

  @Override
  public void visitFixed64(Field field, long v) {
    assert structWriter == null;
    try {
      ensureStructure(field);
      generator.writeString("" + v);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitSFixed64(Field field, long v) {
    assert structWriter == null;
    try {
      ensureStructure(field);
      generator.writeString("" + v);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitFloat(Field field, float f) {
    assert structWriter == null;
    try {
      ensureStructure(field);
      generator.writeNumber(f);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitFixed32(Field field, int v) {
    assert structWriter == null;
    try {
      ensureStructure(field);
      generator.writeNumber(v);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitSFixed32(Field field, int v) {
    assert structWriter == null;
    try {
      ensureStructure(field);
      generator.writeNumber(v);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void destroy() {
    try {
      generator.writeEndObject();
      stack.pop();
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  private void ensureStructure(Field field) throws IOException {
    if (stack.peek()) {
      generator.writeFieldName(field.jsonName());
    }
  }

  @Override
  public void enterRepetition(Field field) {
    if (structWriter != null) {
      structWriter.enterRepetition(field);
    } else {
      try {
        ensureStructure(field);
        stack.push(false);
        generator.writeStartArray();
      } catch (IOException e) {
        throw new EncodeException(e.getMessage());
      }
    }
  }

  @Override
  public void leaveRepetition(Field field) {
    if (structWriter != null) {
      structWriter.leaveRepetition(field);
    } else {
      try {
        stack.pop();
        generator.writeEndArray();
      } catch (IOException e) {
        throw new EncodeException(e.getMessage());
      }
    }
  }

  @Override
  public void enter(Field field) {
    try {
      MessageType type = (MessageType) field.type();
      if (structWriter != null) {
        if (type == MessageLiteral.Struct) {
          structDepth++;
        }
        structWriter.enter(field);
      } else {
        if (type == MessageLiteral.Struct) {
          ensureStructure(field);
          structWriter = new ProtoReader();
          structWriter.init(type);
        } else {
          ensureStructure(field);
          stack.add(true);
          generator.writeStartObject();
        }
      }
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void leave(Field field) {
    MessageType type = (MessageType) field.type();
    if (type == MessageLiteral.Struct) {
      if (structDepth-- == 0) {
        structWriter.destroy();
        JsonObject o = (JsonObject) structWriter.stack.pop();
        structWriter = null;
        JacksonCodec.encodeJson(o, generator);
      } else {
        structWriter.leave(field);
      }
    } else if (structWriter != null) {
      structWriter.leave(field);
    } else {
      try {
        stack.pop();
        generator.writeEndObject();
      } catch (IOException e) {
        throw new EncodeException(e.getMessage());
      }
    }
  }

  @Override
  public void visitString(Field field, String s) {
    if (structWriter != null) {
      structWriter.visitString(field, s);
    } else {
      try {
        ensureStructure(field);
        generator.writeString(s);
      } catch (IOException e) {
        throw new EncodeException(e.getMessage());
      }
    }
  }

  @Override
  public void visitBytes(Field field, byte[] bytes) {
    assert structWriter == null;
    try {
      ensureStructure(field);
      generator.writeBinary(bytes);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }
}
