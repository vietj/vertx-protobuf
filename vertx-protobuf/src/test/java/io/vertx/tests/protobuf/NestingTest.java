package io.vertx.tests.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.tests.nesting.Container;
import io.vertx.tests.nesting.NestedEnum1;
import io.vertx.tests.nesting.NestedEnum2;
import io.vertx.tests.nesting.NestingProto;
import io.vertx.tests.nesting.ProtoWriter;
import io.vertx.tests.nesting.SchemaLiterals;
import io.vertx.tests.nesting.ProtoReader;
import org.junit.Test;

import static org.junit.Assert.*;

public class NestingTest {

  @Test
  public void testNesting() throws Exception {
    byte[] bytes = NestingProto.Container.newBuilder()
      .setNestedMessage(NestingProto.Container.NestedMessage1.newBuilder()
        .setNestedMessage(NestingProto.Container.NestedMessage1.NestedMessage2.newBuilder()
          .setValue("the-string"))
        .setNestedEnum1(NestingProto.Container.NestedEnum1.constant_1)
        .setNestedEnum2(NestingProto.Container.NestedMessage1.NestedEnum2.constant_2))
      .setNestedEnum(NestingProto.Container.NestedEnum1.constant_1)
      .build().toByteArray();
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(SchemaLiterals.CONTAINER, reader, Buffer.buffer(bytes));
    Container msg = (Container) reader.stack.pop();
    assertEquals("the-string", msg.getNestedMessage().getNestedMessage().getValue());
    assertEquals(NestedEnum1.constant_1, msg.getNestedEnum());
    assertEquals(NestedEnum1.constant_1, msg.getNestedMessage().getNestedEnum1());
    assertEquals(NestedEnum2.constant_2, msg.getNestedMessage().getNestedEnum2());
    bytes = ProtobufWriter.encodeToByteArray(visitor -> ProtoWriter.emit(msg, visitor));
    NestingProto.Container container = NestingProto.Container.parseFrom(bytes);
    assertEquals("the-string", container.getNestedMessage().getNestedMessage().getValue());
  }
}
