package io.vertx.tests.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.protobuf.ProtobufDecoder;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.Visitor;
import io.vertx.protobuf.schema.DefaultMessageType;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.ScalarType;
import org.junit.Test;

import static org.junit.Assert.fail;

public class ProtobufReaderTest {

  @Test
  public void testReadInvalidTagWireType() {

    byte[] data = { 8 + 4 };
    DefaultMessageType msg = new DefaultMessageType("whatever");
    msg.addField(1, ScalarType.STRING);
    Visitor visitor = new Visitor() {
      @Override
      public void init(MessageType type) {
      }
      @Override
      public void visitVarInt32(Field field, int v) {
      }
      @Override
      public void visitString(Field field, String s) {
      }
      @Override
      public void enter(Field field) {
      }
      @Override
      public void leave(Field field) {
      }
      @Override
      public void destroy() {
      }
    };
    try {
      ProtobufReader.parse(msg, visitor, Buffer.buffer(data));
      fail();
    } catch (DecodeException expected) {
    }
  }
}
