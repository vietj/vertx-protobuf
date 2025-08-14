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
    InteropProto.Container expected = InteropProto.Container.newBuilder()
      .setDuration(Duration.newBuilder().setSeconds(1).setNanos(1))
      .build();
    Container msg = read(expected);
    java.time.Duration duration = msg.getDuration();
    assertEquals(1, duration.getSeconds());
    assertEquals(1, duration.getNano());
    InteropProto.Container encoded = write(msg);
    assertEquals(expected, encoded);
  }

  protected abstract Container read(InteropProto.Container src);
  protected abstract InteropProto.Container write(Container src);

}
