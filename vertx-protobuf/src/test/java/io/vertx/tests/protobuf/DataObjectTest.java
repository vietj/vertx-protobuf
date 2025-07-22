package io.vertx.tests.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.tests.protobuf.struct.StructProto;
import io.vertx.tests.protobuf.struct.ProtoReader;
import io.vertx.tests.protobuf.struct.SchemaLiterals;
import io.vertx.tests.protobuf.struct.Value;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
    byte[] bytes = StructProto.Value.newBuilder().setStructValue(StructProto.Struct.newBuilder().putFields("foo", StructProto.Value.newBuilder().setStringValue("bar").build()).build()).build().toByteArray();
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(SchemaLiterals.VALUE, reader, Buffer.buffer(bytes));
    Value msg = (Value) reader.stack.pop();
//    assertEquals("hello", msg.getStringValue());
  }
}
