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
    msg.setInt32(0);
    msg.setInt64(0L);
    msg.setUint32(0);
    msg.setUint64(0L);
    msg.setSint32(0);
    msg.setSint64(0L);
    msg.setBool(false);
    msg.setEnum(Enum.constant_0);
    msg.setFixed64(0L);
    msg.setSfixed64(0L);
    msg.setFloat(0f);
    msg.setFixed32(0);
    msg.setSfixed32(0);
    msg.setDouble(0D);
    byte[] bytes = ProtobufWriter.encodeToByteArray(visitor -> ProtoWriter.emit(msg, visitor));
    PresenceProto.Default.Builder builder = PresenceProto.Default.newBuilder();
    byte[] expected = builder
      .build()
      .toByteArray();
    assertEquals(expected.length, bytes.length);
  }

  @Test
  public void testOptionalPresence() {
    Optional msg = new Optional();
    msg.setString("");
    msg.setBytes(Buffer.buffer());
    msg.setInt32(0);
    msg.setInt64(0L);
    msg.setUint32(0);
    msg.setUint64(0L);
    msg.setSint32(0);
    msg.setSint64(0L);
    msg.setBool(false);
    msg.setEnum(Enum.constant_1);
    msg.setFixed64(0L);
    msg.setSfixed64(0L);
    msg.setDouble(0D);
    msg.setFixed32(0);
    msg.setSfixed32(0);
    msg.setFloat(0f);
    byte[] bytes = ProtobufWriter.encodeToByteArray(visitor -> ProtoWriter.emit(msg, visitor));
    PresenceProto.Optional.Builder builder = PresenceProto.Optional.newBuilder()
      .setString("")
      .setBytes(ByteString.EMPTY)
      .setInt32(0)
      .setInt64(0L)
      .setUint32(0)
      .setUint64(0)
      .setSint32(0)
      .setSint64(0L)
      .setBool(false)
      .setEnum(PresenceProto.Enum.constant_1)
      .setFixed64(0L)
      .setSfixed64(0L)
      .setDouble(0D)
      .setFixed32(0)
      .setSfixed32(0)
      .setFloat(0f)
      ;
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
    msg.setInt32(Collections.emptyList());
    msg.setInt64(Collections.emptyList());
    msg.setUint32(Collections.emptyList());
    msg.setUint64(Collections.emptyList());
    msg.setSint32(Collections.emptyList());
    msg.setSint64(Collections.emptyList());
    msg.setBool(Collections.emptyList());
    msg.setEnum(Collections.emptyList());
    msg.setFixed64(Collections.emptyList());
    msg.setSfixed64(Collections.emptyList());
    msg.setDouble(Collections.emptyList());
    msg.setFixed32(Collections.emptyList());
    msg.setSfixed32(Collections.emptyList());
    msg.setFloat(Collections.emptyList());
    byte[] bytes = ProtobufWriter.encodeToByteArray(visitor -> ProtoWriter.emit(msg, visitor));
    PresenceProto.Repeated.Builder builder = PresenceProto.Repeated.newBuilder();
    byte[] expected = builder
      .build()
      .toByteArray();
    assertEquals(expected.length, bytes.length);
  }
}
