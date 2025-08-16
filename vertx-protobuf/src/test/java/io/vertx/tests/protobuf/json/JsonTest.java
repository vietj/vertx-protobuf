package io.vertx.tests.protobuf.json;

import com.google.protobuf.Duration;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.ListValue;
import com.google.protobuf.NullValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Timestamp;
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
  public void testReadStruct() throws Exception {
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
