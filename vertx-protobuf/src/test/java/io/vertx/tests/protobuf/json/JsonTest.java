package io.vertx.tests.protobuf.json;

import com.google.protobuf.ListValue;
import com.google.protobuf.NullValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.protobuf.json.JsonReader;
import io.vertx.protobuf.json.JsonWriter;
import io.vertx.tests.json.Container;
import io.vertx.tests.json.JsonProto;
import io.vertx.tests.json.MessageLiteral;
import io.vertx.tests.json.ProtoReader;
import io.vertx.tests.json.ProtoWriter;
import org.junit.Test;

import static org.junit.Assert.*;

public class JsonTest {

  @Test
  public void testReadStruct() throws Exception {
    String json = JsonFormat.printer().print(JsonProto.Container.newBuilder()
      .setStruct(Struct.newBuilder()
        .putFields("string-key", Value.newBuilder().setStringValue("string-value").build())
        .putFields("null-key", Value.newBuilder().setNullValue(NullValue.NULL_VALUE).build())
        .putFields("number-key", Value.newBuilder().setNumberValue(3.14).build())
        .putFields("true-key", Value.newBuilder().setBoolValue(true).build())
        .putFields("false-key", Value.newBuilder().setBoolValue(false).build())
        .putFields("array-key", Value.newBuilder().setListValue(ListValue.newBuilder().addValues(Value.newBuilder().setStringValue("the-string").build()).build()).build())
        .putFields("object-key", Value.newBuilder().setStructValue(Struct.newBuilder().putFields("the-key", Value.newBuilder().setStringValue("the-value").build()).build()).build())
        .build())
      .build());
    ProtoReader pr = new ProtoReader();
    JsonReader.parse(json, MessageLiteral.Container, pr);
    Container pop = (Container) pr.stack.pop();
    assertEquals("string-value", pop.getStruct().getString("string-key"));
    assertNull(pop.getStruct().getJsonObject("null-key"));
    assertTrue(pop.getStruct().containsKey("null-key"));
    assertEquals(3.14, pop.getStruct().getDouble("number-key"), 0.001);
    assertTrue(pop.getStruct().getBoolean("true-key"));
    assertFalse(pop.getStruct().getBoolean("false-key"));
    assertEquals(new JsonArray().add("the-string"), pop.getStruct().getJsonArray("array-key"));
    assertEquals(new JsonObject().put("the-key", "the-value"), pop.getStruct().getJsonObject("object-key"));
  }

  @Test
  public void testWriteStruct() throws Exception {
    JsonObject json = new JsonObject()
      .put("string-key", "string-value")
      .put("null-key", null)
      .put("number-key", 3.14)
      .put("true-key", true)
      .put("false-key", false)
      .put("array-key", new JsonArray().add("the-string"))
      .put("object-key", new JsonObject().put("the-key", "the-value"));
    Container container = new Container();
    container.setStruct(json);
    String jsonString = JsonWriter.encode(visitor -> {
      ProtoWriter.emit(container, visitor);
    });
    assertEquals(new JsonObject().put("struct", json), new JsonObject(jsonString));
  }
}
