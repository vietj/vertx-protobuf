package io.vertx.tests.protobuf.json;

import com.google.protobuf.ByteString;
import com.google.protobuf.MapEntry;
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
import io.vertx.tests.protobuf.datatypes.DataTypesProto;
import org.junit.Test;

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
      Object value = entry.getValue();
      if (value instanceof String) {
        try {
          entry.setValue(Long.parseLong((String) value));
        } catch (NumberFormatException ignore) {
          continue;
        }
      } else if (value instanceof Number) {
        entry.setValue(value.toString());
      } else {
        continue;
      }
      json = jsonObject.encode();
      checker = visitor.checker();
      JsonReader.parse(json, messageType, checker);
      assertTrue(checker.isEmpty());
    }

    String encoded = JsonWriter.encode(visitor::apply).toString();
    Message.Builder builder = ((Message) expected).newBuilderForType();
    JsonFormat.parser().merge(encoded, builder);
    assertEquals(expected, builder.build());
  }

  @Test
  public void testNullValue() throws Exception {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(SCALAR_TYPES);
    visitor.destroy();
    DataTypesProto.ScalarTypes b = DataTypesProto.ScalarTypes.newBuilder()
      .setInt32(1)
      .setUint32(1)
      .setSint32(1)
      .setInt64(1)
      .setUint64(1)
      .setSint64(1)
      .setBool(true)
      .setFixed32(1)
      .setSfixed32(1)
      .setFloat(1)
      .setFixed64(1)
      .setSfixed64(1)
      .setDouble(1)
      .setString("s")
      .setBytes(ByteString.copyFromUtf8("s"))
      .build();
    JsonObject json = new JsonObject(JsonFormat.printer().print(b));
    for (Map.Entry<String, Object> entry : json) {
      entry.setValue(null);
    }
    RecordingVisitor.Checker checker = visitor.checker();
    JsonReader.parse(json.toString(), SCALAR_TYPES, checker);
    assertTrue(checker.isEmpty());
  }

  @Test
  public void testParseDefaultValue() {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(SCALAR_TYPES);
    visitor.visitBool(BOOL, false);
    visitor.destroy();
    RecordingVisitor.Checker checker = visitor.checker();
    JsonReader.parse("{\"bool\":false}", SCALAR_TYPES, checker);
    assertTrue(checker.isEmpty());
  }

  @Test
  public void testInt32QuotedExponentNotation() {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(SCALAR_TYPES);
    visitor.visitInt32(INT32, 500);
    visitor.destroy();
    RecordingVisitor.Checker checker = visitor.checker();
    JsonReader.parse("{\"int32\":\"5e2\"}", SCALAR_TYPES, checker);
    assertTrue(checker.isEmpty());
  }

  @Test
  public void testUInt32QuotedExponentNotation() {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(SCALAR_TYPES);
    visitor.visitUInt32(UINT32, 500);
    visitor.destroy();
    RecordingVisitor.Checker checker = visitor.checker();
    JsonReader.parse("{\"uint32\":\"5e2\"}", SCALAR_TYPES, checker);
    assertTrue(checker.isEmpty());
  }

  @Test
  public void testUInt64() {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(SCALAR_TYPES);
    visitor.visitUInt64(UINT64, -2048);
    visitor.destroy();
    RecordingVisitor.Checker checker = visitor.checker();
    JsonReader.parse("{\"uint64\":\"18446744073709549568\"}", SCALAR_TYPES, checker);
    assertTrue(checker.isEmpty());
  }
}
