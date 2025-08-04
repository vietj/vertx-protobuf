package io.vertx.conformance.protobuf;

import com.google.protobuf.conformance.Conformance;
import com.google.protobuf_test_messages.proto3.TestMessagesProto3;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.protobuf.com.google.protobuf_test_messages.proto3.ProtoReader;
import io.vertx.protobuf.com.google.protobuf_test_messages.proto3.ProtoWriter;
import io.vertx.protobuf.com.google.protobuf_test_messages.proto3.SchemaLiterals;
import io.vertx.protobuf.com.google.protobuf_test_messages.proto3.TestAllTypesProto3;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ConformanceTest {

  @Test
  public void testConformance() throws Exception {
    byte[] bytes ={ -126, 7, 9, 18, 7, 8, 1, 16, 1, -56, 5, 1, -126, 7, 7, 18, 5, 16, 1, -56, 5, 1 };

    ProtoReader reader = new ProtoReader();
    Buffer buffer = Buffer.buffer(bytes);
    TestMessagesProto3.TestAllTypesProto3 d = TestMessagesProto3.TestAllTypesProto3.parseFrom(bytes);

    System.out.println(d);

    byte[] expected = d.toByteArray();
//    System.out.println(d);

    // repeatedUint64

    //    System.out.println("d = " + d);
    ProtobufReader.parse(SchemaLiterals.MessageLiteral.TestAllTypesProto3, reader, buffer);
    TestAllTypesProto3 testMessage = (TestAllTypesProto3) reader.stack.pop();

    TestAllTypesProto3 corec = testMessage.getOneof_field().asOneofNestedMessage().get().getCorecursive();
    System.out.println(corec.getOptionalInt32());
    System.out.println(corec.getOptionalInt64());
    System.out.println(corec.getUnpackedInt32());

//    Buffer result = ProtobufWriter.encode(visitor -> {
//      ProtoWriter.emit(testMessage, visitor);
//    });
//    assertArrayEquals(expected, actual);
  }
}
