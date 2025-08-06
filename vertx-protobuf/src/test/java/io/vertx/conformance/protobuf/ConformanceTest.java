package io.vertx.conformance.protobuf;

import com.google.protobuf_test_messages.proto3.TestMessagesProto3;
import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.protobuf.com.google.protobuf_test_messages.proto3.MessageLiteral;
import io.vertx.protobuf.com.google.protobuf_test_messages.proto3.ProtoReader;
import io.vertx.protobuf.com.google.protobuf_test_messages.proto3.ProtoWriter;
import io.vertx.protobuf.com.google.protobuf_test_messages.proto3.TestAllTypesProto3;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ConformanceTest {

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
