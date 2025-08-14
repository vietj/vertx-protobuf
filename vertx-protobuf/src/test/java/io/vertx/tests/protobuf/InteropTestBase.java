package io.vertx.tests.protobuf;

import com.google.protobuf.Duration;
import com.google.protobuf.ListValue;
import com.google.protobuf.NullValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.vertx.tests.interop.InteropProto;
import io.vertx.tests.interop.Container;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public abstract class InteropTestBase {

  @Test
  public void testStruct() {
    InteropProto.Container expected = InteropProto.Container.newBuilder()
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
    Container msg = read(expected);
    InteropProto.Container encoded = write(msg);
    assertEquals(expected, encoded);
  }

  @Test
  public void testDuration() {
    testDuration(1, 1);
    testDuration(0, 5);
    testDuration(1, 0);
    testDuration(1, 123456789);
    testDuration(-1, -1);
    testDuration(0, 500_000_000);
  }

  private void testDuration(long seconds, int nano) {
    InteropProto.Container expected = InteropProto.Container.newBuilder()
      .setDuration(Duration.newBuilder().setSeconds(seconds).setNanos(nano))
      .build();
    Container msg = read(expected);
    java.time.Duration duration = msg.getDuration();
    assertEquals(java.time.Duration.ofSeconds(seconds, nano), duration);
    InteropProto.Container actual = write(msg);
    assertEquals(java.time.Duration.ofSeconds(expected.getDuration().getSeconds(), expected.getDuration().getNanos()),
      java.time.Duration.ofSeconds(actual.getDuration().getSeconds(), actual.getDuration().getNanos()));
  }

  protected abstract Container read(InteropProto.Container src);
  protected abstract InteropProto.Container write(Container src);

}
