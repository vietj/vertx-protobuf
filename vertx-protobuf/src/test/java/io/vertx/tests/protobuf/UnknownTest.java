package io.vertx.tests.protobuf;

import com.google.protobuf.ByteString;
import com.google.protobuf.UnknownFieldSet;
import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.tests.unknown.SchemaLiterals;
import io.vertx.tests.unknown.UnknownProto;
import org.junit.Test;

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
    ProtobufReader.parse(SchemaLiterals.MessageLiteral.Message, visitor, visitor, Buffer.buffer(bytes));
    RecordingVisitor.Checker checker = visitor.checker();
    checker.init(SchemaLiterals.MessageLiteral.Message);
    checker.visitUnknownLengthDelimited(SchemaLiterals.MessageLiteral.Message, 2, Buffer.buffer("Hello"));
    checker.visitUnknownLengthDelimited(SchemaLiterals.MessageLiteral.Message, 3, Buffer.buffer("World"));
    checker.destroy();
    assertTrue(checker.isEmpty());
  }

  @Test
  public void testUnknownFixed32() throws Exception {
    byte[] bytes = UnknownProto.Message.newBuilder()
      .setUnknownFields(UnknownFieldSet.newBuilder().addField(2, UnknownFieldSet.Field.newBuilder().addFixed32(15).build()).build())
      .build().toByteArray();
    RecordingVisitor visitor = new RecordingVisitor();
    ProtobufReader.parse(SchemaLiterals.MessageLiteral.Message, visitor, visitor, Buffer.buffer(bytes));
    RecordingVisitor.Checker checker = visitor.checker();
    checker.init(SchemaLiterals.MessageLiteral.Message);
    checker.visitUnknownI32(SchemaLiterals.MessageLiteral.Message, 2, 15);
    checker.destroy();
    assertTrue(checker.isEmpty());
  }

  @Test
  public void testUnknownFixed64() throws Exception {
    byte[] bytes = UnknownProto.Message.newBuilder()
      .setUnknownFields(UnknownFieldSet.newBuilder().addField(2, UnknownFieldSet.Field.newBuilder().addFixed64(15L).build()).build())
      .build().toByteArray();
    RecordingVisitor visitor = new RecordingVisitor();
    ProtobufReader.parse(SchemaLiterals.MessageLiteral.Message, visitor, visitor, Buffer.buffer(bytes));
    RecordingVisitor.Checker checker = visitor.checker();
    checker.init(SchemaLiterals.MessageLiteral.Message);
    checker.visitUnknownI64(SchemaLiterals.MessageLiteral.Message, 2, 15L);
    checker.destroy();
    assertTrue(checker.isEmpty());
  }

  @Test
  public void testUnknownVarInt() throws Exception {
    byte[] bytes = UnknownProto.Message.newBuilder()
      .setUnknownFields(UnknownFieldSet.newBuilder().addField(2, UnknownFieldSet.Field.newBuilder().addVarint(15L).build()).build())
      .build().toByteArray();
    RecordingVisitor visitor = new RecordingVisitor();
    ProtobufReader.parse(SchemaLiterals.MessageLiteral.Message, visitor, visitor, Buffer.buffer(bytes));
    RecordingVisitor.Checker checker = visitor.checker();
    checker.init(SchemaLiterals.MessageLiteral.Message);
    checker.visitUnknownVarInt(SchemaLiterals.MessageLiteral.Message, 2, 15L);
    checker.destroy();
    assertTrue(checker.isEmpty());
  }
}
