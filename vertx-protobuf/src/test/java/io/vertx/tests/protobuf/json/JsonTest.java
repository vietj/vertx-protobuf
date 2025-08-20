package io.vertx.tests.protobuf.json;

import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.Duration;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.ListValue;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.NullValue;
import com.google.protobuf.ProtocolStringList;
import com.google.protobuf.StringValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Timestamp;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.EncodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.protobuf.json.JsonReader;
import io.vertx.protobuf.json.JsonWriter;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.tests.json.Container;
import io.vertx.tests.json.FieldLiteral;
import io.vertx.tests.json.JsonProto;
import io.vertx.tests.json.MessageLiteral;
import io.vertx.tests.json.ProtoReader;
import io.vertx.tests.json.ProtoWriter;
import io.vertx.tests.json.Repetition;
import junit.framework.AssertionFailedError;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class JsonTest {

  @Test
  public void testNonObject() {
    try {
      read("null", MessageLiteral.Container);
      fail();
    } catch (DecodeException expected) {
    }

  }

  @Test
  public void testStruct() {
    assertEquals("string-value", testStruct(Value.newBuilder().setStringValue("string-value").build()).getKind().asStringValue().get());
    assertEquals(0, testStruct(Value.newBuilder().setNullValue(NullValue.NULL_VALUE).build()).getKind().asNullValue().get().number().orElseThrow());
    assertEquals(3.14D, testStruct(Value.newBuilder().setNumberValue(3.14).build()).getKind().asNumberValue().get(), 0.00001D);
    assertEquals(true, testStruct(Value.newBuilder().setBoolValue(true).build()).getKind().asBoolValue().get());
    assertEquals(false, testStruct(Value.newBuilder().setBoolValue(false).build()).getKind().asBoolValue().get());
    assertEquals(1, testStruct(Value.newBuilder().setListValue(ListValue.newBuilder().addValues(Value.newBuilder().setStringValue("the-string").build()).build()).build()).getKind().asListValue().get().getValues().size());
    assertEquals(1, testStruct(Value.newBuilder().setStructValue(Struct.newBuilder().putFields("the-key", Value.newBuilder().setStringValue("the-value").build()).build()).build()).getKind().asStructValue().get().getFields().size());
  }

  private io.vertx.protobuf.well_known_types.Value testStruct(Value value) {
    JsonProto.Container expected = JsonProto.Container.newBuilder()
      .setStruct(Struct.newBuilder()
        .putFields("key", value)
        .build())
      .build();
    Container read = read(expected, MessageLiteral.Container);
    JsonProto.Container actual = write(read);
    assertEquals(actual, expected);
    return read.getStruct().getFields().get("key");
  }

  @Test
  public void testValue() {
    assertEquals(0, testValue(Value.newBuilder().setNullValue(NullValue.NULL_VALUE).build()).asNullValue().get().number().orElseThrow());
    assertEquals(5.12D, testValue(Value.newBuilder().setNumberValue(5.12).build()).asNumberValue().get(), 0.0001D);
    assertEquals("the-string", testValue(Value.newBuilder().setStringValue("the-string").build()).asStringValue().get());
    assertTrue(testValue(Value.newBuilder().setBoolValue(true).build()).asBoolValue().get());
    testValue(Value.newBuilder().setStructValue(Struct.newBuilder().putFields("foo", Value.newBuilder().setStringValue("bar").build()).build()).build());
    testValue(Value.newBuilder().setListValue(ListValue.newBuilder().addValues(Value.newBuilder().setStringValue("bar").build()).build()).build());
  }

  private io.vertx.protobuf.well_known_types.Value.Kind<?> testValue(Value value) {
    JsonProto.Container expected = JsonProto.Container.newBuilder()
      .setValue(value)
      .build();
    Container read = read(expected, MessageLiteral.Container);
    JsonProto.Container actual = write(read);
    assertEquals(expected, actual);
    return read.getValue().getKind();
  }

  @Test
  public void testListValue() {
    JsonProto.Container expected = JsonProto.Container.newBuilder()
      .setListValue(ListValue.newBuilder()
        .addValues(Value.newBuilder().setStringValue("string-value").build())
        .addValues(Value.newBuilder().setNullValue(NullValue.NULL_VALUE).build())
        .addValues(Value.newBuilder().setNumberValue(3.14).build())
        .addValues(Value.newBuilder().setBoolValue(true).build())
        .addValues(Value.newBuilder().setBoolValue(false).build())
        .addValues(Value.newBuilder().setListValue(ListValue.newBuilder().addValues(Value.newBuilder().setStringValue("the-string").build()).build()).build())
        .addValues(Value.newBuilder().setStructValue(Struct.newBuilder().putFields("the-key", Value.newBuilder().setStringValue("the-value").build()).build()).build())
        .build())
      .build();
    Container read = read(expected, MessageLiteral.Container);
    assertEquals("string-value", read.getListValue().getValues().get(0).getKind().asStringValue().get());
    assertEquals(0, read.getListValue().getValues().get(1).getKind().asNullValue().get().number().orElseThrow());
    assertEquals(3.14D, read.getListValue().getValues().get(2).getKind().asNumberValue().get(), 0.00001D);
    assertEquals(true, read.getListValue().getValues().get(3).getKind().asBoolValue().get());
    assertEquals(false, read.getListValue().getValues().get(4).getKind().asBoolValue().get());
    assertEquals(1, read.getListValue().getValues().get(5).getKind().asListValue().get().getValues().size());
    assertEquals(1, read.getListValue().getValues().get(6).getKind().asStructValue().get().getFields().size());
    JsonProto.Container actual = write(read);
    assertEquals(actual, expected);
  }

  @Test
  public void testDuration() {
    long[] listOfSeconds = { 1, 0, 1, 1, -1, 0 };
    int[] listOfNano = { 1, 5, 0, 123456789, -1, 500_000_000 };
    for (int i = 0;i < listOfSeconds.length;i++) {
      JsonProto.Container expected = JsonProto.Container.newBuilder()
        .setDuration(Duration.newBuilder().setSeconds(listOfSeconds[i]).setNanos(listOfNano[i]).build())
        .build();
      Container container = read(expected, MessageLiteral.Container);
      assertEquals(listOfSeconds[i], (long)container.getDuration().getSeconds());
      assertEquals(listOfNano[i], (int)container.getDuration().getNanos());
      assertEquals(expected, write(container));
    }
  }

  @Test
  public void testReadInvalidDuration() {
    try {
      read(new JsonObject().put("duration", (JsonReader.MAX_DURATION_SECONDS + 1) + "s"), MessageLiteral.Container);
      read(new JsonObject().put("duration", (JsonReader.MIN_DURATION_SECONDS - 1) + "s"), MessageLiteral.Container);
      read(new JsonObject().put("duration", "0." + (JsonReader.MAX_DURATION_NANOS + 1) + "s"), MessageLiteral.Container);
      read(new JsonObject().put("duration", "0." + (JsonReader.MIN_DURATION_NANOS - 1) + "s"), MessageLiteral.Container);
      fail();
    } catch (DecodeException expected) {
    }
  }

  @Test
  public void testWriteInvalidDuration() {
    long[] listOfSeconds = {JsonReader.MAX_DURATION_SECONDS + 1, JsonReader.MIN_DURATION_SECONDS - 1, 0, 0};
    int[] listOfNano = {0, 0, JsonReader.MAX_DURATION_NANOS + 1, JsonReader.MIN_DURATION_NANOS - 1};

    // {"optionalDuration": "315576000001.000000000s"}

    for (int i = 0; i < listOfSeconds.length; i++) {
      try {
        toJson(new Container().setDuration(new io.vertx.protobuf.well_known_types.Duration()
          .setSeconds(listOfSeconds[i])
          .setNanos(listOfNano[i])));
        fail();
      } catch (EncodeException expected) {
      }
    }
  }

  @Test
  public void testReadInvalidTimestamp() {
    String[] timestamps = {
      "0001-01-01t00:00:00Z",
      "0001-01-01T00:00:00z",
      "0001-01-01 00:00:00Z",
      "0001-01-01T00:00:00",
      "10000-01-01T00:00:00Z",
      "0000-01-01T00:00:00Z"
    };
    for (int i = 0;i < timestamps.length;i++) {
      String timestamp = timestamps[i];
      try {
        read(new JsonObject().put(FieldLiteral.Container_timestamp.jsonName(), timestamp), MessageLiteral.Container);
        fail("Failed to parse " + i);
      } catch (DecodeException expected) {
      }
    }
  }

  @Test
  public void testWriteInvalidTimestamp() {
    long[] listOfSeconds = { 253402300800L, -62135596801L, 0L, 0L };
    int[] listOfNano = { 0, 0, 1_000_000_000, -1 };
    for (int i = 0; i < listOfSeconds.length; i++) {
      try {
        toJson(new Container().setTimestamp(new io.vertx.protobuf.well_known_types.Timestamp()
          .setSeconds(listOfSeconds[i])
          .setNanos(listOfNano[i])));
        fail();
      } catch (EncodeException expected) {
      }
    }
  }

  @Test
  public void testTimestamp() {
    long[] listOfSeconds = { 1, 0, 1, 1, 0, 729302400 };
    int[] listOfNano = { 1, 5, 0, 123456789, 500_000_000, 0 };
    for (int i = 0;i < listOfSeconds.length;i++) {
      JsonProto.Container expected = JsonProto.Container.newBuilder()
        .setTimestamp(Timestamp.newBuilder().setSeconds(listOfSeconds[i]).setNanos(listOfNano[i]).build())
        .build();
      Container container = read(expected, MessageLiteral.Container);
      assertEquals(listOfSeconds[i], (long)container.getTimestamp().getSeconds());
      assertEquals(listOfNano[i], (int)container.getTimestamp().getNanos());
      assertEquals(expected, write(container));
    }
  }

  @Test
  public void testWrappers() {
    testWrapper(JsonProto.Container.newBuilder().setDoubleValue(DoubleValue.newBuilder().setValue(4.5D)),
      container -> assertEquals(4.5D, container.getDoubleValue().getValue(), 0.0001D));
    testWrapper(JsonProto.Container.newBuilder().setFloatValue(FloatValue.newBuilder().setValue(4.2f)),
      container -> assertEquals(4.2F, container.getFloatValue().getValue(), 0.0001D));
    testWrapper(JsonProto.Container.newBuilder().setInt64Value(Int64Value.newBuilder().setValue(7L)),
      container -> assertEquals(7L, (long)container.getInt64Value().getValue()));
    testWrapper(JsonProto.Container.newBuilder().setUint64Value(UInt64Value.newBuilder().setValue(8L)),
      container -> assertEquals(8L, (long)container.getUint64Value().getValue()));
    testWrapper(JsonProto.Container.newBuilder().setInt32Value(Int32Value.newBuilder().setValue(3)),
      container -> assertEquals(3, (int)container.getInt32Value().getValue()));
    testWrapper(JsonProto.Container.newBuilder().setUint32Value(UInt32Value.newBuilder().setValue(4)),
      container -> assertEquals(4, (int)container.getUint32Value().getValue()));
    testWrapper(JsonProto.Container.newBuilder().setBoolValue(BoolValue.newBuilder().setValue(true)),
      container -> assertTrue(container.getBoolValue().getValue()));
    testWrapper(JsonProto.Container.newBuilder().setStringValue(StringValue.newBuilder().setValue("the-string")),
      container -> assertEquals("the-string", container.getStringValue().getValue()));
    testWrapper(JsonProto.Container.newBuilder().setBytesValue(BytesValue.newBuilder().setValue(ByteString.copyFromUtf8("the-bytes"))),
      container -> assertEquals("the-bytes", container.getBytesValue().getValue().toString("UTF-8")));
  }

  @Test
  public void testWrapperNullValue() {
    Container container = read(new JsonObject().put("int64Value", null), MessageLiteral.Container);

  }

  private void testWrapper(JsonProto.Container.Builder expected, Consumer<Container> checker) {
    for (boolean quoteNumbers : new boolean[]{true, false}) {
      Container container = read(expected, MessageLiteral.Container, quoteNumbers);
      checker.accept(container);
      assertEquals(expected.build(), write(container));
    }
  }

  @Test
  public void testIgnoreUnknownFields() {
    ProtoReader pr = new ProtoReader();
    JsonReader reader = new JsonReader("{\"unknown\":{\"foo\":3}}", pr);
    reader.ignoreUnknownFields(true);
    reader.read(MessageLiteral.Container);
  }

  @Test
  public void testRepetition() {
    JsonProto.Repetition expected = JsonProto.Repetition.newBuilder()
      .addInt32Value(Int32Value.newBuilder().setValue(1))
      .addInt32Value(Int32Value.newBuilder().setValue(2))
      .addListValue(ListValue.newBuilder().addValues(Value.newBuilder().setStringValue("1")))
      .addListValue(ListValue.newBuilder().addValues(Value.newBuilder().setNumberValue(2)))
      .addListValue(ListValue.newBuilder().addValues(Value.newBuilder().setBoolValue(true)))
      .addListValue(ListValue.newBuilder().addValues(Value.newBuilder().setNullValue(NullValue.NULL_VALUE)))
      .addListValue(ListValue.newBuilder().addValues(Value.newBuilder().setListValue(ListValue.newBuilder().addValues(Value.newBuilder().setStringValue("1")))))
      .addListValue(ListValue.newBuilder().addValues(Value.newBuilder().setStructValue(Struct.newBuilder().putFields("the-key", Value.newBuilder().setStringValue("the-value").build()))))
      .addValue(Value.newBuilder().setListValue(ListValue.newBuilder().addValues(Value.newBuilder().setStringValue("1"))))
      .build();
    Repetition repetition = read(expected, MessageLiteral.Repetition);
    assertEquals(2, repetition.getInt32Value().size());
    assertEquals(1, (int)repetition.getInt32Value().get(0).getValue());
    assertEquals(2, (int)repetition.getInt32Value().get(1).getValue());
    assertEquals(6, repetition.getListValue().size());
    for (int i = 0;i < 6;i++) {
      assertEquals(1, repetition.getListValue().get(i).getValues().size());
    }
    assertEquals("1", repetition.getListValue().get(0).getValues().get(0).getKind().asStringValue().get());
    assertEquals(2, repetition.getListValue().get(1).getValues().get(0).getKind().asNumberValue().get(), 0.0001D);
    assertEquals(true, repetition.getListValue().get(2).getValues().get(0).getKind().asBoolValue().get());
    assertEquals(0, repetition.getListValue().get(3).getValues().get(0).getKind().asNullValue().get().number().orElseThrow());
    assertEquals(1, repetition.getListValue().get(4).getValues().get(0).getKind().asListValue().get().getValues().size());
    assertEquals(1, repetition.getListValue().get(5).getValues().get(0).getKind().asStructValue().get().getFields().size());
    assertEquals(1, repetition.getValue().size());
    assertEquals(1, repetition.getValue().get(0).getKind().asListValue().get().getValues().size());
  }

  @Ignore
  @Test
  public void testAny() {
    JsonObject json = new JsonObject().put("any",
      new JsonObject()
        .put("@type", "type.googleapis.com/google.protobuf.Struct")
        .put("value", new JsonObject().put("foo", 1))
    );
    Container container = read(json, MessageLiteral.Container);
  }

  @Test
  public void testLowerCamelToSnake() {
    assertEquals("bar_baz", JsonReader.lowerCamelToSnake("BarBaz"));
    assertEquals("_bar_baz", JsonReader.lowerCamelToSnake("_BarBaz"));
    assertEquals("bar_baz", JsonReader.lowerCamelToSnake("barBaz"));
    assertEquals("bar_1234", JsonReader.lowerCamelToSnake("Bar_1234"));
    assertEquals("bar_", JsonReader.lowerCamelToSnake("Bar_"));
    assertEquals("bar_baz", JsonReader.lowerCamelToSnake("Bar_Baz"));
    assertEquals("bar__baz", JsonReader.lowerCamelToSnake("Bar__Baz"));
    assertEquals("b_b_b", JsonReader.lowerCamelToSnake("BBB"));
    assertEquals("a_ba_ba_b", JsonReader.lowerCamelToSnake("aBaBaB"));
    assertEquals("", JsonReader.lowerCamelToSnake(""));
  }

  @Test
  public void testSnakeToLowerCamel() {
    assertEquals("barBaz", JsonReader.snakeToLowerCamel("bar_baz"));
    assertEquals("BarBaz", JsonReader.snakeToLowerCamel("_bar_baz"));
    assertEquals("bar12Baz", JsonReader.snakeToLowerCamel("bar12_baz"));
    assertEquals("barBaz", JsonReader.snakeToLowerCamel("bar_Baz"));
    assertEquals("Bar", JsonReader.snakeToLowerCamel("__bar"));
    assertEquals("123", JsonReader.snakeToLowerCamel("_123"));
    assertEquals("bBB", JsonReader.snakeToLowerCamel("b_b_b"));
  }

  @Test
  public void testFieldMask() throws Exception {
    testFieldMask("foo,barBaz", "foo", "bar_baz");
    testFieldMask("foo,BBB", "foo", "b_b_b");
    testFieldMask("foo,aBaBaB", "foo", "a_ba_ba_b");
    testFieldMask("");
  }

  private void testFieldMask(String mask, String... expected) throws Exception {
    JsonObject json = new JsonObject()
      .put("fieldMask", mask);
    JsonProto.Container.Builder builder = JsonProto.Container.newBuilder();
    JsonFormat.parser().merge(json.encode(), builder);
    ProtocolStringList list = builder.build().getFieldMask().getPathsList();
    assertEquals(Arrays.asList(expected), list);
    Container container = read(json, MessageLiteral.Container);
    assertEquals(Arrays.asList(expected), container.getFieldMask().getPaths());
    JsonObject ser = toJson(container);
    assertEquals(new JsonObject(JsonFormat.printer().print(builder.build())), ser);
  }

  @Test
  public void testEmptyFieldMask() {
    // TODO
  }

  private <T> T read(MessageOrBuilder container, MessageType type) {
    return read(container, type, false);
  }

  private static void quoteNumbers(Object o) {
    if (o instanceof JsonObject) {
      for (Map.Entry<String, Object> entry : (JsonObject)o) {
        Object value = entry.getValue();
        if (value instanceof Number) {
          entry.setValue("" + value);
        } else {
          quoteNumbers(value);
        }
      }
    } else if (o instanceof JsonArray) {
      JsonArray array = (JsonArray) o;
      for (int i = 0;i < array.size();i++) {
        Object value = array.getValue(i);
        if (value instanceof Number) {
          array.set(i, "" + value);
        } else {
          quoteNumbers(value);
        }
      }
    }
  }

  private <T> T read(MessageOrBuilder container, MessageType type, boolean quoteNumbers) {
    try {
      JsonObject json = new JsonObject(JsonFormat.printer().print(container));
      if (quoteNumbers) {
        quoteNumbers(json);
      }
      return read(json, type);
    } catch (InvalidProtocolBufferException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
  }

  private <T> T read(String json, MessageType type) {
    ProtoReader pr = new ProtoReader();
    JsonReader.parse(json, type, pr);
    return (T)pr.stack.pop();
  }

  private <T> T read(JsonObject json, MessageType type) {
    return read(json.encode(), type);
  }

  private JsonObject toJson(Container container) {
    return JsonWriter.encode(visitor -> {
      ProtoWriter.emit(container, visitor);
    });
  }

  private JsonProto.Container write(Container container) {
    JsonObject json = toJson(container);
    JsonProto.Container.Builder builder = JsonProto.Container.newBuilder();
    try {
      JsonFormat.parser().merge(json.encode(), builder);
    } catch (InvalidProtocolBufferException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
    return builder.build();
  }
}
