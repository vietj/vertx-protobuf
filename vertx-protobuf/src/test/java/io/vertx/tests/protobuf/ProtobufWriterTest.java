package io.vertx.tests.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufEncoder;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.protobuf.schema.DefaultField;
import io.vertx.protobuf.schema.DefaultMessageType;
import io.vertx.protobuf.schema.DefaultSchema;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.ScalarType;
import io.vertx.protobuf.schema.Schema;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProtobufWriterTest {

  @Test
  public void testWritePackedUInt64() {
    DefaultSchema schema = new DefaultSchema();
    DefaultMessageType msg = schema.of("msg");
    DefaultField field = msg.addField(1, ScalarType.UINT64);
    Buffer output = ProtobufWriter.encode(visitor -> {
      visitor.init(msg);
      visitor.enter(field);
      visitor.visitUInt64(field, -1);
      visitor.leave(field);
    });
    assertEquals(10, output.getByte(1));
  }
}
