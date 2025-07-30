package io.vertx.tests.protobuf;

import com.google.protobuf.ByteString;
import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.tests.presence.Default;
import io.vertx.tests.presence.Optional;
import io.vertx.tests.presence.PresenceProto;
import io.vertx.tests.presence.ProtoWriter;
import io.vertx.tests.presence.Enum;
import io.vertx.tests.presence.Repeated;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class PresenceTest {

  @Test
  public void testDefaultPresence() {
    Default msg = new Default();
    msg.setString("");
    msg.setBytes(Buffer.buffer());
    msg.setFloat(0f);
    msg.setDouble(0D);
    msg.setInt32(0);
    msg.setInt64(0L);
    msg.setUint32(0);
    msg.setUint64(0L);
    msg.setSint32(0);
    msg.setSint64(0L);
    msg.setFixed32(0);
    msg.setFixed64(0L);
    msg.setSfixed32(0);
    msg.setSfixed64(0L);
    msg.setEnum(Enum.constant_0);
    byte[] bytes = ProtobufWriter.encodeToByteArray(visitor -> ProtoWriter.emit(msg, visitor));
    PresenceProto.Default.Builder builder = PresenceProto.Default.newBuilder();
    byte[] expected = builder
      .build()
      .toByteArray();
    assertArrayEquals(bytes, expected);
  }

  @Test
  public void testOptionalPresence() {
    Optional msg = new Optional();
    msg.setString("");
    msg.setBytes(Buffer.buffer());
    msg.setFloat(0f);
    msg.setDouble(0D);
    msg.setInt32(0);
    msg.setInt64(0L);
    msg.setUint32(0);
    msg.setUint64(0L);
    msg.setSint32(0);
    msg.setSint64(0L);
    msg.setFixed32(0);
    msg.setFixed64(0L);
    msg.setSfixed32(0);
    msg.setSfixed64(0L);
    msg.setEnum(Enum.constant_1);
    byte[] bytes = ProtobufWriter.encodeToByteArray(visitor -> ProtoWriter.emit(msg, visitor));
    PresenceProto.Optional.Builder builder = PresenceProto.Optional.newBuilder()
      .setString("")
      .setBytes(ByteString.EMPTY)
      .setFloat(0f)
      .setDouble(0D)
      .setInt32(0)
      .setInt64(0L)
      .setUint32(0)
      .setUint64(0)
      .setSint32(0)
      .setSint64(0L)
      .setFixed32(0)
      .setFixed64(0L)
      .setSfixed32(0)
      .setSfixed64(0L)
      .setEnum(PresenceProto.Enum.constant_1);
    byte[] expected = builder
      .build()
      .toByteArray();
    assertEquals(expected.length, bytes.length);
  }

  @Test
  public void testRepeatedPresence() {
    Repeated msg = new Repeated();
    msg.setString(Collections.emptyList());
    msg.setBytes(Collections.emptyList());
    msg.setFloat(Collections.emptyList());
    msg.setDouble(Collections.emptyList());
    msg.setInt32(Collections.emptyList());
    msg.setInt64(Collections.emptyList());
    msg.setUint32(Collections.emptyList());
    msg.setUint64(Collections.emptyList());
    msg.setSint32(Collections.emptyList());
    msg.setSint64(Collections.emptyList());
    msg.setFixed32(Collections.emptyList());
    msg.setFixed64(Collections.emptyList());
    msg.setSfixed32(Collections.emptyList());
    msg.setSfixed64(Collections.emptyList());
    msg.setEnum(Collections.emptyList());
    byte[] bytes = ProtobufWriter.encodeToByteArray(visitor -> ProtoWriter.emit(msg, visitor));
    PresenceProto.Repeated.Builder builder = PresenceProto.Repeated.newBuilder();
    byte[] expected = builder
      .build()
      .toByteArray();
    assertEquals(expected.length, bytes.length);
  }
}
