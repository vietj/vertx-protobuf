package io.vertx.tests.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufDecoder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProtobufDecoderTest {

  @Test
  public void testReadOversizedVarInt() {
    byte[] data = { -1, -1, -1, -1, -1, -1, -1, -1, 127 };
    long expected = 0b1111111_1111111_1111111_1111111_1111111_1111111_1111111_1111111_1111111L;
    ProtobufDecoder decoder = new ProtobufDecoder(Buffer.buffer(data));
    assertTrue(decoder.readVarInt());
    assertEquals((int)expected, decoder.intValue());

    // WARNING, test=Recommended.Proto3.ProtobufInput.ValidDataScalarBinary.INT32[8].ProtobufOutput:
    // Output was not equivalent to reference message:
    // Expect:
    // \010\377\377\377\377\377\377\377\377\377\001, but got:
    // \010\377\377\377\377\017\250\001\000\260\001\000\270\001\000\230\023\000
    // request=goo.gle/debugstr
    // protobuf_payload: "\010\377\377\377\377\377\377\377\377\177"
    // requested_output_format: PROTOBUF message_type: "protobuf_test_messages.proto3.TestAllTypesProto3" test_category:
    // BINARY_TEST, response=goo.gle/debugstr   protobuf_payload:
    // "\010\377\377\377\377\017\250\001\000\260\001\000\270\001\000\230\023\000"

  }
}
