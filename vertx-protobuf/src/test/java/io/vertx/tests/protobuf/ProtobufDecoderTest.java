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
 }
}
