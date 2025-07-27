package io.vertx.tests.protobuf;

import io.vertx.protobuf.Visitor;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.Assert.*;

public class RecordingVisitor implements Visitor {

  private static abstract class Record {
    protected abstract void apply(Visitor visitor);
  }

  private static class Init extends Record {
    private final MessageType messageType;
    Init(MessageType messageType) {
      this.messageType = messageType;
    }
    @Override
    protected void apply(Visitor visitor) {
      visitor.init(messageType);
    }
  }

  private static class Destroy extends Record {
    @Override
    protected void apply(Visitor visitor) {
      visitor.destroy();
    }
  }

  private static class VarInt64 extends Record {
    private final Field field;
    private final long value;
    VarInt64(Field field, long value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(Visitor visitor) {
      visitor.visitVarInt64(field, value);
    }
  }

  private static class VarInt32 extends Record {
    private final Field field;
    private final int value;
    VarInt32(Field field, int value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(Visitor visitor) {
      visitor.visitVarInt32(field, value);
    }
  }

  private static class Fixed32 extends Record {
    private final Field field;
    private final int value;
    Fixed32(Field field, int value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(Visitor visitor) {
      visitor.visitFixed32(field, value);
    }
  }

  private static class Fixed64 extends Record {
    private final Field field;
    private final long value;
    Fixed64(Field field, long value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(Visitor visitor) {
      visitor.visitFixed64(field, value);
    }
  }

  private static class SFixed32 extends Record {
    private final Field field;
    private final int value;
    SFixed32(Field field, int value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(Visitor visitor) {
      visitor.visitSFixed32(field, value);
    }
  }

  private static class SFixed64 extends Record {
    private final Field field;
    private final long value;
    SFixed64(Field field, long value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(Visitor visitor) {
      visitor.visitSFixed64(field, value);
    }
  }

  private List<Record> records = new ArrayList<>();

  @Override
  public void init(MessageType type) {
    records.add(new Init(type));
  }

  @Override
  public void visitVarInt32(Field field, int v) {
    records.add(new VarInt32(field, v));
  }

  @Override
  public void visitVarInt64(Field field, long v) {
    records.add(new VarInt64(field, v));
  }

  @Override
  public void visitString(Field field, String s) {

  }

  @Override
  public void visitDouble(Field field, double d) {

  }

  @Override
  public void visitFixed32(Field field, int v) {
    records.add(new Fixed32(field, v));
  }

  @Override
  public void visitFixed64(Field field, long v) {
    records.add(new Fixed64(field, v));
  }

  @Override
  public void visitSFixed32(Field field, int v) {
    records.add(new SFixed32(field, v));
  }

  @Override
  public void visitSFixed64(Field field, long v) {
    records.add(new SFixed64(field, v));
  }

  @Override
  public void enter(Field field) {

  }

  @Override
  public void leave(Field field) {

  }

  @Override
  public void destroy() {
    records.add(new Destroy());
  }

  public void apply(Visitor visitor) {
    for (Record record : records) {
      record.apply(visitor);
    }
  }

  public Checker checker() {
    Deque<Record> records = new ArrayDeque<>(RecordingVisitor.this.records);
    return new Checker(records);
  }

  public static class Checker implements Visitor {

    private final Deque<Record> expectations;

    public Checker(Deque<Record> expectations) {
      this.expectations = expectations;
    }

    private <E extends Record> E expecting(Class<E> type) {
      Record expectation = expectations.poll();
      assertNotNull(expectation);
      assertTrue("Expecting an instance of " + type.getName() + " instead of " + expectation.getClass().getName(), type.isInstance(expectation));
      return type.cast(expectation);
    }

    @Override
    public void init(MessageType type) {
      assertSame(expecting(Init.class).messageType, type);
    }

    @Override
    public void visitVarInt32(Field field, int v) {
      VarInt32 expectation = expecting(VarInt32.class);
      assertSame(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void visitVarInt64(Field field, long v) {
      VarInt64 expectation = expecting(VarInt64.class);
      assertSame(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void visitString(Field field, String s) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void visitDouble(Field field, double d) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void visitFixed32(Field field, int v) {
      Fixed32 expectation = expecting(Fixed32.class);
      assertSame(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void visitFixed64(Field field, long v) {
      Fixed64 expectation = expecting(Fixed64.class);
      assertSame(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void visitSFixed32(Field field, int v) {
      SFixed32 expectation = expecting(SFixed32.class);
      assertSame(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void visitSFixed64(Field field, long v) {
      SFixed64 expectation = expecting(SFixed64.class);
      assertSame(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void enter(Field field) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void leave(Field field) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void destroy() {
      expecting(Destroy.class);
    }

    public boolean isEmpty() {
      return expectations.isEmpty();
    }
  }
}
