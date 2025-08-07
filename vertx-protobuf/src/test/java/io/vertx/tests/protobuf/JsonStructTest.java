package io.vertx.tests.protobuf;

import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class JsonStructTest {

  @Test
  public void testDecode() throws Exception {
    testDecodeJson(new JsonObject().put("string-1", "the-string-1").put("string-2", "the-string-2"));
    testDecodeJson(new JsonObject().put("number-1", 0).put("number-2", 4321));
    testDecodeJson(new JsonObject().put("object", new JsonObject().put("string", "the-string")));
    testDecodeJson(new JsonObject().put("object", new JsonArray().add(1)));
    testDecodeJson(new JsonObject().put("null-1", null).put("null-2", null));
    testDecodeJson(new JsonObject().put("true", true).put("false", false));
    testDecodeJson(new JsonObject().put("object", new JsonArray().add(new JsonObject().put("string", "the-string")).add(4)));
  }

  private void testDecodeJson(Object value) throws Exception {
    String s = Json.encode(value);
    Struct.Builder builder = Struct.newBuilder();
    JsonFormat.parser().merge(s, builder);
    byte[] protobuf = builder.build().toByteArray();
    Buffer buffer = Buffer.buffer(protobuf);
    JsonObject json = io.vertx.protobuf.json.Json.parseStruct(buffer);
    assertEquals(value, json);
  }

  @Test
  public void testEncode() throws Exception {
    testEncodeJson(new JsonObject().put("string-1", "the-string-1").put("string-2", "the-string-2"));
    testEncodeJson(new JsonObject().put("number-1", 0).put("number-2", 4321));
    testEncodeJson(new JsonObject().put("object", new JsonObject().put("string", "the-string")));
    testEncodeJson(new JsonObject().put("object", new JsonArray().add(1)));
    testEncodeJson(new JsonObject().put("null-1", null).put("null-2", null));
    testEncodeJson(new JsonObject().put("true", true).put("false", false));
    testEncodeJson(new JsonObject().put("object", new JsonArray().add(new JsonObject().put("string", "the-string")).add(4)));
    testEncodeJson(new JsonObject()
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

  private void testEncodeJson(JsonObject json) throws Exception {
    Buffer buffer = io.vertx.protobuf.json.Json.encodeToBuffer(json);
    String S1 = new BigInteger(1, buffer.getBytes()).toString(16);
    Struct.Builder builder = Struct.newBuilder();
    JsonFormat.parser().merge(json.encode(), builder);
    byte[] real = builder.build().toByteArray();
    String S2 = new BigInteger(1, real).toString(16);
    assertEquals(S2, S1);
  }
}
