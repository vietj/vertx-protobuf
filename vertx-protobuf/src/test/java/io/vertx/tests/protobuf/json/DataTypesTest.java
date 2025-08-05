package io.vertx.tests.protobuf.json;

import com.fasterxml.jackson.core.JsonParser;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.vertx.core.json.jackson.JacksonCodec;
import io.vertx.protobuf.json.JsonReader;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.tests.protobuf.DataTypeTestBase;
import io.vertx.tests.protobuf.RecordingVisitor;
import io.vertx.tests.protobuf.datatypes.DataTypesProto;

import static org.junit.Assert.assertTrue;

public class DataTypesTest extends DataTypeTestBase {

  @Override
  protected void testDataType(RecordingVisitor visitor, MessageType messageType, MessageLite expected) throws Exception {
    String json = JsonFormat.printer().print((MessageOrBuilder) expected);
    JsonParser parser = JacksonCodec.createParser(json);
    RecordingVisitor.Checker checker = visitor.checker();
    JsonReader.parse(parser, messageType, checker);
    assertTrue(checker.isEmpty());
//    bytes = ProtobufWriter.encode(visitor::apply).getBytes();
//    assertEquals(expected, DataTypesProto.ScalarTypes.parseFrom(bytes));
  }
}
