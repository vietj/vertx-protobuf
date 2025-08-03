package io.vertx.tests.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.UnknownRecordVisitor;
import io.vertx.protobuf.Visitor;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.Assert.*;

public class RecordingVisitor implements Visitor, UnknownRecordVisitor {

  private static abstract class Record {
    protected abstract void apply(Visitor visitor, UnknownRecordVisitor unknownFieldHandler);
  }

  private static class Init extends Record {
    private final MessageType messageType;
    Init(MessageType messageType) {
      this.messageType = messageType;
    }
    @Override
    protected void apply(Visitor visitor, UnknownRecordVisitor unknownFieldHandler) {
      visitor.init(messageType);
    }
  }

  private static class Destroy extends Record {
    @Override
    protected void apply(Visitor visitor, UnknownRecordVisitor unknownFieldHandler) {
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
    protected void apply(Visitor visitor, UnknownRecordVisitor unknownFieldHandler) {
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
    protected void apply(Visitor visitor, UnknownRecordVisitor unknownFieldHandler) {
      visitor.visitDouble(field, value);
    }
  }

  private static class VisitInt64 extends Record {
    private final Field field;
    private final long value;
    VisitInt64(Field field, long value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(Visitor visitor, UnknownRecordVisitor unknownFieldHandler) {
      visitor.visitInt64(field, value);
    }
  }

  private static class VisitUInt64 extends Record {
    private final Field field;
    private final long value;
    VisitUInt64(Field field, long value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(Visitor visitor, UnknownRecordVisitor unknownFieldHandler) {
      visitor.visitUInt64(field, value);
    }
  }

  private static class VisitSInt64 extends Record {
    private final Field field;
    private final long value;
    VisitSInt64(Field field, long value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(Visitor visitor, UnknownRecordVisitor unknownFieldHandler) {
      visitor.visitSInt64(field, value);
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
    protected void apply(Visitor visitor, UnknownRecordVisitor unknownFieldHandler) {
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
    protected void apply(Visitor visitor, UnknownRecordVisitor unknownFieldHandler) {
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
    protected void apply(Visitor visitor, UnknownRecordVisitor unknownFieldHandler) {
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
    protected void apply(Visitor visitor, UnknownRecordVisitor unknownFieldHandler) {
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
    protected void apply(Visitor visitor, UnknownRecordVisitor unknownFieldHandler) {
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
    protected void apply(Visitor visitor, UnknownRecordVisitor unknownFieldHandler) {
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
    protected void apply(Visitor visitor, UnknownRecordVisitor unknownFieldHandler) {
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
    protected void apply(Visitor visitor, UnknownRecordVisitor unknownFieldHandler) {
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
    protected void apply(Visitor visitor, UnknownRecordVisitor unknownFieldHandler) {
      visitor.visitSFixed64(field, value);
    }
  }

  private static class UnknownLengthDelimited extends Record {
    private final MessageType messageType;
    private final int fieldNumber;
    private final Buffer buffer;
    public UnknownLengthDelimited(MessageType messageType, int fieldNumber, Buffer buffer) {
      this.messageType = messageType;
      this.fieldNumber = fieldNumber;
      this.buffer = buffer;
    }
    @Override
    protected void apply(Visitor visitor, UnknownRecordVisitor unknownFieldHandler) {
      throw new UnsupportedOperationException("TODO");
    }
  }

  private static class UnknownI32 extends Record {
    private final MessageType messageType;
    private final int fieldNumber;
    private final int value;
    public UnknownI32(MessageType messageType, int fieldNumber, int value) {
      this.messageType = messageType;
      this.fieldNumber = fieldNumber;
      this.value = value;
    }
    @Override
    protected void apply(Visitor visitor, UnknownRecordVisitor unknownRecordVisitor) {
      throw new UnsupportedOperationException("TODO");
    }
  }

  private static class UnknownI64 extends Record {
    private final MessageType messageType;
    private final int fieldNumber;
    private final long value;
    public UnknownI64(MessageType messageType, int fieldNumber, long value) {
      this.messageType = messageType;
      this.fieldNumber = fieldNumber;
      this.value = value;
    }
    @Override
    protected void apply(Visitor visitor, UnknownRecordVisitor unknownRecordVisitor) {
      throw new UnsupportedOperationException("TODO");
    }
  }

  private static class UnknownVarInt extends Record {
    private final MessageType messageType;
    private final int fieldNumber;
    private final long value;
    public UnknownVarInt(MessageType messageType, int fieldNumber, long value) {
      this.messageType = messageType;
      this.fieldNumber = fieldNumber;
      this.value = value;
    }
    @Override
    protected void apply(Visitor visitor, UnknownRecordVisitor unknownRecordVisitor) {
      throw new UnsupportedOperationException("TODO");
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
  public void visitInt64(Field field, long v) {
    records.add(new VisitInt64(field, v));
  }

  @Override
  public void visitUInt64(Field field, long v) {
    records.add(new VisitUInt64(field, v));
  }

  @Override
  public void visitSInt64(Field field, long v) {
    records.add(new VisitSInt64(field, v));
  }

  @Override
  public void visitString(Field field, String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visitBytes(Field field, byte[] bytes) {
    throw new UnsupportedOperationException();
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

  @Override
  public void visitUnknownLengthDelimited(MessageType messageType, int fieldNumber, Buffer payload) {
    records.add(new UnknownLengthDelimited(messageType, fieldNumber, payload));
  }

  @Override
  public void visitUnknownI32(MessageType messageType, int fieldNumber, int value) {
    records.add(new UnknownI32(messageType, fieldNumber, value));
  }

  @Override
  public void visitUnknownI64(MessageType messageType, int fieldNumber, long value) {
    records.add(new UnknownI64(messageType, fieldNumber, value));
  }

  @Override
  public void visitUnknownVarInt(MessageType messageType, int fieldNumber, long value) {
    records.add(new UnknownVarInt(messageType, fieldNumber, value));
  }

  public void apply(Visitor visitor) {
    apply(visitor, new UnknownRecordVisitor() {
      @Override
      public void visitUnknownLengthDelimited(MessageType messageType, int fieldNumber, Buffer payload) {
      }
      @Override
      public void visitUnknownI32(MessageType messageType, int fieldNumber, int value) {

      }
    });
  }

  public void apply(Visitor visitor, UnknownRecordVisitor unknownRecordVisitor) {
    for (Record record : records) {
      record.apply(visitor, unknownRecordVisitor);
    }
  }

  public Checker checker() {
    Deque<Record> records = new ArrayDeque<>(RecordingVisitor.this.records);
    return new Checker(records);
  }

  public static class Checker implements Visitor, UnknownRecordVisitor {

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
    public void visitInt64(Field field, long v) {
      VisitInt64 expectation = expecting(VisitInt64.class);
      assertSame(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void visitSInt64(Field field, long v) {
      VisitSInt64 expectation = expecting(VisitSInt64.class);
      assertSame(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void visitUInt64(Field field, long v) {
      VisitUInt64 expectation = expecting(VisitUInt64.class);
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
    public void visitBytes(Field field, byte[] bytes) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void destroy() {
      expecting(Destroy.class);
    }

    @Override
    public void visitUnknownLengthDelimited(MessageType messageType, int fieldNumber, Buffer payload) {
      UnknownLengthDelimited expectation = expecting(UnknownLengthDelimited.class);
      assertSame(expectation.messageType, messageType);
      assertSame(expectation.fieldNumber, fieldNumber);
      assertEquals(expectation.buffer, payload);
    }

    @Override
    public void visitUnknownI32(MessageType messageType, int fieldNumber, int value) {
      UnknownI32 expectation = expecting(UnknownI32.class);
      assertSame(expectation.messageType, messageType);
      assertSame(expectation.fieldNumber, fieldNumber);
      assertEquals(expectation.value, value);
    }

    @Override
    public void visitUnknownI64(MessageType messageType, int fieldNumber, long value) {
      UnknownI64 expectation = expecting(UnknownI64.class);
      assertSame(expectation.messageType, messageType);
      assertSame(expectation.fieldNumber, fieldNumber);
      assertEquals(expectation.value, value);
    }

    @Override
    public void visitUnknownVarInt(MessageType messageType, int fieldNumber, long value) {
      UnknownVarInt expectation = expecting(UnknownVarInt.class);
      assertSame(expectation.messageType, messageType);
      assertSame(expectation.fieldNumber, fieldNumber);
      assertEquals(expectation.value, value);
    }

    public boolean isEmpty() {
      return expectations.isEmpty();
    }
  }
}
