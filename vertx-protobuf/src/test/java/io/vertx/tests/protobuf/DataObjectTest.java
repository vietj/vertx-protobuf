package io.vertx.tests.protobuf;

import com.google.protobuf.ByteString;
import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.tests.importing.Container;
import io.vertx.tests.importing.ImportingProto;
import io.vertx.tests.protobuf.struct.NullValue;
import io.vertx.tests.protobuf.struct.Struct;
import io.vertx.tests.protobuf.struct.StructProto;
import io.vertx.tests.protobuf.struct.ProtoReader;
import io.vertx.tests.protobuf.struct.SchemaLiterals;
import io.vertx.tests.protobuf.struct.Value;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class DataObjectTest {

  @Test
  public void testReadSimple() {
    byte[] bytes = TestProto.SimpleMessage.newBuilder()
      .setStringField("hello")
      .setBytesField(ByteString.copyFromUtf8("hello"))
      .putMapField("the-key", "the-value")
      .build().toByteArray();
    io.vertx.tests.protobuf.ProtoReader reader = new io.vertx.tests.protobuf.ProtoReader();
    ProtobufReader.parse(io.vertx.tests.protobuf.SchemaLiterals.SIMPLEMESSAGE, reader, Buffer.buffer(bytes));
    SimpleMessage msg = (SimpleMessage) reader.stack.pop();
    assertEquals("hello", msg.getStringField());
    assertEquals("hello", msg.getBytesField().toString());
    assertEquals(Map.of("the-key", "the-value"), msg.getMapField());
  }

  @Test
  public void testWriteSimple() throws Exception {
    SimpleMessage value = new SimpleMessage()
      .setStringField("the-string")
      .setBytesField(Buffer.buffer("the-bytes"))
      .setMapField(Map.of("the-key", "the-value"));
    Buffer result = ProtobufWriter.encode(visitor -> {
      io.vertx.tests.protobuf.ProtoWriter.emit(value, visitor);
    });
    TestProto.SimpleMessage res = TestProto.SimpleMessage.parseFrom(result.getBytes());
    assertEquals("the-string", res.getStringField());
    assertEquals("the-bytes", res.getBytesField().toStringUtf8());
    assertEquals(Map.of("the-key", "the-value"), res.getMapFieldMap());
  }

  @Test
  public void testStringValue() {
    byte[] bytes = StructProto.Value.newBuilder().setStringValue("hello").build().toByteArray();
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(SchemaLiterals.VALUE, reader, Buffer.buffer(bytes));
    Value msg = (Value) reader.stack.pop();
    assertEquals("hello", msg.getStringValue());
  }

  @Test
  public void testReadStructValue() {
    byte[] bytes = StructProto.Value.newBuilder().setStructValue(StructProto.Struct.newBuilder()
      .putFields("foo", StructProto.Value.newBuilder().setStringValue("string").build())
      .putFields("bar", StructProto.Value.newBuilder().setBoolValue(true).build())
      .putFields("juu", StructProto.Value.newBuilder().setNumberValue(5.1).build())
      .putFields("daa", StructProto.Value.newBuilder().setNullValue(StructProto.NullValue.NULL_VALUE).build())
      .putFields("bii", StructProto.Value.newBuilder().setStructValue(StructProto.Struct.newBuilder()).build())
      .build()).build().toByteArray();
    ProtoReader reader = new ProtoReader();
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
    struct.getFields().put("foo", new Value().setStringValue("string"));
    struct.getFields().put("bar", new Value().setBoolValue(true));
    struct.getFields().put("juu", new Value().setNumberValue(5.1));
    struct.getFields().put("daa", new Value().setNullValue(NullValue.NULL_VALUE));
    Buffer result = ProtobufWriter.encode(visitor -> {
      io.vertx.tests.protobuf.struct.ProtoWriter.emit(struct, visitor);
    });
    StructProto.Struct res = StructProto.Struct.parseFrom(result.getBytes());
    assertEquals("string", res.getFieldsMap().get("foo").getStringValue());
    assertTrue(res.getFieldsMap().get("bar").getBoolValue());
    assertEquals(5.1, res.getFieldsMap().get("juu").getNumberValue(), 0.001);
    assertTrue(res.getFieldsMap().get("daa").hasNullValue());
  }

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
