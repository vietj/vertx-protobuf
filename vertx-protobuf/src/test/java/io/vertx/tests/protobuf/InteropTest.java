package io.vertx.tests.protobuf;

import com.google.protobuf.Struct;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.tests.interop.InteropProto;
import io.vertx.tests.interop.Container;
import io.vertx.tests.interop.MessageLiteral;
import io.vertx.tests.interop.ProtoReader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InteropTest {

  @Test
  public void testReadStruct() {
    byte[] bytes = InteropProto.Container.newBuilder()
      .setStruct(Struct.newBuilder().putFields("the-string", com.google.protobuf.Value.newBuilder().setStringValue("the-string-value").build()).build())
      .build().toByteArray();
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(MessageLiteral.Container, reader, Buffer.buffer(bytes));
    Container msg = (Container) reader.stack.pop();
    JsonObject s = msg.getStruct();
    assertEquals("the-string-value", s.getString("the-string"));
  }
}
