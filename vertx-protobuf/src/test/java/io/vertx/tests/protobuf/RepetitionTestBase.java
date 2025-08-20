package io.vertx.tests.protobuf;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.tests.repetition.Enumerated;
import io.vertx.tests.repetition.FieldLiteral;
import io.vertx.tests.repetition.MessageLiteral;
import io.vertx.tests.repetition.Packed;
import io.vertx.tests.repetition.ProtoWriter;
import io.vertx.tests.repetition.Repeated;
import io.vertx.tests.repetition.RepetitionProto;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public abstract class RepetitionTestBase {

  @Test
  public void testParseUnpackedInt32Repetition() {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(MessageLiteral.Repeated);
    visitor.visitInt32(FieldLiteral.Repeated_int32, 0);
    visitor.visitInt32(FieldLiteral.Repeated_int32, 1);
    visitor.destroy();
    assertRepetition(RepetitionProto.Repeated.newBuilder().addInt32(0).addInt32(1).build(), visitor);
  }

  @Test
  public void testParseUnpackedStringRepetition() {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(MessageLiteral.Repeated);
    visitor.visitString(FieldLiteral.Repeated_string, "s1");
    visitor.visitString(FieldLiteral.Repeated_string, "s2");
    visitor.destroy();
    assertRepetition(RepetitionProto.Repeated.newBuilder().addString("s1").addString("s2").build(), visitor);
  }

  protected void assertRepetition(RepetitionProto.Repeated repeated, RecordingVisitor visitor) {
    assertRepetition(repeated, MessageLiteral.Repeated, visitor);
  }

  protected void assertRepetition(RepetitionProto.Packed repeated, RecordingVisitor visitor) {
    assertRepetition(repeated, MessageLiteral.Packed, visitor);
  }

  protected abstract void assertRepetition(MessageLite message, MessageType type, RecordingVisitor visitor);

  @Test
  public void testParseRepetition() throws Exception {
    RepetitionProto.Repeated r = RepetitionProto.Repeated.newBuilder()
      .addString("0")
      .addString("1")
      .addBytes(ByteString.copyFromUtf8("0"))
      .addBytes(ByteString.copyFromUtf8("1"))
      .addInt32(0)
      .addInt32(1)
      .addInt64(0)
      .addInt64(1)
      .addUint32(0)
      .addUint32(1)
      .addUint64(0)
      .addUint64(1)
      .addSint32(0)
      .addSint32(1)
      .addSint64(0)
      .addSint64(1)
      .addBool(true)
      .addBool(false)
      .addEnum(RepetitionProto.Enumerated.constant_0)
      .addEnum(RepetitionProto.Enumerated.constant_1)
      .addFixed64(0)
      .addFixed64(1)
      .addSfixed64(0)
      .addSfixed64(1)
      .addDouble(0D)
      .addDouble(1D)
      .addFixed32(0)
      .addFixed32(1)
      .addSfixed32(0)
      .addSfixed32(1)
      .addFloat(0F)
      .addFloat(1F)
      .build();
    Repeated msg = parseRepetition(r, MessageLiteral.Repeated);
    assertEquals(Arrays.asList("0", "1"), msg.getString());
    assertEquals(Arrays.asList(Buffer.buffer("0"), Buffer.buffer("1")), msg.getBytes());
    assertEquals(Arrays.asList(0, 1), msg.getInt32());
    assertEquals(Arrays.asList(0L, 1L), msg.getInt64());
    assertEquals(Arrays.asList(0, 1), msg.getUint32());
    assertEquals(Arrays.asList(0L, 1L), msg.getUint64());
    assertEquals(Arrays.asList(0L, 1L), msg.getSint64());
    assertEquals(Arrays.asList(0, 1), msg.getFixed32());
    assertEquals(Arrays.asList(true, false), msg.getBool());
    assertEquals(Arrays.asList(io.vertx.tests.repetition.Enumerated.constant_0, Enumerated.constant_1), msg.getEnum());
    assertEquals(Arrays.asList(0L, 1L), msg.getFixed64());
    assertEquals(Arrays.asList(0L, 1L), msg.getSfixed64());
    assertEquals(Arrays.asList(0d, 1d), msg.getDouble());
    assertEquals(Arrays.asList(0, 1), msg.getSint32());
    assertEquals(Arrays.asList(0, 1), msg.getSfixed32());
    assertEquals(Arrays.asList(0f, 1f), msg.getFloat());
  }

  @Test
  public void testParsePackedRepetition() throws Exception {
    RepetitionProto.Packed p = RepetitionProto.Packed.newBuilder()
      .addInt32(0)
      .addInt32(1)
      .addInt64(0)
      .addInt64(1)
      .addUint32(0)
      .addUint32(1)
      .addUint64(0)
      .addUint64(1)
      .addSint32(0)
      .addSint32(1)
      .addSint64(0)
      .addSint64(1)
      .addBool(true)
      .addBool(false)
      .addEnum(RepetitionProto.Enumerated.constant_0)
      .addEnum(RepetitionProto.Enumerated.constant_1)
      .addFixed64(0)
      .addFixed64(1)
      .addSfixed64(0)
      .addSfixed64(1)
      .addDouble(0D)
      .addDouble(1D)
      .addFixed32(0)
      .addFixed32(1)
      .addSfixed32(0)
      .addSfixed32(1)
      .addFloat(0F)
      .addFloat(1F)
      .build();
    Packed msg = parseRepetition(p, MessageLiteral.Packed);
    assertEquals(Arrays.asList(0, 1), msg.getInt32());
    assertEquals(Arrays.asList(0L, 1L), msg.getInt64());
    assertEquals(Arrays.asList(0, 1), msg.getUint32());
    assertEquals(Arrays.asList(0L, 1L), msg.getUint64());
    assertEquals(Arrays.asList(0L, 1L), msg.getSint64());
    assertEquals(Arrays.asList(0, 1), msg.getFixed32());
    assertEquals(Arrays.asList(true, false), msg.getBool());
    assertEquals(Arrays.asList(Enumerated.constant_0, Enumerated.constant_1), msg.getEnum());
    assertEquals(Arrays.asList(0L, 1L), msg.getFixed64());
    assertEquals(Arrays.asList(0L, 1L), msg.getSfixed64());
    assertEquals(Arrays.asList(0d, 1d), msg.getDouble());
    assertEquals(Arrays.asList(0, 1), msg.getSint32());
    assertEquals(Arrays.asList(0, 1), msg.getSfixed32());
    assertEquals(Arrays.asList(0f, 1f), msg.getFloat());
  }

  @Test
  public void testWritePackedRepetition() throws Exception {
    RepetitionProto.Packed p = RepetitionProto.Packed.newBuilder()
      .addInt32(0)
      .addInt32(1)
      .addInt32(2)
      .addInt32(3)
      .addInt32(4)
      .build();
    Packed msg = parseRepetition(p, MessageLiteral.Packed);
    assertEquals(Arrays.asList(0, 1, 2, 3, 4), msg.getInt32());
    byte[] actual = ProtobufWriter.encode(visitor -> ProtoWriter.emit(msg, visitor)).getBytes();
    assertEquals(5, actual[1]);
    RepetitionProto.Packed blah = RepetitionProto.Packed.parseFrom(actual);
    assertEquals(Arrays.asList(0, 1, 2, 3, 4), blah.getInt32List());
  }

  protected  abstract <T> T parseRepetition(MessageLite message, MessageType type);

  }
