package io.vertx.conformance.protobuf;

import com.google.protobuf.util.JsonFormat;
import com.google.protobuf_test_messages.proto3.MessageLiteral;
import com.google.protobuf_test_messages.proto3.ProtoReader;
import com.google.protobuf_test_messages.proto3.ProtoWriter;
import com.google.protobuf_test_messages.proto3.TestAllTypesProto3;
import com.google.protobuf_test_messages.proto3.TestMessagesProto3;
import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.protobuf.json.JsonReader;
import io.vertx.protobuf.well_known_types.FieldLiteral;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ConformanceTest {

  @Test
  public void testJsonInput() throws Exception {

    String json = "{\n" +
      "        \"optionalInt32\": null,\n" +
      "        \"optionalInt64\": null,\n" +
      "        \"optionalUint32\": null,\n" +
      "        \"optionalUint64\": null,\n" +
      "        \"optionalSint32\": null,\n" +
      "        \"optionalSint64\": null,\n" +
      "        \"optionalFixed32\": null,\n" +
      "        \"optionalFixed64\": null,\n" +
      "        \"optionalSfixed32\": null,\n" +
      "        \"optionalSfixed64\": null,\n" +
      "        \"optionalFloat\": null,\n" +
      "        \"optionalDouble\": null,\n" +
      "        \"optionalBool\": null,\n" +
      "        \"optionalString\": null,\n" +
      "        \"optionalBytes\": null,\n" +
      "        \"optionalNestedEnum\": null,\n" +
      "        \"optionalNestedMessage\": null,\n" +
      "        \"repeatedInt32\": null,\n" +
      "        \"repeatedInt64\": null,\n" +
      "        \"repeatedUint32\": null,\n" +
      "        \"repeatedUint64\": null,\n" +
      "        \"repeatedSint32\": null,\n" +
      "        \"repeatedSint64\": null,\n" +
      "        \"repeatedFixed32\": null,\n" +
      "        \"repeatedFixed64\": null,\n" +
      "        \"repeatedSfixed32\": null,\n" +
      "        \"repeatedSfixed64\": null,\n" +
      "        \"repeatedFloat\": null,\n" +
      "        \"repeatedDouble\": null,\n" +
      "        \"repeatedBool\": null,\n" +
      "        \"repeatedString\": null,\n" +
      "        \"repeatedBytes\": null,\n" +
      "        \"repeatedNestedEnum\": null,\n" +
      "        \"repeatedNestedMessage\": null,\n" +
      "        \"mapInt32Int32\": null,\n" +
      "        \"mapBoolBool\": null,\n" +
      "        \"mapStringNestedMessage\": null\n" +
      "      }";

    TestMessagesProto3.TestAllTypesProto3.Builder builder = TestMessagesProto3.TestAllTypesProto3.newBuilder();
    JsonFormat.parser().merge(json, builder);
    TestMessagesProto3.TestAllTypesProto3 d = builder.build();

    System.out.println(d.getOptionalInt32());

//    System.out.println(d);

    // repeatedUint64

    //    System.out.println("d = " + d);
    ProtoReader reader = new ProtoReader();
    JsonReader.parse(json, MessageLiteral.TestAllTypesProto3, reader);
    TestAllTypesProto3 testMessage = (TestAllTypesProto3) reader.stack.pop();
    System.out.println(testMessage.getOptionalInt32());

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
