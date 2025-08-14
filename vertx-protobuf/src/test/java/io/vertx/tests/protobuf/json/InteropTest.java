package io.vertx.tests.protobuf.json;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.json.JsonReader;
import io.vertx.tests.interop.Container;
import io.vertx.tests.interop.InteropProto;
import io.vertx.tests.interop.MessageLiteral;
import io.vertx.tests.interop.ProtoReader;
import io.vertx.tests.protobuf.InteropTestBase;
import junit.framework.AssertionFailedError;

public class InteropTest extends InteropTestBase {

  @Override
  protected Container convert(InteropProto.Container src) {
    String json;
    try {
      json = JsonFormat.printer().print(src);
    } catch (InvalidProtocolBufferException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
    ProtoReader reader = new ProtoReader();
    JsonReader.parse(json, MessageLiteral.Container, reader);
    return (Container) reader.stack.pop();
  }
}
