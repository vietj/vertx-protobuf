package io.vertx.tests.protobuf;

import com.google.protobuf.ByteString;
import com.google.protobuf.UnknownFieldSet;
import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.schema.TypeID;
import io.vertx.protobuf.schema.WireType;
import io.vertx.tests.unknown.SchemaLiterals;
import io.vertx.tests.unknown.UnknownProto;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

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
    ProtobufReader.parse(SchemaLiterals.MessageLiteral.Message, visitor, Buffer.buffer(bytes));
    RecordingVisitor.Checker checker = visitor.checker();
    checker.init(SchemaLiterals.MessageLiteral.Message);
    checker.visitBytes(SchemaLiterals.MessageLiteral.Message.unknownField(2, TypeID.BYTES, WireType.LEN), "Hello".getBytes(StandardCharsets.UTF_8));
    checker.visitBytes(SchemaLiterals.MessageLiteral.Message.unknownField(3, TypeID.BYTES, WireType.LEN), "World".getBytes(StandardCharsets.UTF_8));
    checker.destroy();
    assertTrue(checker.isEmpty());
  }

  @Test
  public void testUnknownFixed32() throws Exception {
    byte[] bytes = UnknownProto.Message.newBuilder()
      .setUnknownFields(UnknownFieldSet.newBuilder().addField(2, UnknownFieldSet.Field.newBuilder().addFixed32(15).build()).build())
      .build().toByteArray();
    RecordingVisitor visitor = new RecordingVisitor();
    ProtobufReader.parse(SchemaLiterals.MessageLiteral.Message, visitor, Buffer.buffer(bytes));
    RecordingVisitor.Checker checker = visitor.checker();
    checker.init(SchemaLiterals.MessageLiteral.Message);
    checker.visitFixed32(SchemaLiterals.MessageLiteral.Message.unknownField(2, TypeID.FIXED32, WireType.I32), 15);
    checker.destroy();
    assertTrue(checker.isEmpty());
  }

  @Test
  public void testUnknownFixed64() throws Exception {
    byte[] bytes = UnknownProto.Message.newBuilder()
      .setUnknownFields(UnknownFieldSet.newBuilder().addField(2, UnknownFieldSet.Field.newBuilder().addFixed64(15L).build()).build())
      .build().toByteArray();
    RecordingVisitor visitor = new RecordingVisitor();
    ProtobufReader.parse(SchemaLiterals.MessageLiteral.Message, visitor, Buffer.buffer(bytes));
    RecordingVisitor.Checker checker = visitor.checker();
    checker.init(SchemaLiterals.MessageLiteral.Message);
    checker.visitFixed64(SchemaLiterals.MessageLiteral.Message.unknownField(2, TypeID.FIXED64, WireType.I64), 15L);
    checker.destroy();
    assertTrue(checker.isEmpty());
  }

  @Test
  public void testUnknownVarInt() throws Exception {
    byte[] bytes = UnknownProto.Message.newBuilder()
      .setUnknownFields(UnknownFieldSet.newBuilder().addField(2, UnknownFieldSet.Field.newBuilder().addVarint(15L).build()).build())
      .build().toByteArray();
    RecordingVisitor visitor = new RecordingVisitor();
    ProtobufReader.parse(SchemaLiterals.MessageLiteral.Message, visitor, Buffer.buffer(bytes));
    RecordingVisitor.Checker checker = visitor.checker();
    checker.init(SchemaLiterals.MessageLiteral.Message);
    checker.visitVarInt64(SchemaLiterals.MessageLiteral.Message.unknownField(2, TypeID.INT64, WireType.VARINT), 15L);
    checker.destroy();
    assertTrue(checker.isEmpty());
  }
}
