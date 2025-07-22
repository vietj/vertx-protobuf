package io.vertx.protobuf;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.internal.buffer.BufferInternal;

import java.nio.charset.StandardCharsets;

public class ProtobufEncoder {

  private final Buffer buffer;

  public ProtobufEncoder(Buffer buffer) {
    this.buffer = buffer;
  }

  public int length() {
    return buffer.length();
  }

  public ProtobufEncoder writeTag(int fieldNumber, int wireType) {
    int tag = fieldNumber << 3 | (wireType & 0x03);
    writeRawVarint32(buffer, tag);
    return this;
  }

  public ProtobufEncoder writeVarInt32(int v) {
    writeRawVarint32(buffer, v);
    return this;
  }

  public ProtobufEncoder writeDouble(double d) {
    long l = Double.doubleToRawLongBits(d);
    buffer.appendLongLE(l);
    return this;
  }

  public ProtobufEncoder writeString(String s) {
    ByteBuf bbuf = ((BufferInternal) buffer).unwrap();
    bbuf.writeCharSequence(s, StandardCharsets.UTF_8);
    return this;
  }

  public ProtobufEncoder writeBytes(byte[] bytes) {
    buffer.appendBytes(bytes);
    return this;
  }

//  public ProtoEncoder write

  /**
   * Writes protobuf varint32 to (@link ByteBuf).
   * @param out to be written to
   * @param value to be written
   */
  static void writeRawVarint32(Buffer out, int value) {
    while (true) {
      if ((value & ~0x7F) == 0) {
        out.appendByte((byte) value);
        return;
      } else {
        out.appendByte((byte) ((value & 0x7F) | 0x80));
        value >>>= 7;
      }
    }
  }

  /**
   * Computes size of protobuf varint32 after encoding.
   * @param value which is to be encoded.
   * @return size of value encoded as protobuf varint32.
   */
  public static int computeRawVarint32Size(final int value) {
    if ((value & (0xffffffff <<  7)) == 0) {
      return 1;
    }
    if ((value & (0xffffffff << 14)) == 0) {
      return 2;
    }
    if ((value & (0xffffffff << 21)) == 0) {
      return 3;
    }
    if ((value & (0xffffffff << 28)) == 0) {
      return 4;
    }
    return 5;
  }
}
