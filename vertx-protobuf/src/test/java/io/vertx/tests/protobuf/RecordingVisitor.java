package io.vertx.tests.protobuf;

import io.vertx.protobuf.RecordVisitor;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.Assert.*;

public class RecordingVisitor implements RecordVisitor {

  private static abstract class Action {
    protected abstract void apply(RecordVisitor visitor);
  }

  private static class Enter extends Action {
    private final Field field;
    public Enter(Field field) {
      this.field = field;
    }
    @Override
    protected void apply(RecordVisitor visitor) {
      visitor.enter(field);
    }
  }

  private static class Leave extends Action {
    private final Field field;
    public Leave(Field field) {
      this.field = field;
    }
    @Override
    protected void apply(RecordVisitor visitor) {
      visitor.leave(field);
    }
  }

  private static class EnterPacked extends Action {
    private final Field field;
    public EnterPacked(Field field) {
      this.field = field;
    }
    @Override
    protected void apply(RecordVisitor visitor) {
      visitor.enterPacked(field);
    }
  }

  private static class LeavePacked extends Action {
    private final Field field;
    public LeavePacked(Field field) {
      this.field = field;
    }
    @Override
    protected void apply(RecordVisitor visitor) {
      visitor.leavePacked(field);
    }
  }

  private static class Init extends Action {
    private final MessageType messageType;
    Init(MessageType messageType) {
      this.messageType = messageType;
    }
    @Override
    protected void apply(RecordVisitor visitor) {
      visitor.init(messageType);
    }
  }

  private static class Destroy extends Action {
    @Override
    protected void apply(RecordVisitor visitor) {
      visitor.destroy();
    }
  }

  private static class Float extends Action {
    private final Field field;
    private final float value;
    Float(Field field, float value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(RecordVisitor visitor) {
      visitor.visitFloat(field, value);
    }
  }

  private static class Double extends Action {
    private final Field field;
    private final double value;
    Double(Field field, double value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(RecordVisitor visitor) {
      visitor.visitDouble(field, value);
    }
  }

  private static class VisitInt64 extends Action {
    private final Field field;
    private final long value;
    VisitInt64(Field field, long value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(RecordVisitor visitor) {
      visitor.visitInt64(field, value);
    }
  }

  private static class VisitUInt64 extends Action {
    private final Field field;
    private final long value;
    VisitUInt64(Field field, long value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(RecordVisitor visitor) {
      visitor.visitUInt64(field, value);
    }
  }

  private static class VisitSInt64 extends Action {
    private final Field field;
    private final long value;
    VisitSInt64(Field field, long value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(RecordVisitor visitor) {
      visitor.visitSInt64(field, value);
    }
  }

  private static class VisitInt32 extends Action {
    private final Field field;
    private final int value;
    VisitInt32(Field field, int value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(RecordVisitor visitor) {
      visitor.visitInt32(field, value);
    }
  }

  private static class VisitSInt32 extends Action {
    private final Field field;
    private final int value;
    VisitSInt32(Field field, int value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(RecordVisitor visitor) {
      visitor.visitSInt32(field, value);
    }
  }

  private static class VisitUInt32 extends Action {
    private final Field field;
    private final int value;
    VisitUInt32(Field field, int value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(RecordVisitor visitor) {
      visitor.visitUInt32(field, value);
    }
  }

  private static class VisitEnum extends Action {
    private final Field field;
    private final int value;
    VisitEnum(Field field, int value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(RecordVisitor visitor) {
      visitor.visitEnum(field, value);
    }
  }

  private static class VisitBool extends Action {
    private final Field field;
    private final boolean value;
    VisitBool(Field field, boolean value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(RecordVisitor visitor) {
      visitor.visitBool(field, value);
    }
  }

  private static class Fixed32 extends Action {
    private final Field field;
    private final int value;
    Fixed32(Field field, int value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(RecordVisitor visitor) {
      visitor.visitFixed32(field, value);
    }
  }

  private static class Fixed64 extends Action {
    private final Field field;
    private final long value;
    Fixed64(Field field, long value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(RecordVisitor visitor) {
      visitor.visitFixed64(field, value);
    }
  }

  private static class SFixed32 extends Action {
    private final Field field;
    private final int value;
    SFixed32(Field field, int value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(RecordVisitor visitor) {
      visitor.visitSFixed32(field, value);
    }
  }

  private static class SFixed64 extends Action {
    private final Field field;
    private final long value;
    SFixed64(Field field, long value) {
      this.field = field;
      this.value = value;
    }
    @Override
    protected void apply(RecordVisitor visitor) {
      visitor.visitSFixed64(field, value);
    }
  }

  private static class VisitBytes extends Action {
    private final Field field;
    private final byte[] bytes;
    public VisitBytes(Field field, byte[] bytes) {
      this.field = field;
      this.bytes = bytes;
    }
    @Override
    protected void apply(RecordVisitor visitor) {
      visitor.visitBytes(field, bytes.clone());
    }
  }

  private static class VisitString extends Action {
    private final Field field;
    private final String s;
    public VisitString(Field field, String s) {
      this.field = field;
      this.s = s;
    }
    @Override
    protected void apply(RecordVisitor visitor) {
      visitor.visitString(field, s);
    }
  }

  private List<Action> log = new ArrayList<>();

  @Override
  public void init(MessageType type) {
    log.add(new Init(type));
  }

  @Override
  public void visitInt32(Field field, int v) {
    log.add(new VisitInt32(field, v));
  }

  @Override
  public void visitUInt32(Field field, int v) {
    log.add(new VisitUInt32(field, v));
  }

