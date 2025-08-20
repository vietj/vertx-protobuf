package io.vertx.tests.protobuf;

import com.google.protobuf.MessageLite;
import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.tests.protobuf.datatypes.EnumTypes;
import io.vertx.tests.protobuf.datatypes.Enumerated;
import io.vertx.tests.protobuf.datatypes.FieldLiteral;
import io.vertx.tests.protobuf.datatypes.MessageLiteral;
import io.vertx.tests.protobuf.datatypes.ProtoReader;
import io.vertx.tests.protobuf.datatypes.ProtoWriter;
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

  @Test
  public void testUnknownEnum() {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(MessageLiteral.EnumTypes);
    visitor.visitEnum(FieldLiteral.EnumTypes__enum, -1); // Unknown
    visitor.destroy();
    Buffer buffer = ProtobufWriter.encode(visitor::apply);
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(MessageLiteral.EnumTypes, reader, buffer);
    EnumTypes pop = (EnumTypes) reader.stack.pop();
    Enumerated enumerated = pop.getEnum();
    assertTrue(enumerated.isUnknown());
    assertEquals(-1, enumerated.number().orElseThrow());
    try {
      assertNull(enumerated.name());
      fail();
    } catch (IllegalStateException expected) {
    }
    assertNull(enumerated.asEnum());
    Buffer res = ProtobufWriter.encode(v -> ProtoWriter.emit(pop, v));
    RecordingVisitor.Checker checker = visitor.checker();
    ProtobufReader.parse(MessageLiteral.EnumTypes, checker, res);
    assertTrue(checker.isEmpty());
  }
}
