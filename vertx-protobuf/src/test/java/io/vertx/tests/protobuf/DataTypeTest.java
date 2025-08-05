package io.vertx.tests.protobuf;

import com.google.protobuf.MessageLite;
import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.protobuf.schema.MessageType;
import org.junit.Test;

import static org.junit.Assert.*;

public class DataTypeTest extends DataTypeTestBase {

  protected void testDataType(RecordingVisitor visitor, MessageType messageType, MessageLite expected) throws Exception {
    byte[] bytes = expected.toByteArray();
    RecordingVisitor.Checker checker = visitor.checker();
    ProtobufReader.parse(messageType, checker, Buffer.buffer(bytes));
    assertTrue(checker.isEmpty());
    bytes = ProtobufWriter.encode(visitor::apply).getBytes();
    assertEquals(expected, expected.getParserForType().parseFrom(bytes));
  }

  @Test
  public void testReadOversizedBoolean() throws Exception {
    byte[] data = { (byte)(BOOL.number() * 8), -128, -128, -128, -128, -128, -128, -128, -128, -128, 1 };
    RecordingVisitor visitor = new RecordingVisitor();
    ProtobufReader.parse(SCALAR_TYPES, visitor, Buffer.buffer(data));
    RecordingVisitor.Checker checker = visitor.checker();
    checker.init(SCALAR_TYPES);
    checker.visitBool(BOOL, true);
  }
}
