package io.vertx.tests.protobuf;

import com.google.protobuf.ByteString;
import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.protobuf.com.google.protobuf.SchemaLiterals;
import io.vertx.protobuf.com.google.protobuf.NullValue;
import io.vertx.protobuf.com.google.protobuf.Struct;
import io.vertx.protobuf.com.google.protobuf.Value;
import io.vertx.tests.importing.Container;
import io.vertx.tests.importing.ImportingProto;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DataObjectTest {

  @Test
  public void testReadSimple() {
    byte[] bytes = TestProto.SimpleMessage.newBuilder()
      .setStringField("hello")
      .setBytesField(ByteString.copyFromUtf8("hello"))
      .setInt32Field(5)
      .addAllStringListField(Arrays.asList("s-1", "s-2"))
      .putMapStringString("the-key-1", "the-value-1")
      .putMapStringString("the-key-2", "the-value-2")
      .putMapStringInt32("the-key-1", 4)
      .putMapStringInt32("the-key-2", 5)
      .build().toByteArray();
    io.vertx.tests.protobuf.ProtoReader reader = new io.vertx.tests.protobuf.ProtoReader();
    ProtobufReader.parse(io.vertx.tests.protobuf.SchemaLiterals.SIMPLEMESSAGE, reader, Buffer.buffer(bytes));
    SimpleMessage msg = (SimpleMessage) reader.stack.pop();
    assertEquals("hello", msg.getStringField());
    assertEquals("hello", msg.getBytesField().toString());
    assertEquals(5, (int)msg.getInt32Field());
    assertEquals(Arrays.asList("s-1", "s-2"), msg.getStringListField());
    assertEquals(Map.of("the-key-1", "the-value-1", "the-key-2", "the-value-2"), msg.getMapStringString());
    assertEquals(Map.of("the-key-1", 4, "the-key-2", 5), msg.getMapStringInt32());
  }

  @Test
  public void testWriteSimple() throws Exception {
    SimpleMessage value = new SimpleMessage()
      .setStringField("the-string")
      .setBytesField(Buffer.buffer("the-bytes"))
      .setInt32Field(5)
      .setStringListField(Arrays.asList("s-1", "s-2"))
      .setMapStringString(Map.of("the-key-1", "the-value-1", "the-key-2", "the-value-2"))
      .setMapStringInt32(Map.of("the-key", 4));
    Buffer result = ProtobufWriter.encode(visitor -> {
      io.vertx.tests.protobuf.ProtoWriter.emit(value, visitor);
    });
    TestProto.SimpleMessage res = TestProto.SimpleMessage.parseFrom(result.getBytes());
    assertEquals("the-string", res.getStringField());
    assertEquals("the-bytes", res.getBytesField().toStringUtf8());
    assertEquals(5, res.getInt32Field());
    assertEquals(Arrays.asList("s-1", "s-2"), res.getStringListFieldList());
    assertEquals(Map.of("the-key-1", "the-value-1", "the-key-2", "the-value-2"), res.getMapStringStringMap());
    assertEquals(Map.of("the-key", 4), res.getMapStringInt32Map());
  }

/*
  @Test
  public void testStringValue() {
    byte[] bytes = com.google.protobuf.Value.newBuilder().setStringValue("hello").build().toByteArray();
    io.vertx.protobuf.com.google.protobuf.ProtoReader reader = new io.vertx.protobuf.com.google.protobuf.ProtoReader();
    ProtobufReader.parse(SchemaLiterals.VALUE, reader, Buffer.buffer(bytes));
    Value msg = (Value) reader.stack.pop();
    assertEquals("hello", msg.getStringValue());
  }

  @Test
  public void testReadStructValue() {
    byte[] bytes = com.google.protobuf.Value.newBuilder().setStructValue(com.google.protobuf.Struct.newBuilder()
      .putFields("foo", com.google.protobuf.Value.newBuilder().setStringValue("string").build())
      .putFields("bar", com.google.protobuf.Value.newBuilder().setBoolValue(true).build())
      .putFields("juu", com.google.protobuf.Value.newBuilder().setNumberValue(5.1).build())
      .putFields("daa", com.google.protobuf.Value.newBuilder().setNullValue(com.google.protobuf.NullValue.NULL_VALUE).build())
      .putFields("bii", com.google.protobuf.Value.newBuilder().setStructValue(com.google.protobuf.Struct.newBuilder()).build())
      .build()).build().toByteArray();
    io.vertx.protobuf.com.google.protobuf.ProtoReader reader = new io.vertx.protobuf.com.google.protobuf.ProtoReader();
    Buffer buffer = Buffer.buffer(bytes);
    ProtobufReader.parse(SchemaLiterals.VALUE, reader, buffer);
    Value msg = (Value) reader.stack.pop();
    assertNotNull(msg.getStructValue());
    assertEquals("string", msg.getStructValue().getFields().get("foo").getStringValue());
    assertEquals(true, msg.getStructValue().getFields().get("bar").getBoolValue());
    assertEquals(5.1, msg.getStructValue().getFields().get("juu").getNumberValue(), 0.001);
    assertEquals(NullValue.NULL_VALUE, msg.getStructValue().getFields().get("daa").getNullValue());
    assertNotNull(msg.getStructValue().getFields().get("bii").getStructValue());
  }

  @Test
  public void testWriteStructValue() throws Exception {
    Struct struct = new Struct();
    struct.setFields(new HashMap<>());
    struct.getFields().put("foo", new Value().setStringValue("string"));
    struct.getFields().put("bar", new Value().setBoolValue(true));
    struct.getFields().put("juu", new Value().setNumberValue(5.1));
    struct.getFields().put("daa", new Value().setNullValue(NullValue.NULL_VALUE));
    Buffer result = ProtobufWriter.encode(visitor -> {
      io.vertx.protobuf.com.google.protobuf.ProtoWriter.emit(struct, visitor);
    });
    com.google.protobuf.Struct res = com.google.protobuf.Struct.parseFrom(result.getBytes());
    assertEquals("string", res.getFieldsMap().get("foo").getStringValue());
    assertTrue(res.getFieldsMap().get("bar").getBoolValue());
    assertEquals(5.1, res.getFieldsMap().get("juu").getNumberValue(), 0.001);
    assertTrue(res.getFieldsMap().get("daa").hasNullValue());
  }
*/

  @Test
  public void testReadImports() {
    byte[] bytes = ImportingProto.Container.newBuilder().setSimpleMessage(TestProto.SimpleMessage.newBuilder().setStringField("the-string").build()).build().toByteArray();
    io.vertx.tests.importing.ProtoReader reader = new io.vertx.tests.importing.ProtoReader();
    Buffer buffer = Buffer.buffer(bytes);
    ProtobufReader.parse(io.vertx.tests.importing.SchemaLiterals.CONTAINER, reader, buffer);
    Container msg = (Container) reader.stack.pop();
    assertEquals("the-string", msg.getSimpleMessage().getStringField());
  }

  @Test
  public void testWriteImports() throws Exception {
    Container container = new Container();
    container.setSimpleMessage(new SimpleMessage().setStringField("the-string"));
    Buffer result = ProtobufWriter.encode(visitor -> {
      io.vertx.tests.importing.ProtoWriter.emit(container, visitor);
    });
    ImportingProto.Container res = ImportingProto.Container.parseFrom(result.getBytes());
    assertEquals("the-string", res.getSimpleMessage().getStringField());
  }
}
