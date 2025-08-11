package io.vertx.tests.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.protobuf.schema.DefaultField;
import io.vertx.protobuf.schema.DefaultMessageType;
import io.vertx.protobuf.schema.DefaultSchema;
import io.vertx.protobuf.schema.ScalarType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProtobufWriterTest {

  @Test
  public void testWritePackedUInt64() {
    DefaultSchema schema = new DefaultSchema();
    DefaultMessageType msg = schema.of("msg");
    DefaultField field = msg.addField(builder -> builder.number(1).type(ScalarType.UINT64).repeated(true));
    Buffer output = ProtobufWriter.encode(visitor -> {
      visitor.init(msg);
      visitor.enterPacked(field);
      visitor.visitUInt64(field, -1);
      visitor.leavePacked(field);
    });
    assertEquals(10, output.getByte(1));
  }

  @Test
  public void testSizeOfField() {
    DefaultSchema schema = new DefaultSchema();
    DefaultMessageType msg = schema.of("msg");
    DefaultMessageType nested = schema.of("msg");
    DefaultField nestedField = msg.addField(1, nested);
    DefaultField fixed32Field = nested.addField(builder -> builder.number(89).type(ScalarType.FIXED32).repeated(true));
    Buffer output = ProtobufWriter.encode(visitor -> {
      visitor.init(msg);
      visitor.enter(nestedField);
      visitor.enterPacked(fixed32Field);
      visitor.visitFixed32(fixed32Field, 1);
      visitor.leavePacked(fixed32Field);
      visitor.leave(nestedField);
    });
    assertEquals(7, output.getByte(1));
  }
}
