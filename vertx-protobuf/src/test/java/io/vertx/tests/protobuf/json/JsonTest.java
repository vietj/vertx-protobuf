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
import com.google.protobuf.NullValue;
import com.google.protobuf.StringValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Timestamp;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
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
import junit.framework.AssertionFailedError;
import org.junit.Test;

import static org.junit.Assert.*;

public class JsonTest {

  @Test
  public void testStruct() throws Exception {
    JsonProto.Container expected = JsonProto.Container.newBuilder()
      .setStruct(Struct.newBuilder()
        .putFields("string-key", Value.newBuilder().setStringValue("string-value").build())
        .putFields("null-key", Value.newBuilder().setNullValue(NullValue.NULL_VALUE).build())
        .putFields("number-key", Value.newBuilder().setNumberValue(3.14).build())
        .putFields("true-key", Value.newBuilder().setBoolValue(true).build())
        .putFields("false-key", Value.newBuilder().setBoolValue(false).build())
        .putFields("array-key", Value.newBuilder().setListValue(ListValue.newBuilder().addValues(Value.newBuilder().setStringValue("the-string").build()).build()).build())
        .putFields("object-key", Value.newBuilder().setStructValue(Struct.newBuilder().putFields("the-key", Value.newBuilder().setStringValue("the-value").build()).build()).build())
        .build())
      .build();
    Container read = read(expected);
    assertEquals("string-value", read.getStruct().getFields().get("string-key").getKind().asStringValue().get());
    assertEquals(0, read.getStruct().getFields().get("null-key").getKind().asNullValue().get().number());
    assertEquals(3.14D, read.getStruct().getFields().get("number-key").getKind().asNumberValue().get(), 0.00001D);
    assertEquals(true, read.getStruct().getFields().get("true-key").getKind().asBoolValue().get());
    assertEquals(false, read.getStruct().getFields().get("false-key").getKind().asBoolValue().get());
    assertEquals(1, read.getStruct().getFields().get("array-key").getKind().asListValue().get().getValues().size());
    assertEquals(1, read.getStruct().getFields().get("object-key").getKind().asStructValue().get().getFields().size());
    JsonProto.Container actual = write(read);
    assertEquals(actual, expected);
  }

  @Test
  public void testValue() {
    assertEquals(0, testValue(Value.newBuilder().setNullValue(NullValue.NULL_VALUE).build()).asNullValue().get().number());
    assertEquals(5.12D, testValue(Value.newBuilder().setNumberValue(5.12).build()).asNumberValue().get(), 0.0001D);
    assertEquals("the-string", testValue(Value.newBuilder().setStringValue("the-string").build()).asStringValue().get());
    assertTrue(testValue(Value.newBuilder().setBoolValue(true).build()).asBoolValue().get());
    testValue(Value.newBuilder().setStructValue(Struct.newBuilder().putFields("foo", Value.newBuilder().setStringValue("bar").build()).build()).build());
//    testValue(Value.newBuilder().setListValue(ListValue.newBuilder().addValues(Value.newBuilder().setStringValue("bar").build()).build()).build());
  }

  private io.vertx.protobuf.well_known_types.Value.Kind<?> testValue(Value value) {
    JsonProto.Container expected = JsonProto.Container.newBuilder()
      .setValue(value)
      .build();
    Container read = read(expected);
    JsonProto.Container actual = write(read);
    assertEquals(expected, actual);
    return read.getValue().getKind();
  }

  @Test
  public void testDuration() {
    long[] listOfSeconds = { 1, 0, 1, 1, -1, 0 };
    int[] listOfNano = { 1, 5, 0, 123456789, -1, 500_000_000 };
    for (int i = 0;i < listOfSeconds.length;i++) {
      JsonProto.Container expected = JsonProto.Container.newBuilder()
        .setDuration(Duration.newBuilder().setSeconds(listOfSeconds[i]).setNanos(listOfNano[i]).build())
        .build();
      Container container = read(expected);
      assertEquals(listOfSeconds[i], (long)container.getDuration().getSeconds());
      assertEquals(listOfNano[i], (int)container.getDuration().getNanos());
      assertEquals(expected, write(container));
    }
  }

  @Test
  public void testTimestamp() {
    long[] listOfSeconds = { 1, 0, 1, 1, 0 };
    int[] listOfNano = { 1, 5, 0, 123456789, 500_000_000 };
    for (int i = 0;i < listOfSeconds.length;i++) {
      JsonProto.Container expected = JsonProto.Container.newBuilder()
        .setTimestamp(Timestamp.newBuilder().setSeconds(listOfSeconds[i]).setNanos(listOfNano[i]).build())
        .build();
      Container container = read(expected);
      assertEquals(listOfSeconds[i], (long)container.getTimestamp().getSeconds());
      assertEquals(listOfNano[i], (int)container.getTimestamp().getNanos());
      assertEquals(expected, write(container));
    }
  }

  @Test
  public void testWrappers() {
    JsonProto.Container expected = JsonProto.Container.newBuilder()
      .setDoubleValue(DoubleValue.newBuilder().setValue(4.5D))
      .setFloatValue(FloatValue.newBuilder().setValue(4.2f))
      .setInt64Value(Int64Value.newBuilder().setValue(7L))
      .setUint64Value(UInt64Value.newBuilder().setValue(8L))
      .setInt32Value(Int32Value.newBuilder().setValue(3))
      .setUint32Value(UInt32Value.newBuilder().setValue(4))
      .setBoolValue(BoolValue.newBuilder().setValue(true))
      .setStringValue(StringValue.newBuilder().setValue("the-string"))
      .setBytesValue(BytesValue.newBuilder().setValue(ByteString.copyFromUtf8("the-bytes")))
      .build();
    Container container = read(expected);
    assertEquals(4.5D, container.getDoubleValue().getValue(), 0.0001D);
    assertEquals(4.2F, container.getFloatValue().getValue(), 0.0001D);
    assertEquals(7L, (long)container.getInt64Value().getValue());
    assertEquals(8L, (long)container.getUint64Value().getValue());
    assertEquals(3, (int)container.getInt32Value().getValue());
    assertEquals(4, (int)container.getUint32Value().getValue());
    assertTrue(container.getBoolValue().getValue());
    assertEquals("the-string", container.getStringValue().getValue());
    assertEquals("the-bytes", container.getBytesValue().getValue().toString("UTF-8"));
    assertEquals(expected, write(container));
  }

  private Container read(JsonProto.Container container) {
    try {
      String json = JsonFormat.printer().print(container);
      ProtoReader pr = new ProtoReader();
      JsonReader.parse(json, MessageLiteral.Container, pr);
      return (Container) pr.stack.pop();
    } catch (InvalidProtocolBufferException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
  }

  private JsonProto.Container write(Container container) {
    JsonObject json = JsonWriter.encode(visitor -> {
      ProtoWriter.emit(container, visitor);
    });
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
