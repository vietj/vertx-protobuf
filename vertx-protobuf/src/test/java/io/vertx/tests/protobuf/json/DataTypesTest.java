package io.vertx.tests.protobuf.json;

import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.vertx.protobuf.json.JsonReader;
import io.vertx.protobuf.json.JsonWriter;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.tests.protobuf.DataTypeTestBase;
import io.vertx.tests.protobuf.RecordingVisitor;
import io.vertx.tests.protobuf.datatypes.DataTypesProto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DataTypesTest extends DataTypeTestBase {

  @Override
  protected void testDataType(RecordingVisitor visitor, MessageType messageType, MessageLite expected) throws Exception {
    String json = JsonFormat.printer().print((MessageOrBuilder) expected);
    RecordingVisitor.Checker checker = visitor.checker();
    JsonReader.parse(json, messageType, checker);
    assertTrue(checker.isEmpty());
    String encoded = JsonWriter.encode(visitor::apply);
    Message.Builder builder = ((Message) expected).newBuilderForType();
    JsonFormat.parser().merge(encoded, builder);
    assertEquals(expected, builder.build());
  }
}
