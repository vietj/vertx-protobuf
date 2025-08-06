package io.vertx.conformance.protobuf;

import com.google.protobuf.util.JsonFormat;
import com.google.protobuf_test_messages.proto3.TestMessagesProto3;
import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.protobuf.com.google.protobuf_test_messages.proto3.MessageLiteral;
import io.vertx.protobuf.com.google.protobuf_test_messages.proto3.ProtoReader;
import io.vertx.protobuf.com.google.protobuf_test_messages.proto3.ProtoWriter;
import io.vertx.protobuf.com.google.protobuf_test_messages.proto3.TestAllTypesProto3;
import io.vertx.protobuf.json.JsonReader;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ConformanceTest {

  @Test
  public void testJsonInput() throws Exception {

    String json = "{\n" +
      "        \"fieldname1\": 1,\n" +
      "        \"field_name2\": 2,\n" +
      "        \"_field_name3\": 3,\n" +
      "        \"field__name4_\": 4,\n" +
      "        \"field0name5\": 5,\n" +
      "        \"field_0_name6\": 6,\n" +
      "        \"fieldName7\": 7,\n" +
      "        \"FieldName8\": 8,\n" +
      "        \"field_Name9\": 9,\n" +
      "        \"Field_Name10\": 10,\n" +
      "        \"FIELD_NAME11\": 11,\n" +
      "        \"FIELD_name12\": 12,\n" +
      "        \"__field_name13\": 13,\n" +
      "        \"__Field_name14\": 14,\n" +
      "        \"field__name15\": 15,\n" +
      "        \"field__Name16\": 16,\n" +
      "        \"field_name17__\": 17,\n" +
      "        \"Field_name18__\": 18\n" +
      "      }";

    TestMessagesProto3.TestAllTypesProto3.Builder builder = TestMessagesProto3.TestAllTypesProto3.newBuilder();
    JsonFormat.parser().merge(json, builder);
    TestMessagesProto3.TestAllTypesProto3 d = builder.build();

    System.out.println();

//    System.out.println(d);

    // repeatedUint64

    //    System.out.println("d = " + d);
    ProtoReader reader = new ProtoReader();
    JsonReader.parse(json, MessageLiteral.TestAllTypesProto3, reader);
    TestAllTypesProto3 testMessage = (TestAllTypesProto3) reader.stack.pop();

  }

  @Test
  public void testConformance() throws Exception {

    // Recommended.Proto3.ProtobufInput.ValidDataRepeated.ENUM.PackedInput.UnpackedOutput.ProtobufOutput
    byte[] bytes ={ 8, -128, -128, -128, -128, -8, -1, -1, -1, -1, 1 };



    // Expected
    // [-48, 41, 123,
    // -48, 41, -56, 3,
    // -46, 41, 3, 97, 98, 99,
    // -46, 41, 3, 100, 101, 102
    // ]

    // Actual
    // [-46, 41, 3, 97, 98, 99,
    // -46, 41, 3, 100, 101, 102,
    // -48, 41, 123,
    // -48, 41, -56, 3]

    // 0
    // 1
    // 2
    // -1
    // -1
    // 1


    ProtoReader reader = new ProtoReader();
    Buffer buffer = Buffer.buffer(bytes);
    TestMessagesProto3.TestAllTypesProto3 d = TestMessagesProto3.TestAllTypesProto3.parseFrom(bytes);

    byte[] expected = d.toByteArray();
//    System.out.println(d);

    // repeatedUint64

    //    System.out.println("d = " + d);
    ProtobufReader.parse(MessageLiteral.TestAllTypesProto3, reader, buffer);
    TestAllTypesProto3 testMessage = (TestAllTypesProto3) reader.stack.pop();
    List<TestAllTypesProto3.NestedEnum> a = testMessage.getUnpackedNestedEnum();

    Buffer result = ProtobufWriter.encode(visitor -> {
      ProtoWriter.emit(testMessage, visitor);
    });
  }
}
