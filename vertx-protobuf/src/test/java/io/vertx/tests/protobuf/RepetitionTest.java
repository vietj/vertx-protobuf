package io.vertx.tests.protobuf;

import com.google.protobuf.ByteString;
import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.tests.repetition.Enum;
import io.vertx.tests.repetition.Packed;
import io.vertx.tests.repetition.ProtoReader;
import io.vertx.tests.repetition.ProtoWriter;
import io.vertx.tests.repetition.Repeated;
import io.vertx.tests.repetition.MessageLiteral;
import io.vertx.tests.repetition.FieldLiteral;
import io.vertx.tests.repetition.RepetitionProto;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class RepetitionTest {

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
  public void testParsePackedInt32Repetition() {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(MessageLiteral.Packed);
    visitor.enterPacked(FieldLiteral.Packed_int32);
    visitor.visitInt32(FieldLiteral.Packed_int32, 0);
    visitor.visitInt32(FieldLiteral.Packed_int32, 1);
    visitor.leavePacked(FieldLiteral.Packed_int32);
    visitor.destroy();
    assertRepetition(RepetitionProto.Packed.newBuilder().addInt32(0).addInt32(1).build(), visitor);
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

  @Test
  public void testParseUnpackedEmbeddedRepetition() {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(MessageLiteral.Repeated);
    visitor.enter(FieldLiteral.Repeated_embedded);
    visitor.leave(FieldLiteral.Repeated_embedded);
    visitor.enter(FieldLiteral.Repeated_embedded);
    visitor.visitInt32(FieldLiteral.Embedded_value, 1);
    visitor.enterPacked(FieldLiteral.Embedded_packed);
    visitor.visitInt32(FieldLiteral.Embedded_packed, 1);
    visitor.visitInt32(FieldLiteral.Embedded_packed, 2);
    visitor.leavePacked(FieldLiteral.Embedded_packed);
    visitor.visitInt32(FieldLiteral.Embedded_unpacked, 3);
    visitor.visitInt32(FieldLiteral.Embedded_unpacked, 4);
    visitor.leave(FieldLiteral.Repeated_embedded);
    visitor.enter(FieldLiteral.Repeated_embedded);
    visitor.visitInt32(FieldLiteral.Embedded_value, 2);
    visitor.leave(FieldLiteral.Repeated_embedded);
    visitor.destroy();
    assertRepetition(RepetitionProto.Repeated.newBuilder()
      .addEmbedded(RepetitionProto.Embedded.newBuilder().setValue(0).build())
      .addEmbedded(RepetitionProto.Embedded.newBuilder().setValue(1).addPacked(1).addPacked(2).addUnpacked(3).addUnpacked(4).build())
      .addEmbedded(RepetitionProto.Embedded.newBuilder().setValue(2).build())
      .build(), visitor);
  }

  private void assertRepetition(RepetitionProto.Repeated repeated, RecordingVisitor visitor) {
    byte[] bytes = repeated.toByteArray();
    ProtobufReader.parse(MessageLiteral.Repeated, visitor.checker(), Buffer.buffer(bytes));
  }

  private void assertRepetition(RepetitionProto.Packed repeated, RecordingVisitor visitor) {
    byte[] bytes = repeated.toByteArray();
    ProtobufReader.parse(MessageLiteral.Packed, visitor.checker(), Buffer.buffer(bytes));
  }

  @Test
  public void testParseRepetition() throws Exception {
    byte[] bytes = RepetitionProto.Repeated.newBuilder()
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
      .addEnum(RepetitionProto.Enum.constant_0)
      .addEnum(RepetitionProto.Enum.constant_1)
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
      .build().toByteArray();
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(MessageLiteral.Repeated, reader, Buffer.buffer(bytes));
    Repeated msg = (Repeated) reader.stack.pop();
    assertEquals(Arrays.asList("0", "1"), msg.getString());
    assertEquals(Arrays.asList(Buffer.buffer("0"), Buffer.buffer("1")), msg.getBytes());
    assertEquals(Arrays.asList(0, 1), msg.getInt32());
    assertEquals(Arrays.asList(0L, 1L), msg.getInt64());
    assertEquals(Arrays.asList(0, 1), msg.getUint32());
    assertEquals(Arrays.asList(0L, 1L), msg.getUint64());
    assertEquals(Arrays.asList(0L, 1L), msg.getSint64());
    assertEquals(Arrays.asList(0, 1), msg.getFixed32());
    assertEquals(Arrays.asList(true, false), msg.getBool());
    assertEquals(Arrays.asList(Enum.constant_0, Enum.constant_1), msg.getEnum());
    assertEquals(Arrays.asList(0L, 1L), msg.getFixed64());
    assertEquals(Arrays.asList(0L, 1L), msg.getSfixed64());
    assertEquals(Arrays.asList(0d, 1d), msg.getDouble());
    assertEquals(Arrays.asList(0, 1), msg.getSint32());
    assertEquals(Arrays.asList(0, 1), msg.getSfixed32());
    assertEquals(Arrays.asList(0f, 1f), msg.getFloat());
  }

  @Test
  public void testParsePackedRepetition() throws Exception {
    byte[] bytes = RepetitionProto.Packed.newBuilder()
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
      .addEnum(RepetitionProto.Enum.constant_0)
      .addEnum(RepetitionProto.Enum.constant_1)
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
      .build().toByteArray();
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(MessageLiteral.Packed, reader, Buffer.buffer(bytes));
    Packed msg = (Packed) reader.stack.pop();
    assertEquals(Arrays.asList(0, 1), msg.getInt32());
    assertEquals(Arrays.asList(0L, 1L), msg.getInt64());
    assertEquals(Arrays.asList(0, 1), msg.getUint32());
    assertEquals(Arrays.asList(0L, 1L), msg.getUint64());
    assertEquals(Arrays.asList(0L, 1L), msg.getSint64());
    assertEquals(Arrays.asList(0, 1), msg.getFixed32());
    assertEquals(Arrays.asList(true, false), msg.getBool());
    assertEquals(Arrays.asList(Enum.constant_0, Enum.constant_1), msg.getEnum());
    assertEquals(Arrays.asList(0L, 1L), msg.getFixed64());
    assertEquals(Arrays.asList(0L, 1L), msg.getSfixed64());
    assertEquals(Arrays.asList(0d, 1d), msg.getDouble());
    assertEquals(Arrays.asList(0, 1), msg.getSint32());
    assertEquals(Arrays.asList(0, 1), msg.getSfixed32());
    assertEquals(Arrays.asList(0f, 1f), msg.getFloat());
  }

  @Test
  public void testWritePackedRepetition() throws Exception {
    byte[] expected = RepetitionProto.Packed.newBuilder()
      .addInt32(0)
      .addInt32(1)
      .addInt32(2)
      .addInt32(3)
      .addInt32(4)
      .build().toByteArray();
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(MessageLiteral.Packed, reader, Buffer.buffer(expected));
    Packed msg = (Packed) reader.stack.pop();
    assertEquals(Arrays.asList(0, 1, 2, 3, 4), msg.getInt32());
    byte[] actual = ProtobufWriter.encode(visitor -> ProtoWriter.emit(msg, visitor)).getBytes();
    assertEquals(5, actual[1]);
    RepetitionProto.Packed blah = RepetitionProto.Packed.parseFrom(actual);
    assertEquals(Arrays.asList(0, 1, 2, 3, 4), blah.getInt32List());
  }
}
