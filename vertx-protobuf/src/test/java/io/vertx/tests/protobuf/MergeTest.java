package io.vertx.tests.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.tests.merge.MergeProto;
import io.vertx.tests.merge.Container;
import io.vertx.tests.merge.Nested;
import io.vertx.tests.merge.ProtoReader;
import io.vertx.tests.merge.MessageLiteral;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class MergeTest {

  @Test
  public void testMerge() throws Exception {
    byte[] bytes1 = MergeProto.Container.newBuilder()
      .setInt32(1)
      .addRepeatedInt32(1)
      .setNested(MergeProto.Nested.newBuilder().setInt32(3).addRepeatedInt32(3).build())
      .setOneOfNested(MergeProto.Nested.newBuilder().setInt32(3).addRepeatedInt32(3).build())
      .build().toByteArray();
    byte[] bytes2 = MergeProto.Container.newBuilder()
      .setInt64(1L)
      .addRepeatedInt32(2)
      .setNested(MergeProto.Nested.newBuilder().setInt64(4L).addRepeatedInt32(4).build())
      .setOneOfNested(MergeProto.Nested.newBuilder().setInt64(4L).addRepeatedInt32(4).build())
      .build().toByteArray();
    Buffer aggregated = Buffer.buffer();
    aggregated.appendBytes(bytes1);
    aggregated.appendBytes(bytes2);
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(MessageLiteral.Container, reader, aggregated);
    Container msg = (Container) reader.stack.pop();
    assertEquals(1, (int)msg.getInt32());
    assertEquals(1L, (long)msg.getInt64());
    assertEquals(Arrays.asList(1, 2), msg.getRepeatedInt32());
    Nested nested = msg.getNested();
    assertEquals(3, (int)nested.getInt32());
    assertEquals(4L, (long)nested.getInt64());
    assertEquals(Arrays.asList(3, 4), nested.getRepeatedInt32());
    Nested oneOfNested = msg.getOneOf().asOneOfNested().get();
    assertEquals(3, (int)oneOfNested.getInt32());
    assertEquals(4L, (long)oneOfNested.getInt64());
    assertEquals(Arrays.asList(3, 4), oneOfNested.getRepeatedInt32());
  }

  @Test
  public void testOneOfOverride() {
    byte[] bytes1 = MergeProto.Container.newBuilder()
      .setOneOfNested(MergeProto.Nested.newBuilder().setInt32(3).build())
      .build().toByteArray();
    byte[] bytes2 = MergeProto.Container.newBuilder().setOneOfInt32(4).build().toByteArray();
    Buffer aggregated = Buffer.buffer();
    aggregated.appendBytes(bytes1);
    aggregated.appendBytes(bytes2);
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(MessageLiteral.Container, reader, aggregated);
    Container msg = (Container) reader.stack.pop();
    Integer i = msg.getOneOf().asOneOfInt32().get();
    assertEquals(4, (int)i);
  }
}
