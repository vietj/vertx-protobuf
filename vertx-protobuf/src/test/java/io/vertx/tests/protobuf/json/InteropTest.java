package io.vertx.tests.protobuf.json;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.vertx.protobuf.json.ProtoJsonReader;
import io.vertx.protobuf.json.ProtoJsonWriter;
import io.vertx.protobuf.well_known_types.Duration;
import io.vertx.tests.interop.Container;
import io.vertx.tests.interop.InteropProto;
import io.vertx.tests.interop.MessageLiteral;
import io.vertx.tests.interop.ProtoReader;
import io.vertx.tests.interop.ProtoWriter;
import io.vertx.tests.protobuf.InteropTestBase;
import junit.framework.AssertionFailedError;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class InteropTest extends InteropTestBase {

  @Override
  protected Container read(InteropProto.Container src) {
    String json;
    try {
      json = JsonFormat.printer().print(src);
    } catch (InvalidProtocolBufferException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
    ProtoReader reader = new ProtoReader();
    ProtoJsonReader.parse(json, MessageLiteral.Container, reader);
    return (Container) reader.stack.pop();
  }

  @Override
  protected InteropProto.Container write(Container src) {
    String json = ProtoJsonWriter.encode(v -> ProtoWriter.emit(src, v)).encode();
    try {
      InteropProto.Container.Builder builder = InteropProto.Container.newBuilder();
      JsonFormat.parser().merge(json, builder);
      return builder.build();
    } catch (InvalidProtocolBufferException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
  }

  @Test
  public void testDurationConversion() {
    assertDuration(new Duration(), "0s");
    assertDuration(new Duration().setSeconds(1L), "1s");
    assertDuration(new Duration().setNanos(1), "0.000000001s");
    assertDuration(new Duration().setNanos(10), "0.00000001s");
    assertDuration(new Duration().setNanos(100), "0.0000001s");
    assertDuration(new Duration().setNanos(123456789), "0.123456789s");
    assertDuration(new Duration().setSeconds(-315576000000L).setNanos(-999999999), "-315576000000.999999999s");
  }

  private void assertDuration(Duration expected, String s) {
    Duration parsed = ProtoJsonReader.parseDuration(s);
    assertNotNull(parsed);
    assertEquals(expected.getSeconds(), parsed.getSeconds());
    assertEquals(expected.getNanos(), parsed.getNanos());
  }
}
