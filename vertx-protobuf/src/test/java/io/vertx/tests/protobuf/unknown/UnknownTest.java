package io.vertx.tests.protobuf.unknown;

import com.google.protobuf.ByteString;
import com.google.protobuf.UnknownFieldSet;
import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.WireType;
import io.vertx.tests.protobuf.RecordingVisitor;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

public class UnknownTest {

  @Test
  public void testUnknownLengthDelimited() throws Exception {
    byte[] bytes = UnknownProto.Message.newBuilder()
      .setUnknownFields(UnknownFieldSet.newBuilder()
        .addField(2, UnknownFieldSet.Field.newBuilder().addLengthDelimited(ByteString.copyFromUtf8("Hello")).build())
        .addField(3, UnknownFieldSet.Field.newBuilder().addLengthDelimited(ByteString.copyFromUtf8("World")).build())
        .build()
      ).build().toByteArray();
    RecordingVisitor visitor = new RecordingVisitor();
    ProtobufReader.parse(MessageLiteral.Message, visitor, Buffer.buffer(bytes));
    RecordingVisitor.Checker checker = visitor.checker();
    checker.init(MessageLiteral.Message);
    Field uf2 = MessageLiteral.Message.unknownField(2, WireType.LEN);
    checker.enter(uf2);
    checker.visitBytes(uf2, "Hello".getBytes(StandardCharsets.UTF_8));
    checker.leave(uf2);
    Field uf3 = MessageLiteral.Message.unknownField(3, WireType.LEN);
    checker.enter(uf3);
    checker.visitBytes(uf3, "World".getBytes(StandardCharsets.UTF_8));
    checker.leave(uf3);
    checker.destroy();
    assertTrue(checker.isEmpty());
  }

  @Test
  public void testUnknownFixed32() throws Exception {
    byte[] bytes = UnknownProto.Message.newBuilder()
      .setUnknownFields(UnknownFieldSet.newBuilder().addField(2, UnknownFieldSet.Field.newBuilder().addFixed32(15).build()).build())
      .build().toByteArray();
    RecordingVisitor visitor = new RecordingVisitor();
    ProtobufReader.parse(MessageLiteral.Message, visitor, Buffer.buffer(bytes));
    RecordingVisitor.Checker checker = visitor.checker();
    checker.init(MessageLiteral.Message);
    checker.visitFixed32(MessageLiteral.Message.unknownField(2, WireType.I32), 15);
    checker.destroy();
    assertTrue(checker.isEmpty());
  }

  @Test
  public void testUnknownFixed64() throws Exception {
    byte[] bytes = UnknownProto.Message.newBuilder()
      .setUnknownFields(UnknownFieldSet.newBuilder().addField(2, UnknownFieldSet.Field.newBuilder().addFixed64(15L).build()).build())
      .build().toByteArray();
    RecordingVisitor visitor = new RecordingVisitor();
    ProtobufReader.parse(MessageLiteral.Message, visitor, Buffer.buffer(bytes));
    RecordingVisitor.Checker checker = visitor.checker();
    checker.init(MessageLiteral.Message);
    checker.visitFixed64(MessageLiteral.Message.unknownField(2, WireType.I64), 15L);
    checker.destroy();
    assertTrue(checker.isEmpty());
  }

  @Test
  public void testUnknownVarInt() throws Exception {
    byte[] bytes = UnknownProto.Message.newBuilder()
      .setUnknownFields(UnknownFieldSet.newBuilder().addField(2, UnknownFieldSet.Field.newBuilder().addVarint(15L).build()).build())
      .build().toByteArray();
    RecordingVisitor visitor = new RecordingVisitor();
    ProtobufReader.parse(MessageLiteral.Message, visitor, Buffer.buffer(bytes));
    RecordingVisitor.Checker checker = visitor.checker();
    checker.init(MessageLiteral.Message);
    checker.visitVarInt64(MessageLiteral.Message.unknownField(2, WireType.VARINT), 15L);
    checker.destroy();
    assertTrue(checker.isEmpty());
  }

  @Test
  public void testMessage() throws Exception {
    byte[] bytes = UnknownProto.Message.newBuilder()
      .setUnknownFields(UnknownFieldSet.newBuilder()
        .addField(2, UnknownFieldSet.Field.newBuilder().addLengthDelimited(ByteString.copyFromUtf8("Hello")).build())
        .addField(3, UnknownFieldSet.Field.newBuilder().addLengthDelimited(ByteString.copyFromUtf8("World")).build())
        .addField(4, UnknownFieldSet.Field.newBuilder().addFixed64(15L).addFixed64(20L).build())
        .addField(5, UnknownFieldSet.Field.newBuilder().addFixed32(17).build())
        .addField(6, UnknownFieldSet.Field.newBuilder().addVarint(18L).build())
        .build()
      ).build().toByteArray();
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(MessageLiteral.Message, reader, Buffer.buffer(bytes));
    Message msg = (Message) reader.stack.pop();
    assertNotNull(msg.unknownFields);
    assertEquals(Map.of(
      MessageLiteral.Message.unknownField(2, WireType.LEN), Collections.singletonList(Buffer.buffer("Hello")),
      MessageLiteral.Message.unknownField(3, WireType.LEN), Collections.singletonList(Buffer.buffer("World")),
      MessageLiteral.Message.unknownField(4, WireType.I64), Arrays.asList(15L, 20L),
      MessageLiteral.Message.unknownField(5, WireType.I32), Collections.singletonList(17),
      MessageLiteral.Message.unknownField(6, WireType.VARINT), Collections.singletonList(18L)
    )
      , msg.unknownFields);
    bytes = ProtobufWriter.encodeToByteArray(visitor -> ProtoWriter.emit(msg, visitor));
    UnknownProto.Message protoMsg = UnknownProto.Message.parseFrom(bytes);
    String stringUtf8 = protoMsg.getUnknownFields().getField(2).getLengthDelimitedList().get(0).toStringUtf8();
    assertEquals("Hello", stringUtf8);
  }
}
