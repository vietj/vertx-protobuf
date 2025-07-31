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

 @Test
  public void testSome() {
   byte[] data = {
     -71, 96,
     -71, -32, -128, 0,
     -71, -32,
     -128, -128, -128, -128, -128, -128, 0, -1, -1, -1, -1, 7, -128, -128, -128, -128, -8, -1, -1, -1, -1, 1, -128, -128, -128, -128, 32, -1, -1, -1, -1, 31, -1, -1, -1, -1, -1, -1, -1, -1, 127, -127, -128, -128, -128, -128, -128, -128, -128, -128, 1 };
   ProtobufDecoder decoder = new ProtobufDecoder(Buffer.buffer(data));
   int[] expected = { 12345, 12345, 12345, 2147483647, -2147483648, 0, -1, -1, 1 };
   for (int i = 0;i < expected.length;i++) {
     assertTrue(decoder.readVarInt());
     assertEquals("Not same at " + i, expected[i], decoder.intValue());
   }


   // 10111001
   // 01100000

   // 0111001_1100000

 }

}
