package io.vertx.tests.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.tests.importing.Container;
import io.vertx.tests.importing.ImportingProto;
import io.vertx.tests.protobuf.struct.StructProto;
import io.vertx.tests.protobuf.struct.ProtoReader;
import io.vertx.tests.protobuf.struct.SchemaLiterals;
import io.vertx.tests.protobuf.struct.Value;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DataObjectTest {

/*
  @Test
  public void testSimple() {
    byte[] bytes = TestProto.SimpleMessage.newBuilder().setStringField("hello").build().toByteArray();
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(SchemaLiterals.SIMPLEMESSAGE, reader, Buffer.buffer(bytes));
    SimpleMessage msg = (SimpleMessage) reader.stack.pop();
    assertEquals("hello", msg.getStringField());
  }
*/

  @Test
  public void testStringValue() {
    byte[] bytes = StructProto.Value.newBuilder().setStringValue("hello").build().toByteArray();
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(SchemaLiterals.VALUE, reader, Buffer.buffer(bytes));
    Value msg = (Value) reader.stack.pop();
    assertEquals("hello", msg.getStringValue());
  }

  @Test
  public void testStructValue() {
    byte[] bytes = StructProto.Value.newBuilder().setStructValue(StructProto.Struct.newBuilder()
      .putFields("foo", StructProto.Value.newBuilder().setStringValue("bar").build())
      .putFields("juu", StructProto.Value.newBuilder().setStringValue("daa").build())
      .build()).build().toByteArray();
    ProtoReader reader = new ProtoReader();
    Buffer buffer = Buffer.buffer(bytes);
    ProtobufReader.parse(SchemaLiterals.VALUE, reader, buffer);
    Value msg = (Value) reader.stack.pop();
    assertNotNull(msg.getStructValue());
//    assertEquals("bar", msg.getStructValue().getFields().get("foo"));
//    assertEquals("daa", msg.getStructValue().getFields().get("juu"));
  }

  @Test
  public void testImports() {
    byte[] bytes = ImportingProto.Container.newBuilder().setSimpleMessage(TestProto.SimpleMessage.newBuilder().setStringField("the-string").build()).build().toByteArray();
    io.vertx.tests.importing.ProtoReader reader = new io.vertx.tests.importing.ProtoReader();
    Buffer buffer = Buffer.buffer(bytes);
    ProtobufReader.parse(io.vertx.tests.importing.SchemaLiterals.CONTAINER, reader, buffer);
    Container msg = (Container) reader.stack.pop();
    assertEquals("the-string", msg.getSimpleMessage().getStringField());
  }
}
