package io.vertx.tests.protobuf;

import com.google.protobuf.Struct;
import io.vertx.core.json.JsonObject;
import io.vertx.tests.interop.InteropProto;
import io.vertx.tests.interop.Container;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public abstract class InteropTestBase {

  @Test
  public void testReadStruct() {
    Container msg = convert(InteropProto.Container.newBuilder()
      .setStruct(Struct.newBuilder().putFields("the-string", com.google.protobuf.Value.newBuilder().setStringValue("the-string-value").build()).build())
      .build());
    JsonObject s = msg.getStruct();
    assertEquals("the-string-value", s.getString("the-string"));
  }

  protected abstract Container convert(InteropProto.Container src);

}
