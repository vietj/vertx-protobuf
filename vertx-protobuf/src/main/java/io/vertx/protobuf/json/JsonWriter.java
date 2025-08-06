package io.vertx.protobuf.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import io.vertx.core.json.EncodeException;
import io.vertx.protobuf.RecordVisitor;
import io.vertx.protobuf.schema.EnumType;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;

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
    try {
      ensureStructure(field);
      generator.writeNumber(v);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitUInt32(Field field, int v) {
    try {
      ensureStructure(field);
      generator.writeNumber(v);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitSInt32(Field field, int v) {
    try {
      ensureStructure(field);
      generator.writeNumber(v);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitEnum(Field field, int number) {
    try {
      ensureStructure(field);
      EnumType type = (EnumType) field.type();
      generator.writeString(type.nameOf(number));
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitInt64(Field field, long v) {
    try {
      ensureStructure(field);
      generator.writeString("" + v);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitUInt64(Field field, long v) {
    try {
      ensureStructure(field);
      generator.writeString("" + v);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitSInt64(Field field, long v) {
    try {
      ensureStructure(field);
      generator.writeString("" + v);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitBool(Field field, boolean v) {
    try {
      ensureStructure(field);
      generator.writeBoolean(v);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitDouble(Field field, double d) {
    try {
      ensureStructure(field);
      generator.writeString("" + d);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitFixed64(Field field, long v) {
    try {
      ensureStructure(field);
      generator.writeString("" + v);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitSFixed64(Field field, long v) {
    try {
      ensureStructure(field);
      generator.writeString("" + v);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitFloat(Field field, float f) {
    try {
      ensureStructure(field);
      generator.writeNumber(f);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitFixed32(Field field, int v) {
    try {
      ensureStructure(field);
      generator.writeNumber(v);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitSFixed32(Field field, int v) {
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
    try {
      ensureStructure(field);
      stack.push(false);
      generator.writeStartArray();
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void enter(Field field) {
    if (field.type() instanceof MessageType) {
      try {
        ensureStructure(field);
        stack.add(true);
        generator.writeStartObject();
      } catch (IOException e) {
        throw new EncodeException(e.getMessage());
      }
    }
  }

  @Override
  public void leaveRepetition(Field field) {
    try {
      stack.pop();
      generator.writeEndArray();
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void leave(Field field) {
    if (field.type() instanceof MessageType) {
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
    try {
      ensureStructure(field);
      generator.writeString(s);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }

  @Override
  public void visitBytes(Field field, byte[] bytes) {
    try {
      ensureStructure(field);
      generator.writeBinary(bytes);
    } catch (IOException e) {
      throw new EncodeException(e.getMessage());
    }
  }
}
