package io.vertx.tests.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.protobuf.RecordVisitor;
import io.vertx.protobuf.interop.ProtoWriter;
import io.vertx.tests.interop.Container;
import io.vertx.tests.interop.InteropProto;
import io.vertx.tests.interop.MessageLiteral;
import io.vertx.tests.interop.ProtoReader;
import junit.framework.AssertionFailedError;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class InteropTest extends InteropTestBase {

  @Override
  protected Container read(InteropProto.Container src) {
    byte[] bytes = src.toByteArray();
    io.vertx.tests.interop.ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(MessageLiteral.Container, reader, Buffer.buffer(bytes));
    return (Container) reader.stack.pop();
  }

  protected InteropProto.Container write(Container src) {
    Buffer bytes = ProtobufWriter.encode(v -> io.vertx.tests.interop.ProtoWriter.emit(src, v));
    try {
      return InteropProto.Container.parseFrom(bytes.getBytes());
    } catch (InvalidProtocolBufferException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
  }

  @Test
  public void testDecodeStruct() throws Exception {
    testDecodeStruct(new JsonObject().put("string-1", "the-string-1").put("string-2", "the-string-2"));
    testDecodeStruct(new JsonObject().put("number-1", 0).put("number-2", 4321));
    testDecodeStruct(new JsonObject().put("object", new JsonObject().put("string", "the-string")));
    testDecodeStruct(new JsonObject().put("object", new JsonArray().add(1)));
    testDecodeStruct(new JsonObject().put("null-1", null).put("null-2", null));
    testDecodeStruct(new JsonObject().put("true", true).put("false", false));
    testDecodeStruct(new JsonObject().put("object", new JsonArray().add(new JsonObject().put("string", "the-string")).add(4)));
    testDecodeStruct(new JsonObject().put("object", new JsonArray().add(new JsonObject().put("string", "the-string")).add(4)));
  }

  private void testDecodeStruct(Object value) throws Exception {
    String s = Json.encode(value);
    Struct.Builder builder = Struct.newBuilder();
    JsonFormat.parser().merge(s, builder);
    byte[] protobuf = builder.build().toByteArray();
    Buffer buffer = Buffer.buffer(protobuf);
    JsonObject json = io.vertx.protobuf.json.Json.parseStruct(buffer);
    assertEquals(value, json);
  }

  @Test
  public void testEncodeStruct() throws Exception {
    testEncodeStruct(new JsonObject().put("string-1", "the-string-1").put("string-2", "the-string-2"));
    testEncodeStruct(new JsonObject().put("number-1", 0).put("number-2", 4321));
    testEncodeStruct(new JsonObject().put("object", new JsonObject().put("string", "the-string")));
    testEncodeStruct(new JsonObject().put("object", new JsonArray().add(1)));
    testEncodeStruct(new JsonObject().put("null-1", null).put("null-2", null));
    testEncodeStruct(new JsonObject().put("true", true).put("false", false));
    testEncodeStruct(new JsonObject().put("object", new JsonArray().add(new JsonObject().put("string", "the-string")).add(4)));
    testEncodeStruct(new JsonObject()
      .put("the-string", "the-string-value")
      .put("the-number", 4)
      .put("the-boolean", true)
      .put("the-null", null)
      .put("the-object", new JsonObject()
        .put("the-string", "the-string-value")
        .put("the-number", 4)
        .put("the-boolean", true)
        .put("the-null", null)));
  }

  private void testEncodeStruct(JsonObject json) throws Exception {
    Buffer buffer = io.vertx.protobuf.json.Json.encodeToBuffer(json);
    String S1 = new BigInteger(1, buffer.getBytes()).toString(16);
    Struct.Builder builder = Struct.newBuilder();
    JsonFormat.parser().merge(json.encode(), builder);
    byte[] real = builder.build().toByteArray();
    String S2 = new BigInteger(1, real).toString(16);
    assertEquals(S2, S1);
  }

  @Test
  public void testDecodeDuration() throws Exception {
    testDecodeDuration(1, 1);
    testDecodeDuration(0, 0);
    testDecodeDuration(1, 0);
    testDecodeDuration(0, 1);
  }

  private void testDecodeDuration(long seconds, int nano) throws Exception {
    byte[] bytes = com.google.protobuf.Duration
      .newBuilder()
      .setSeconds(seconds).setNanos(nano).build()
      .toByteArray();

    Buffer buffer = Buffer.buffer(bytes);
    io.vertx.protobuf.interop.ProtoReader builder = new io.vertx.protobuf.interop.ProtoReader();
    ProtobufReader.parse(io.vertx.protobuf.well_known_types.MessageLiteral.Duration, builder, buffer);
    io.vertx.protobuf.well_known_types.Duration duration = (io.vertx.protobuf.well_known_types.Duration) builder.pop();
    assertEquals(seconds, (long)duration.getSeconds());
    assertEquals(nano, (int)duration.getNanos());
  }

  @Test
  public void testEncodeDuration() throws Exception {
    testEncodeDuration(1, 1);
    testEncodeDuration(0, 0);
    testEncodeDuration(1, 0);
    testEncodeDuration(0, 1);
  }

  private void testEncodeDuration(long seconds, int nano) throws Exception {
    Consumer<RecordVisitor> consumer = visitor -> {
      ProtoWriter.emit(Duration.ofSeconds(seconds, nano), visitor);
    };
    Buffer buffer = ProtobufWriter.encode(consumer);
    com.google.protobuf.Duration duration = com.google.protobuf.Duration.parseFrom(buffer.getBytes());
    assertEquals(seconds, duration.getSeconds());
    assertEquals(nano, duration.getNanos());
  }
}
