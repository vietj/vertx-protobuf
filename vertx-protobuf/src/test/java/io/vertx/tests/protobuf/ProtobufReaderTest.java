package io.vertx.tests.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.RecordVisitor;
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
    testInvalidInput(data);
  }

  @Test
  public void testReadInvalidTagNumberType() {
    byte[] data = { 1 };
    testInvalidInput(data);
  }

  private void testInvalidInput(byte[] data) {
    DefaultMessageType msg = new DefaultMessageType("whatever");
    msg.addField(1, ScalarType.STRING);
    RecordingVisitor visitor = new RecordingVisitor();
    try {
      ProtobufReader.parse(msg, visitor, Buffer.buffer(data));
      fail();
    } catch (DecodeException expected) {
    }
  }
}
