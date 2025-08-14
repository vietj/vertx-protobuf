package io.vertx.tests.protobuf;

import com.google.protobuf.Duration;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.vertx.core.json.JsonObject;
import io.vertx.tests.interop.InteropProto;
import io.vertx.tests.interop.Container;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public abstract class InteropTestBase {

  @Test
  public void testStruct() {
    InteropProto.Container expected = InteropProto.Container.newBuilder()
      .setStruct(Struct.newBuilder().putFields("the-string", Value.newBuilder().setStringValue("the-string-value").build()).build())
      .build();
    Container msg = read(expected);
    JsonObject s = msg.getStruct();
    assertEquals("the-string-value", s.getString("the-string"));
    InteropProto.Container encoded = write(msg);
    assertEquals(expected, encoded);
  }

  @Test
  public void testDuration() {
    testDuration(1, 1);
    testDuration(0, 1);
    testDuration(1, 0);
    testDuration(1, 123456789);
  }

  private void testDuration(long seconds, int nano) {
    InteropProto.Container expected = InteropProto.Container.newBuilder()
      .setDuration(Duration.newBuilder().setSeconds(seconds).setNanos(nano))
      .build();
    Container msg = read(expected);
    java.time.Duration duration = msg.getDuration();
    assertEquals(seconds, duration.getSeconds());
    assertEquals(nano, duration.getNano());
    InteropProto.Container actual = write(msg);
    assertEquals(expected.getDuration().getSeconds(), actual.getDuration().getSeconds());
    assertEquals(expected.getDuration().getNanos(), actual.getDuration().getNanos());
  }

  protected abstract Container read(InteropProto.Container src);
  protected abstract InteropProto.Container write(Container src);

}
