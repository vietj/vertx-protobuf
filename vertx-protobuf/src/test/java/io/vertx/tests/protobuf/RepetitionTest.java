package io.vertx.tests.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.tests.repetition.Enum;
import io.vertx.tests.repetition.Packed;
import io.vertx.tests.repetition.ProtoReader;
import io.vertx.tests.repetition.ProtoWriter;
import io.vertx.tests.repetition.Repeated;
import io.vertx.tests.repetition.SchemaLiterals;
import io.vertx.tests.repetition.RepetitionProto;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class RepetitionTest {

  @Test
  public void testRepetition() throws Exception {
    byte[] bytes = RepetitionProto.Repeated.newBuilder()
      .addRepeatedInt(0)
      .addRepeatedInt(1)
      .addRepeatedInt(2)
      .addRepeatedEnum(RepetitionProto.Enum.constant)
      .addRepeatedDouble(0)
      .addRepeatedDouble(1)
      .addRepeatedDouble(2)
      .addRepeatedFixed64(0)
      .addRepeatedFixed64(1)
      .addRepeatedFixed64(2)
      .addRepeatedFloat(0)
      .addRepeatedFloat(1)
      .addRepeatedFloat(2)
      .addRepeatedFixed32(0)
      .addRepeatedFixed32(1)
      .addRepeatedFixed32(2)
      .build().toByteArray();
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(SchemaLiterals.REPEATED, reader, Buffer.buffer(bytes));
    Repeated msg = (Repeated) reader.stack.pop();
    assertEquals(Arrays.asList(0, 1, 2), msg.getRepeatedInt());
    assertEquals(Collections.singletonList(Enum.constant), msg.getRepeatedEnum());
    assertEquals(Arrays.asList(0D, 1D, 2D), msg.getRepeatedDouble());
    assertEquals(Arrays.asList(0L, 1L, 2L), msg.getRepeatedFixed64());
    assertEquals(Arrays.asList(0F, 1F, 2F), msg.getRepeatedFloat());
    assertEquals(Arrays.asList(0, 1, 2), msg.getRepeatedFixed32());
    bytes = ProtobufWriter.encodeToByteArray(visitor -> ProtoWriter.emit(msg, visitor));
    RepetitionProto.Repeated container = RepetitionProto.Repeated.parseFrom(bytes);
    assertEquals(Arrays.asList(0, 1, 2), container.getRepeatedIntList());
    assertEquals(Collections.singletonList(RepetitionProto.Enum.constant), container.getRepeatedEnumList());
    assertEquals(Arrays.asList(0D, 1D, 2D), container.getRepeatedDoubleList());
    assertEquals(Arrays.asList(0L, 1L, 2L), container.getRepeatedFixed64List());
    assertEquals(Arrays.asList(0F, 1F, 2F), container.getRepeatedFloatList());
    assertEquals(Arrays.asList(0, 1, 2), container.getRepeatedFixed32List());
  }

  @Test
  public void testPackedRepetition() throws Exception {
    byte[] bytes = RepetitionProto.Packed.newBuilder()
      .addRepeatedInt(0)
      .addRepeatedInt(1)
      .addRepeatedInt(2)
      .addRepeatedEnum(RepetitionProto.Enum.constant)
      .addRepeatedDouble(0)
      .addRepeatedDouble(1)
      .addRepeatedDouble(2)
      .addRepeatedFixed64(0)
      .addRepeatedFixed64(1)
      .addRepeatedFixed64(2)
      .addRepeatedFloat(0)
      .addRepeatedFloat(1)
      .addRepeatedFloat(2)
      .addRepeatedFixed32(0)
      .addRepeatedFixed32(1)
      .addRepeatedFixed32(2)
      .build().toByteArray();
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(SchemaLiterals.PACKED, reader, Buffer.buffer(bytes));
    Packed msg = (Packed) reader.stack.pop();
    assertEquals(Arrays.asList(0, 1, 2), msg.getRepeatedInt());
    assertEquals(Collections.singletonList(Enum.constant), msg.getRepeatedEnum());
    assertEquals(Arrays.asList(0D, 1D, 2D), msg.getRepeatedDouble());
    assertEquals(Arrays.asList(0L, 1L, 2L), msg.getRepeatedFixed64());
    assertEquals(Arrays.asList(0F, 1F, 2F), msg.getRepeatedFloat());
    assertEquals(Arrays.asList(0, 1, 2), msg.getRepeatedFixed32());
    bytes = ProtobufWriter.encodeToByteArray(visitor -> ProtoWriter.emit(msg, visitor));
    RepetitionProto.Repeated container = RepetitionProto.Repeated.parseFrom(bytes);
    assertEquals(Arrays.asList(0, 1, 2), container.getRepeatedIntList());
    assertEquals(Collections.singletonList(RepetitionProto.Enum.constant), container.getRepeatedEnumList());
    assertEquals(Arrays.asList(0D, 1D, 2D), container.getRepeatedDoubleList());
    assertEquals(Arrays.asList(0L, 1L, 2L), container.getRepeatedFixed64List());
    assertEquals(Arrays.asList(0F, 1F, 2F), container.getRepeatedFloatList());
    assertEquals(Arrays.asList(0, 1, 2), container.getRepeatedFixed32List());
  }
}
