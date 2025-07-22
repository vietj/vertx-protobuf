package io.vertx.tests.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DataObjectTest {

  @Test
  public void testSome() {
    byte[] bytes = TestProto.SimpleMessage.newBuilder().setStringField("hello").build().toByteArray();
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(SchemaLiterals.SIMPLEMESSAGE, reader, Buffer.buffer(bytes));
    SimpleMessage msg = (SimpleMessage) reader.stack.pop();
    assertEquals("hello", msg.getStringField());
  }
}
