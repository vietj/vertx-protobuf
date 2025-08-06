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
      visitor.enterRepetition(field);
      visitor.visitUInt64(field, -1);
      visitor.leaveRepetition(field);
    });
    assertEquals(10, output.getByte(1));
  }

  @Test
  public void testSizeOfField() {
    DefaultSchema schema = new DefaultSchema();
    DefaultMessageType msg = schema.of("msg");
    DefaultMessageType nested = schema.of("msg");
    DefaultField nestedField = msg.addField(1, nested);
    DefaultField fixed32Field = nested.addField(89, ScalarType.FIXED32);
    Buffer output = ProtobufWriter.encode(visitor -> {
      visitor.init(msg);
      visitor.enter(nestedField);
      visitor.enterRepetition(fixed32Field);
      visitor.visitFixed32(fixed32Field, 1);
      visitor.leaveRepetition(fixed32Field);
      visitor.leave(nestedField);
    });
    assertEquals(7, output.getByte(1));
  }
}