  @Override
  public void visitSInt32(Field field, int v) {
    log.add(new VisitSInt32(field, v));
  }

  @Override
  public void visitBool(Field field, boolean v) {
    log.add(new VisitBool(field, v));
  }

  @Override
  public void visitEnum(Field field, int number) {
    log.add(new VisitEnum(field, number));
  }

  @Override
  public void visitInt64(Field field, long v) {
    log.add(new VisitInt64(field, v));
  }

  @Override
  public void visitUInt64(Field field, long v) {
    log.add(new VisitUInt64(field, v));
  }

  @Override
  public void visitSInt64(Field field, long v) {
    log.add(new VisitSInt64(field, v));
  }

  @Override
  public void visitString(Field field, String s) {
    log.add(new VisitString(field, s));
  }

  @Override
  public void visitBytes(Field field, byte[] bytes) {
    log.add(new VisitBytes(field, bytes));
  }

  @Override
  public void visitFloat(Field field, float f) {
    log.add(new Float(field, f));
  }

  @Override
  public void visitDouble(Field field, double d) {
    log.add(new Double(field, d));
  }

  @Override
  public void visitFixed32(Field field, int v) {
    log.add(new Fixed32(field, v));
  }

  @Override
  public void visitFixed64(Field field, long v) {
    log.add(new Fixed64(field, v));
  }

  @Override
  public void visitSFixed32(Field field, int v) {
    log.add(new SFixed32(field, v));
  }

  @Override
  public void visitSFixed64(Field field, long v) {
    log.add(new SFixed64(field, v));
  }

  @Override
  public void enterPacked(Field field) {
    log.add(new EnterPacked(field));
  }

  @Override
  public void enter(Field field) {
    log.add(new Enter(field));
  }

  @Override
  public void leavePacked(Field field) {
    log.add(new LeavePacked(field));
  }

  @Override
  public void leave(Field field) {
    log.add(new Leave(field));
  }

  @Override
  public void destroy() {
    log.add(new Destroy());
  }

  public void apply(RecordVisitor visitor) {
    for (Action action : log) {
      action.apply(visitor);
    }
  }

  public Checker checker() {
    Deque<Action> log = new ArrayDeque<>(RecordingVisitor.this.log);
    return new Checker(log);
  }

  public static class Checker implements RecordVisitor {

    private final Deque<Action> expectations;

    public Checker(Deque<Action> expectations) {
      this.expectations = expectations;
    }

    private <E extends Action> E expecting(Class<E> type) {
      Action expectation = expectations.poll();
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
      assertEquals(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void visitUInt32(Field field, int v) {
      VisitUInt32 expectation = expecting(VisitUInt32.class);
      assertEquals(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void visitSInt32(Field field, int v) {
      VisitSInt32 expectation = expecting(VisitSInt32.class);
      assertEquals(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void visitBool(Field field, boolean v) {
      VisitBool expectation = expecting(VisitBool.class);
      assertEquals(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void visitEnum(Field field, int number) {
      VisitEnum expectation = expecting(VisitEnum.class);
      assertEquals(expectation.field, field);
      assertEquals(expectation.value, number);
    }

    @Override
    public void visitInt64(Field field, long v) {
      VisitInt64 expectation = expecting(VisitInt64.class);
      assertEquals(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void visitSInt64(Field field, long v) {
      VisitSInt64 expectation = expecting(VisitSInt64.class);
      assertEquals(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void visitUInt64(Field field, long v) {
      VisitUInt64 expectation = expecting(VisitUInt64.class);
      assertEquals(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void visitString(Field field, String s) {
      VisitString expectation = expecting(VisitString.class);
      assertEquals(expectation.field, field);
      assertEquals(expectation.s, s);
    }

    @Override
    public void visitFloat(Field field, float f) {
      Float expectation = expecting(Float.class);
      assertEquals(expectation.field, field);
      assertEquals(expectation.value, f, 0.001D);
    }

    @Override
    public void visitDouble(Field field, double d) {
      Double expectation = expecting(Double.class);
      assertEquals(expectation.field, field);
      assertEquals(expectation.value, d, 0.001D);
    }

    @Override
    public void visitFixed32(Field field, int v) {
      Fixed32 expectation = expecting(Fixed32.class);
      assertEquals(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void visitFixed64(Field field, long v) {
      Fixed64 expectation = expecting(Fixed64.class);
      assertEquals(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void visitSFixed32(Field field, int v) {
      SFixed32 expectation = expecting(SFixed32.class);
      assertEquals(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void visitSFixed64(Field field, long v) {
      SFixed64 expectation = expecting(SFixed64.class);
      assertEquals(expectation.field, field);
      assertEquals(expectation.value, v);
    }

    @Override
    public void enterPacked(Field field) {
      EnterPacked enter = expecting(EnterPacked.class);
      assertEquals(enter.field, field);
    }

    @Override
    public void enter(Field field) {
      Enter enter = expecting(Enter.class);
      assertEquals(enter.field, field);
    }

    @Override
    public void leavePacked(Field field) {
      LeavePacked leave = expecting(LeavePacked.class);
      assertEquals(leave.field, field);
    }

    @Override
    public void leave(Field field) {
      Leave leave = expecting(Leave.class);
      assertEquals(leave.field, field);
    }

    @Override
    public void visitBytes(Field field, byte[] bytes) {
      VisitBytes expectation = expecting(VisitBytes.class);
      assertEquals(expectation.field, field);
      assertArrayEquals(expectation.bytes, bytes);
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
