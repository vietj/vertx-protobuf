package io.vertx.tests.protobuf.json;

import com.google.protobuf.ByteString;
import com.google.protobuf.MapEntry;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.protobuf.json.JsonReader;
import io.vertx.protobuf.json.JsonWriter;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.ScalarType;
import io.vertx.protobuf.schema.WireType;
import io.vertx.tests.protobuf.DataTypeTestBase;
import io.vertx.tests.protobuf.RecordingVisitor;
import io.vertx.tests.protobuf.datatypes.DataTypesProto;
import io.vertx.tests.protobuf.datatypes.EnumTypes;
import io.vertx.tests.protobuf.datatypes.Enumerated;
import io.vertx.tests.protobuf.datatypes.MessageLiteral;
import io.vertx.tests.protobuf.datatypes.ProtoReader;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.*;

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
        }
      } else if (value instanceof Number) {
        entry.setValue(value.toString());
      } else {
      }
    }
    json = jsonObject.encode();
    checker = visitor.checker();
    JsonReader.parse(json, messageType, checker);
    assertTrue(checker.isEmpty());

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
  public void testUInt32() {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(SCALAR_TYPES);
    visitor.visitUInt32(UINT32, -1);
    visitor.destroy();
    RecordingVisitor.Checker checker = visitor.checker();
    JsonReader.parse("{\"uint32\":\"" + BigInteger.valueOf(0xFFFFFFFFL) + "\"}", SCALAR_TYPES, checker);
    assertTrue(checker.isEmpty());
    assertEquals(BigInteger.valueOf(0xFFFFFFFFL).toString(), JsonWriter.encode(visitor::apply).getValue("uint32"));
  }

  @Test
  public void testFixed32() {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(SCALAR_TYPES);
    visitor.visitFixed32(FIXED32, -1);
    visitor.destroy();
    RecordingVisitor.Checker checker = visitor.checker();
    JsonReader.parse("{\"fixed32\":\"" + BigInteger.valueOf(0xFFFFFFFFL) + "\"}", SCALAR_TYPES, checker);
    assertTrue(checker.isEmpty());
    assertEquals(4294967295L, JsonWriter.encode(visitor::apply).getValue("fixed32"));
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
    assertEquals("18446744073709549568", JsonWriter.encode(visitor::apply).getValue("uint64"));
  }

  @Test
  public void testFixed64() {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(SCALAR_TYPES);
    visitor.visitFixed64(FIXED64, -1);
    visitor.destroy();
    RecordingVisitor.Checker checker = visitor.checker();
    JsonReader.parse("{\"fixed64\":\"18446744073709551615\"}", SCALAR_TYPES, checker);
    assertTrue(checker.isEmpty());
    assertEquals("18446744073709551615", JsonWriter.encode(visitor::apply).getValue("fixed64"));
  }

  @Test
  public void testEnumNumber() {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(ENUM_TYPES);
    visitor.visitEnum(ENUM, 1);
    visitor.destroy();
    RecordingVisitor.Checker checker = visitor.checker();
    JsonReader.parse("{\"_enum\":1}", ENUM_TYPES, checker);
    assertTrue(checker.isEmpty());
  }

  // {"optionalDouble": 1.89769e+308}

  @Test
  public void testEmptyNumber() {
    for (Field field : SCALAR_TYPES.fields()) {
      if (field.type() instanceof ScalarType && field.type().wireType() != WireType.LEN) {
        assertDecodeException(field.jsonName(), "\"\"");
      }
    }
  }

  @Test
  public void testDoubleInfinity() {
    for (String infinity : Arrays.asList("Infinity", "-Infinity")) {
      RecordingVisitor visitor = new RecordingVisitor();
      visitor.init(SCALAR_TYPES);
      visitor.visitDouble(DOUBLE, Double.parseDouble(infinity));
      visitor.destroy();
      RecordingVisitor.Checker checker = visitor.checker();
      JsonReader.parse("{\"" + DOUBLE.jsonName() + "\":\"" + infinity + "\"}", SCALAR_TYPES, checker);
      assertTrue(checker.isEmpty());
    }
  }

  @Test
  public void testFloatInfinity() {
    for (String infinity : Arrays.asList("Infinity", "-Infinity")) {
      RecordingVisitor visitor = new RecordingVisitor();
      visitor.init(SCALAR_TYPES);
      visitor.visitFloat(FLOAT, Float.parseFloat(infinity));
      visitor.destroy();
      RecordingVisitor.Checker checker = visitor.checker();
      JsonReader.parse("{\"" + FLOAT.jsonName() + "\":\"" + infinity + "\"}", SCALAR_TYPES, checker);
      assertTrue(checker.isEmpty());
    }
  }

  @Test
  public void testInvalidDouble() {
    assertDecodeException(DOUBLE.jsonName(), "\"1.89769e+308\"");
    assertDecodeException(DOUBLE.jsonName(), "\"-1.89769e+308\"");
    assertDecodeException(DOUBLE.jsonName(), "1.89769e+308");
    assertDecodeException(DOUBLE.jsonName(), "-1.89769e+308");
  }

  @Test
  public void testInvalidFloat() {
    assertDecodeException(FLOAT.jsonName(), "\"3.502823e+38\"");
    assertDecodeException(FLOAT.jsonName(), "\"-3.502823e+38\"");
    assertDecodeException(FLOAT.jsonName(), "3.502823e+38");
    assertDecodeException(FLOAT.jsonName(), "-3.502823e+38");
  }

  @Test
  public void testInvalidInteger() {
    Field[] fields = {
      INT32,
      UINT32,
      SINT32,
      FIXED32,
      SFIXED32,
      INT64,
      UINT64,
      SINT64,
      FIXED64,
      SFIXED64
    };
    for (Field field : fields) {
      assertDecodeException(field.jsonName(), "0.5");
      assertDecodeException(field.jsonName(), "\"0.5\"");
    }
  }

  @Test
  public void testIntegerInteger() {
    Field[] fields = {
      INT32,
      UINT32,
      SINT32,
      FIXED32,
      SFIXED32
    };
    for (Field field : fields) {
      assertDecodeException(field.jsonName(), "4294967296");
      assertDecodeException(field.jsonName(), "\"4294967296\"");
    }
  }

  @Test
  public void testUInt32MaxValue() {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(SCALAR_TYPES);
    visitor.visitUInt32(UINT32, -1);
    visitor.destroy();
    RecordingVisitor.Checker checker = visitor.checker();
    JsonReader.parse("{\"" + UINT32.jsonName() + "\":" + 0xFFFFFFFFL + "}", SCALAR_TYPES, checker);
    assertTrue(checker.isEmpty());
  }

  @Test
  public void testUInt64MaxValue() {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(SCALAR_TYPES);
    visitor.visitUInt64(UINT64, -1);
    visitor.destroy();
    RecordingVisitor.Checker checker = visitor.checker();
    JsonReader.parse("{\"" + UINT64.jsonName() + "\":" + new BigInteger("FFFFFFFFFFFFFFFF", 16) + "}", SCALAR_TYPES, checker);
    assertTrue(checker.isEmpty());
  }

  @Test
  public void testParseExactFloatingValueInt32() {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(SCALAR_TYPES);
    visitor.visitInt32(INT32, 4);
    visitor.destroy();
    RecordingVisitor.Checker checker = visitor.checker();
    JsonReader.parse("{\"" + INT32.jsonName() + "\":4.0}", SCALAR_TYPES, checker);
    assertTrue(checker.isEmpty());
  }

  @Test
  public void testParseExactFloatingValueInt64() {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(SCALAR_TYPES);
    visitor.visitInt64(INT64, 4);
    visitor.destroy();
    RecordingVisitor.Checker checker = visitor.checker();
    JsonReader.parse("{\"" + INT64.jsonName() + "\":4.0}", SCALAR_TYPES, checker);
    assertTrue(checker.isEmpty());
  }

  @Test
  public void testParseExactFloatingValueUInt32() {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(SCALAR_TYPES);
    visitor.visitUInt32(UINT32, 4);
    visitor.destroy();
    RecordingVisitor.Checker checker = visitor.checker();
    JsonReader.parse("{\"" + UINT32.jsonName() + "\":4.0}", SCALAR_TYPES, checker);
    assertTrue(checker.isEmpty());
  }

  @Test
  public void testParseExactFloatingValueUInt64() {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(SCALAR_TYPES);
    visitor.visitUInt64(UINT64, 4);
    visitor.destroy();
    RecordingVisitor.Checker checker = visitor.checker();
    JsonReader.parse("{\"" + UINT64.jsonName() + "\":4.0}", SCALAR_TYPES, checker);
    assertTrue(checker.isEmpty());
  }

  private static void assertDecodeException(String fieldName, String value) {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(SCALAR_TYPES);
    RecordingVisitor.Checker checker = visitor.checker();
    try {
      JsonReader.parse("{\"" + fieldName + "\":" + value + "}", SCALAR_TYPES, checker);
      fail();
    } catch (DecodeException expected) {
    }
    assertTrue(checker.isEmpty());
  }

  @Test
  public void testUnknownEnum() {
    ProtoReader reader = new ProtoReader();
    JsonReader.parse("{\"_enum\":\"unknown\"}", MessageLiteral.EnumTypes, reader);
    EnumTypes c = (EnumTypes) reader.stack.pop();
    Enumerated enumerated = c.getEnum();
    assertTrue(enumerated.isUnknown());
    assertNull(enumerated.asEnum());
    assertNull(enumerated.asEnum());
  }
}
