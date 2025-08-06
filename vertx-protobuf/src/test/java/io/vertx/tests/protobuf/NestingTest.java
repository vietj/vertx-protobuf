package io.vertx.tests.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.tests.nesting.Container;
import io.vertx.tests.nesting.NestingProto;
import io.vertx.tests.nesting.ProtoWriter;
import io.vertx.tests.nesting.Repeated;
import io.vertx.tests.nesting.MessageLiteral;
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
    ProtobufReader.parse(MessageLiteral.Container, reader, Buffer.buffer(bytes));
    Container msg = (Container) reader.stack.pop();
    assertEquals("the-string", msg.getNestedMessage().getNestedMessage().getValue());
    assertEquals(Container.NestedEnum1.constant_1, msg.getNestedEnum());
    assertEquals(Container.NestedEnum1.constant_1, msg.getNestedMessage().getNestedEnum1());
    assertEquals(Container.NestedMessage1.NestedEnum2.constant_2, msg.getNestedMessage().getNestedEnum2());
    bytes = ProtobufWriter.encodeToByteArray(visitor -> ProtoWriter.emit(msg, visitor));
    NestingProto.Container container = NestingProto.Container.parseFrom(bytes);
    assertEquals("the-string", container.getNestedMessage().getNestedMessage().getValue());
  }

  @Test
  public void testRepetition() {
    byte[] bytes = NestingProto.Repeated.newBuilder()
      .addNestedMessages(NestingProto.Repeated.NestedMessage.getDefaultInstance())
      .addNestedMessages(NestingProto.Repeated.NestedMessage.newBuilder().setVal(3).build())
      .addNestedMessages(NestingProto.Repeated.NestedMessage.newBuilder().setVal(5).build())
      .build()
      .toByteArray();
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(MessageLiteral.Repeated, reader, Buffer.buffer(bytes));
    Repeated msg = (Repeated) reader.stack.pop();
    assertEquals(3, msg.getNestedMessages().size());
    assertEquals(0, (int)msg.getNestedMessages().get(0).getVal());
    assertEquals(3, (int)msg.getNestedMessages().get(1).getVal());
    assertEquals(5, (int)msg.getNestedMessages().get(2).getVal());
  }
}
