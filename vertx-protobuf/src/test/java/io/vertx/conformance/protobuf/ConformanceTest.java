package io.vertx.conformance.protobuf;

import com.google.protobuf.Any;
import com.google.protobuf.Struct;
import com.google.protobuf.TypeRegistry;
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
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

public class ConformanceTest {

  private TypeRegistry typeRegistry;

  public ConformanceTest() {
    typeRegistry =
      TypeRegistry.newBuilder()
        .add(TestMessagesProto3.TestAllTypesProto3.getDescriptor())
        .add(com.google.protobuf_test_messages.proto3.TestMessagesProto3.TestAllTypesProto3.getDescriptor())
        .build();
  }

  @Test
  public void testJsonInput() throws Exception {

    String json = "{\n" +
      "        \"optionalAny\": {\n" +
      "          \"@type\": \"type.googleapis.com/google.protobuf.Struct\",\n" +
      "          \"value\": {\n" +
      "            \"foo\": 1\n" +
      "    }\n" +
      "  }\n" +
      "      }";

/*
    json = "{\n" +
      "        \"optionalAny\": {\n" +
      "          \"@type\": \"type.googleapis.com/protobuf_test_messages.proto3.TestAllTypesProto3\",\n" +
      "          \"optionalInt32\": 12345\n" +
      "  }\n" +
      "      }";
*/

    TestMessagesProto3.TestAllTypesProto3.Builder builder = TestMessagesProto3.TestAllTypesProto3.newBuilder();
    JsonFormat.parser().usingTypeRegistry(typeRegistry).merge(json, builder);
    TestMessagesProto3.TestAllTypesProto3 d = builder.build();

    Any any = d.getOptionalAny();
    System.out.println(any.getTypeUrl());
    System.out.println(any.getValue());
    Struct struct = any.unpack(Struct.class);
//    TestMessagesProto3.TestAllTypesProto3 struct = any.unpack(TestMessagesProto3.TestAllTypesProto3.class);
//    System.out.println("struct = " + struct);
//    any.

//    ProtoReader reader = new ProtoReader();
//    JsonReader.parse(json, MessageLiteral.TestAllTypesProto3, reader);
//    TestAllTypesProto3 testMessage = (TestAllTypesProto3) reader.stack.pop();
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
