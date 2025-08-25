package io.vertx.protobuf.it;

import io.vertx.protobuf.ProtoVisitor;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.Field;
import java.util.Deque;
import java.util.ArrayDeque;

public class ProtoReader implements ProtoVisitor {

  public final Deque<Object> stack;
  private ProtoVisitor next;

  public ProtoReader(Deque<Object> stack) {
    this.stack = stack;
  }

  public ProtoReader() {
    this(new ArrayDeque<>());
  }

  public void init(MessageType type) {
    if (type instanceof MessageLiteral) {
      MessageLiteral literal = (MessageLiteral)type;
      switch (literal) {
        case SimpleMessage: {
          stack.push(new io.vertx.protobuf.it.SimpleMessage());
          break;
        }
        default:
          throw new UnsupportedOperationException();
        }
    } else if (next != null) {
      next.init(type);
    } else {
      throw new IllegalArgumentException("");
    }
  }

  public void visitString(Field field, String value) {
    if (field instanceof FieldLiteral) {
      FieldLiteral fieldLiteral = (FieldLiteral)field;
      switch (fieldLiteral) {
        case SimpleMessage_string_field: {
          ((io.vertx.protobuf.it.SimpleMessage)stack.peek()).setStringField(value);
          break;
        }
        default:
          throw new IllegalArgumentException("Invalid field " + field);
      }
    } else if (next != null) {
      next.visitString(field, value);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void visitBytes(Field field, byte[] value) {
    if (field instanceof FieldLiteral) {
      FieldLiteral fieldLiteral = (FieldLiteral)field;
      switch (fieldLiteral) {
        default:
          throw new IllegalArgumentException("Invalid field " + field);
      }
    } else if (field.isUnknown()) {
      io.vertx.protobuf.lang.MessageBase base = (io.vertx.protobuf.lang.MessageBase)stack.peek();
      base.unknownField(field).add(io.vertx.core.buffer.Buffer.buffer(value));
    } else if (next != null) {
      next.visitBytes(field, value);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void visitFixed32(Field field, int value) {
    if (field instanceof FieldLiteral) {
      FieldLiteral fieldLiteral = (FieldLiteral)field;
      switch (fieldLiteral) {
        default:
          throw new IllegalArgumentException("Invalid field " + field);
      }
    } else if (field.isUnknown()) {
      io.vertx.protobuf.lang.MessageBase base = (io.vertx.protobuf.lang.MessageBase)stack.peek();
      base.unknownField(field).add(value);
    } else if (next != null) {
      next.visitFixed32(field, value);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void visitFixed64(Field field, long value) {
    if (field instanceof FieldLiteral) {
      FieldLiteral fieldLiteral = (FieldLiteral)field;
      switch (fieldLiteral) {
        default:
          throw new IllegalArgumentException("Invalid field " + field);
      }
    } else if (field.isUnknown()) {
      io.vertx.protobuf.lang.MessageBase base = (io.vertx.protobuf.lang.MessageBase)stack.peek();
      base.unknownField(field).add(value);
    } else if (next != null) {
      next.visitFixed64(field, value);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void visitSFixed32(Field field, int value) {
    if (field instanceof FieldLiteral) {
      FieldLiteral fieldLiteral = (FieldLiteral)field;
      switch (fieldLiteral) {
        default:
          throw new IllegalArgumentException("Invalid field " + field);
      }
    } else if (next != null) {
      next.visitSFixed32(field, value);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void visitSFixed64(Field field, long value) {
    if (field instanceof FieldLiteral) {
      FieldLiteral fieldLiteral = (FieldLiteral)field;
      switch (fieldLiteral) {
        default:
          throw new IllegalArgumentException("Invalid field " + field);
      }
    } else if (next != null) {
      next.visitSFixed64(field, value);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void visitFloat(Field field, float value) {
    if (field instanceof FieldLiteral) {
      FieldLiteral fieldLiteral = (FieldLiteral)field;
      switch (fieldLiteral) {
        default:
          throw new IllegalArgumentException("Invalid field " + field);
      }
    } else if (next != null) {
      next.visitFloat(field, value);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void visitDouble(Field field, double value) {
    if (field instanceof FieldLiteral) {
      FieldLiteral fieldLiteral = (FieldLiteral)field;
      switch (fieldLiteral) {
        default:
          throw new IllegalArgumentException("Invalid field " + field);
      }
    } else if (next != null) {
      next.visitDouble(field, value);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void visitInt32(Field field, int value) {
    if (field instanceof FieldLiteral) {
      FieldLiteral fieldLiteral = (FieldLiteral)field;
      switch (fieldLiteral) {
        default:
          throw new IllegalArgumentException("Invalid field " + field);
      }
    } else if (next != null) {
      next.visitInt32(field, value);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void visitUInt32(Field field, int value) {
    if (field instanceof FieldLiteral) {
      FieldLiteral fieldLiteral = (FieldLiteral)field;
      switch (fieldLiteral) {
        default:
          throw new IllegalArgumentException("Invalid field " + field);
      }
    } else if (next != null) {
      next.visitUInt32(field, value);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void visitSInt32(Field field, int value) {
    if (field instanceof FieldLiteral) {
      FieldLiteral fieldLiteral = (FieldLiteral)field;
      switch (fieldLiteral) {
        default:
          throw new IllegalArgumentException("Invalid field " + field);
      }
    } else if (next != null) {
      next.visitSInt32(field, value);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void visitBool(Field field, boolean value) {
    if (field instanceof FieldLiteral) {
      FieldLiteral fieldLiteral = (FieldLiteral)field;
      switch (fieldLiteral) {
        default:
          throw new IllegalArgumentException("Invalid field " + field);
      }
    } else if (next != null) {
      next.visitBool(field, value);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void visitEnum(Field field, int value) {
    if (field instanceof FieldLiteral) {
      FieldLiteral fieldLiteral = (FieldLiteral)field;
      switch (fieldLiteral) {
        default:
          throw new IllegalArgumentException("Invalid field " + field);
      }
    } else if (next != null) {
      next.visitEnum(field, value);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void visitInt64(Field field, long value) {
    if (field instanceof FieldLiteral) {
      FieldLiteral fieldLiteral = (FieldLiteral)field;
      switch (fieldLiteral) {
        case SimpleMessage_long_field: {
          ((io.vertx.protobuf.it.SimpleMessage)stack.peek()).setLongField(value);
          break;
        }
        default:
          throw new IllegalArgumentException("Invalid field " + field);
      }
    } else if (field.isUnknown()) {
      io.vertx.protobuf.lang.MessageBase base = (io.vertx.protobuf.lang.MessageBase)stack.peek();
      base.unknownField(field).add(value);
    } else if (next != null) {
      next.visitInt64(field, value);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void visitSInt64(Field field, long value) {
    if (field instanceof FieldLiteral) {
      FieldLiteral fieldLiteral = (FieldLiteral)field;
      switch (fieldLiteral) {
        default:
          throw new IllegalArgumentException("Invalid field " + field);
      }
    } else if (next != null) {
      next.visitSInt64(field, value);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void visitUInt64(Field field, long value) {
    if (field instanceof FieldLiteral) {
      FieldLiteral fieldLiteral = (FieldLiteral)field;
      switch (fieldLiteral) {
        default:
          throw new IllegalArgumentException("Invalid field " + field);
      }
    } else if (next != null) {
      next.visitUInt64(field, value);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void enter(Field field) {
    if (field instanceof FieldLiteral) {
      FieldLiteral literal = (FieldLiteral)field;
      switch (literal) {
        default:
          throw new UnsupportedOperationException();
      }
    } else if (field.isUnknown()) {
    } else if (next != null) {
      next.enter(field);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void leave(Field field) {
    if (field instanceof FieldLiteral) {
      FieldLiteral literal = (FieldLiteral)field;
      switch (literal) {
        default:
          throw new UnsupportedOperationException();
      }
    } else if (field.isUnknown()) {
    } else if (next != null) {
      next.leave(field);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void enterPacked(Field field) {
    if (field instanceof FieldLiteral) {
      FieldLiteral literal = (FieldLiteral)field;
      switch (literal) {
        default:
          throw new UnsupportedOperationException();
      }
    } else if (next != null) {
      next.enterPacked(field);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void leavePacked(Field field) {
    if (field instanceof FieldLiteral) {
      FieldLiteral literal = (FieldLiteral)field;
      switch (literal) {
        default:
          throw new UnsupportedOperationException();
      }
    } else if (next != null) {
      next.leavePacked(field);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void destroy() {
    if (next != null) {
      next.destroy();
    }
  }
}
