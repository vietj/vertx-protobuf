package io.vertx.tests.protobuf.json;

import com.google.protobuf.util.JsonFormat;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.json.ProtoJsonReader;
import io.vertx.protobuf.json.ProtoJsonWriter;
import io.vertx.tests.protobuf.MessageLiteral;
import io.vertx.tests.protobuf.ProtoReader;
import io.vertx.tests.protobuf.ProtoWriter;
import io.vertx.tests.protobuf.SimpleMessage;
import io.vertx.tests.protobuf.TestProto;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class SimpleTest {

  @Test
  public void testSimple() throws Exception {

    String json = JsonFormat.printer().print(TestProto.SimpleMessage.newBuilder()
      .setStringField("the-string")
      .setInt32Field(4)
      .addStringListField("s1")
      .addStringListField("s2")
      .build());

    ProtoReader pr = new ProtoReader();

    ProtoJsonReader.parse(json, MessageLiteral.SimpleMessage, pr);

    SimpleMessage pop = (SimpleMessage) pr.stack.pop();

    assertEquals("the-string", pop.getStringField());
    assertEquals(4, (int)pop.getInt32Field());
    assertEquals(Arrays.asList("s1", "s2"), pop.getStringListField());

    JsonObject actual = ProtoJsonWriter.encode(v -> ProtoWriter.emit(pop, v));

    assertEquals(new JsonObject(json), actual);
  }

  @Test
  public void testTransmute() {
    byte[] bytes = TestProto.SimpleMessage.newBuilder()
      .setStringField("the-string")
      .setInt32Field(4)
      .addStringListField("s1")
      .addStringListField("s2")
      .build()
      .toByteArray();
    ProtoJsonWriter writer = new ProtoJsonWriter();
    ProtobufReader.parse(MessageLiteral.SimpleMessage, writer, Buffer.buffer(bytes));
    JsonObject json = (JsonObject) writer.stack.pop();
    System.out.println("json = " + json);
    JsonObject expected = new JsonObject()
      .put("stringField", "the-string")
      .put("int32Field", 4)
      .put("stringListField", new JsonArray().add("s1").add("s2"));
    assertEquals(expected, json);
  }
}
