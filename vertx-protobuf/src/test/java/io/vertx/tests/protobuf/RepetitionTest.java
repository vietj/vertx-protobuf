package io.vertx.tests.protobuf;

import com.google.protobuf.MessageLite;
import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.tests.repetition.ProtoReader;
import io.vertx.tests.repetition.MessageLiteral;
import io.vertx.tests.repetition.FieldLiteral;
import io.vertx.tests.repetition.RepetitionProto;
import org.junit.Test;

public class RepetitionTest extends RepetitionTestBase {

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

  @Override
  protected void assertRepetition(MessageLite message, MessageType type, RecordingVisitor visitor) {
    byte[] bytes = message.toByteArray();
    ProtobufReader.parse(type, visitor.checker(), Buffer.buffer(bytes));
  }

  protected  <T> T parseRepetition(MessageLite message, MessageType type) {
    byte[] bytes = message.toByteArray();
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(type, reader, Buffer.buffer(bytes));
    return (T) reader.stack.pop();
  }
}
