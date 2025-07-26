package io.vertx.tests.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.ScalarType;
import io.vertx.protobuf.schema.Schema;
import io.vertx.tests.protobuf.datatypes.DataTypesProto;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DataTypeTest {

  private static final Schema SCHEMA = new Schema();
  private static final MessageType DATA_TYPE = SCHEMA.of("DataType");
  private static final Field INT64 = DATA_TYPE.addField(4, ScalarType.INT64);
  private static final Field UINT32 = DATA_TYPE.addField(5, ScalarType.UINT32);
  private static final Field UINT64 = DATA_TYPE.addField(6, ScalarType.UINT64);
  private static final Field SINT32 = DATA_TYPE.addField(7, ScalarType.SINT32);
  private static final Field SINT64 = DATA_TYPE.addField(8, ScalarType.SINT64);

  private void testDataType(RecordingVisitor visitor, DataTypesProto.DataTypes expected) throws Exception {
    byte[] bytes = expected.toByteArray();
    RecordingVisitor.Checker checker = visitor.checker();
    ProtobufReader.parse(DATA_TYPE, checker, Buffer.buffer(bytes));
    assertTrue(checker.isEmpty());
    bytes = ProtobufWriter.encode(visitor::apply).getBytes();
    assertEquals(expected, DataTypesProto.DataTypes.parseFrom(bytes));
  }

  @Test
  public void testSint() {
    test(0, 0);
    test(1, -1);
    test(2, 1);
    test(3, -2);
    test(4, 2);
    test(5, -3);
  }

  private static void test(int encoded, int decoded) {

    // Encoding
    int d = (decoded << 1) ^ (decoded >> 31);

    assertEquals(encoded, d);

    // Decoding

    int b;
    if ((encoded & 1) == 0) {
      b = encoded / 2;
    } else {
      b = (encoded + 1) / -2;
    }
    assertEquals(decoded, b);

//    System.out.println(decoded + " " + c + " ");




  }

  @Test
  public void testInt64() throws Exception {
    testInt64(4);
    // testVarInt64(Long.MAX_VALUE);
  }

  private void testInt64(long value) throws Exception {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(DATA_TYPE);
    visitor.visitVarInt64(INT64, value);
    visitor.destroy();
    testDataType(visitor, DataTypesProto.DataTypes.newBuilder().setInt64(value).build());
  }

  @Test
  public void testUint64() throws Exception {
    testUint64(4);
    // testVarInt64(Long.MAX_VALUE);
  }

  private void testUint64(long value) throws Exception {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(DATA_TYPE);
    visitor.visitVarInt64(UINT64, value);
    visitor.destroy();
    testDataType(visitor, DataTypesProto.DataTypes.newBuilder().setUint64(value).build());
  }

  @Test
  public void testUint32() throws Exception {
    testUint32(4);
    testUint32(Integer.MAX_VALUE);
  }

  private void testUint32(int value) throws Exception {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(DATA_TYPE);
    visitor.visitVarInt32(UINT32, value);
    visitor.destroy();
    testDataType(visitor, DataTypesProto.DataTypes.newBuilder().setUint32(value).build());
  }

  @Test
  public void testSint32() throws Exception {
    testSint32(4);
    // testSint32(Integer.MAX_VALUE);
  }

  private void testSint32(int value) throws Exception {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(DATA_TYPE);
    visitor.visitVarInt32(SINT32, value);
    visitor.destroy();
    testDataType(visitor, DataTypesProto.DataTypes.newBuilder().setSint32(value).build());
  }

  @Test
  public void testSint64() throws Exception {
    testSint64(4);
    // testSint32(Integer.MAX_VALUE);
  }

  private void testSint64(long value) throws Exception {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(DATA_TYPE);
    visitor.visitVarInt64(SINT64, value);
    visitor.destroy();
    testDataType(visitor, DataTypesProto.DataTypes.newBuilder().setSint64(value).build());
  }
}
