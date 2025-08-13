package io.vertx.tests.protobuf.json;

import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.vertx.core.json.JsonObject;
import io.vertx.protobuf.json.JsonReader;
import io.vertx.protobuf.json.JsonWriter;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.tests.protobuf.DataTypeTestBase;
import io.vertx.tests.protobuf.RecordingVisitor;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DataTypesTest extends DataTypeTestBase {

  @Override
  protected void testDataType(RecordingVisitor visitor, MessageType messageType, MessageLite expected) throws Exception {
    String json = JsonFormat.printer().print((MessageOrBuilder) expected);

    RecordingVisitor.Checker checker = visitor.checker();
    JsonReader.parse(json, messageType, checker);
    assertTrue(checker.isEmpty());

    // Try parsing string formatted numbers as real numbers, this must be parseable
    JsonObject jsonObject = new JsonObject(json);
    for (Map.Entry<String, Object> entry : jsonObject) {
      if (entry.getValue() instanceof String) {
        try {
          entry.setValue(Long.parseLong((String)entry.getValue()));
          json = jsonObject.encode();
          checker = visitor.checker();
          JsonReader.parse(json, messageType, checker);
          assertTrue(checker.isEmpty());
        } catch (NumberFormatException ignore) {
        }
      }
    }

    String encoded = JsonWriter.encode(visitor::apply).toString();
    Message.Builder builder = ((Message) expected).newBuilderForType();
    JsonFormat.parser().merge(encoded, builder);
    assertEquals(expected, builder.build());
  }
}
