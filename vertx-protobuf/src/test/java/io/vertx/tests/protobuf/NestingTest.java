package io.vertx.tests.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.tests.nesting.Container;
import io.vertx.tests.nesting.NestingProto;
import io.vertx.tests.nesting.ProtoWriter;
import io.vertx.tests.nesting.SchemaLiterals;
import io.vertx.tests.nesting.ProtoReader;
import org.junit.Test;

import static org.junit.Assert.*;

public class NestingTest {

  @Test
  public void testnestingMessage() throws Exception {
    byte[] bytes = NestingProto.Container.newBuilder()
      .setNestedMessage(NestingProto.Container.NestedMessage1.newBuilder()
        .setNestedMessage(NestingProto.Container.NestedMessage1.NestedMessage2.newBuilder()
          .setValue("the-string")))
      .build().toByteArray();
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(SchemaLiterals.CONTAINER, reader, Buffer.buffer(bytes));
    Container msg = (Container) reader.stack.pop();
    assertEquals("the-string", msg.getNestedMessage().getNestedMessage().getValue());
    bytes = ProtobufWriter.encodeToByteArray(visitor -> ProtoWriter.emit(msg, visitor));
    NestingProto.Container container = NestingProto.Container.parseFrom(bytes);
    assertEquals("the-string", container.getNestedMessage().getNestedMessage().getValue());
  }
}
