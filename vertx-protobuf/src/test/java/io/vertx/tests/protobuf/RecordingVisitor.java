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

  private static class Float extends Record {
    private final Field field;
    private final float value;
    Float(Field field, float value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(Visitor visitor) {
      visitor.visitFloat(field, value);
    }
  }

  private static class Double extends Record {
    private final Field field;
    private final double value;
    Double(Field field, double value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(Visitor visitor) {
      visitor.visitDouble(field, value);
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

  private static class VisitInt32 extends Record {
    private final Field field;
    private final int value;
    VisitInt32(Field field, int value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(Visitor visitor) {
      visitor.visitInt32(field, value);
    }
  }

  private static class VisitSInt32 extends Record {
    private final Field field;
    private final int value;
    VisitSInt32(Field field, int value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(Visitor visitor) {
      visitor.visitSInt32(field, value);
    }
  }

  private static class VisitUInt32 extends Record {
    private final Field field;
    private final int value;
    VisitUInt32(Field field, int value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(Visitor visitor) {
      visitor.visitUInt32(field, value);
    }
  }

  private static class VisitEnum extends Record {
    private final Field field;
    private final int value;
    VisitEnum(Field field, int value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(Visitor visitor) {
      visitor.visitEnum(field, value);
    }
  }

  private static class VisitBool extends Record {
    private final Field field;
    private final boolean value;
    VisitBool(Field field, boolean value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(Visitor visitor) {
      visitor.visitBool(field, value);
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
  public void visitInt32(Field field, int v) {
    records.add(new VisitInt32(field, v));
  }

  @Override
  public void visitUInt32(Field field, int v) {
    records.add(new VisitUInt32(field, v));
  }

  @Override
  public void visitSInt32(Field field, int v) {
    records.add(new VisitSInt32(field, v));
  }

  @Override
  public void visitBool(Field field, boolean v) {
    records.add(new VisitBool(field, v));
  }

  @Override
  public void visitEnum(Field field, int number) {
    records.add(new VisitEnum(field, number));
  }

  @Override
  public void visitVarInt64(Field field, long v) {
    records.add(new VarInt64(field, v));
  }

  @Override
  public void visitString(Field field, String s) {

  }

  @Override
  public void visitFloat(Field field, float f) {
    records.add(new Float(field, f));
  }

  @Override
  public void visitDouble(Field field, double d) {
    records.add(new Double(field, d));
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
    public void visitInt32(Field field, int v) {
      VisitInt32 expectation = expecting(VisitInt32.class);
      assertSame(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void visitUInt32(Field field, int v) {
      VisitUInt32 expectation = expecting(VisitUInt32.class);
      assertSame(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void visitSInt32(Field field, int v) {
      VisitSInt32 expectation = expecting(VisitSInt32.class);
      assertSame(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void visitBool(Field field, boolean v) {
      VisitBool expectation = expecting(VisitBool.class);
      assertSame(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void visitEnum(Field field, int number) {
      VisitEnum expectation = expecting(VisitEnum.class);
      assertSame(expectation.field, field);
      assertEquals(expectation.value, number);
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
    public void visitFloat(Field field, float f) {
      Float expectation = expecting(Float.class);
      assertSame(expectation.field, field);
      assertEquals(expectation.value, f, 0.001D);
    }

    @Override
    public void visitDouble(Field field, double d) {
      Double expectation = expecting(Double.class);
      assertSame(expectation.field, field);
      assertEquals(expectation.value, d, 0.001D);
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
