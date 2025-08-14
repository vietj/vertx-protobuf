package io.vertx.tests.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.tests.interop.Container;
import io.vertx.tests.interop.InteropProto;
import io.vertx.tests.interop.MessageLiteral;
import io.vertx.tests.interop.ProtoReader;

public class InteropTest extends InteropTestBase {

  @Override
  protected Container convert(InteropProto.Container src) {
    byte[] bytes = src.toByteArray();
    io.vertx.tests.interop.ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(MessageLiteral.Container, reader, Buffer.buffer(bytes));
    return (Container) reader.stack.pop();
  }
}
