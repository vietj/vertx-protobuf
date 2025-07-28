package io.vertx.protobuf;

import io.netty.handler.codec.CorruptedFrameException;
import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.schema.WireType;

public class ProtobufDecoder {

  private final Buffer buffer;
  private int idx;
  private int len;
  private int fieldNumber;
  private WireType wireType;
  private int intValue;
  private long longValue;

  public ProtobufDecoder(Buffer buffer) {
    this.buffer = buffer;
    this.idx = 0;
    this.len = buffer.length();
  }

  public int len() {
    return len;
  }

  public ProtobufDecoder len(int len) {
    this.len = len;
    return this;
  }

  public int index() {
    return idx;
  }

  public void skip(int n) {
    idx += n;
  }

  public String readString(int lengthInBytes) {
    String str = buffer.getString(idx, idx + lengthInBytes, "UTF-8");
    idx += lengthInBytes;
    return str;
  }

  public byte[] readBytes(int lengthInBytes) {
    byte[] str = buffer.getBytes(idx, idx + lengthInBytes);
    idx += lengthInBytes;
    return str;
  }

  private static final WireType[] wireTypes = {
    WireType.VARINT,
    WireType.I64,
    WireType.LEN,
    null,
    null,
    WireType.I32,
    null,
    null,
    null
  };

  public boolean readTag() {
    int c = idx;
    int e = readRawVarint32();
    // Can be branch-less
    if (idx > c) {
      fieldNumber = e >> 3;
      wireType = wireTypes[e & 0b0111];
      return true;
    } else {
      return false;
    }
  }

  public int fieldNumber() {
    return fieldNumber;
  }

  public WireType wireType() {
    return wireType;
  }

  public int intValue() {
    return intValue;
  }

  public long longValue() {
    return longValue;
  }

  public boolean readVarInt() {
    int c = idx;
    intValue = readRawVarint32();
    return idx > c;
  }

  public boolean readI32() {
    int l = buffer.getIntLE(idx);
    idx += 4;
    intValue = l;
    return true;
  }

  public boolean readI64() {
    long l = buffer.getLongLE(idx);
    idx += 8;
    longValue = l;
    return true;
  }

  private int readableBytes() {
    return len - idx;
  }

  public boolean isReadable() {
    return idx < len;
  }

  /**
   * Reads variable length 32bit int from buffer
   *
   * @return decoded int if buffers readerIndex has been forwarded else nonsense value
   */
  public int readRawVarint32() {
    if (readableBytes() < 4) {
      return readRawVarint24();
    }
    int wholeOrMore = buffer.getIntLE(idx);
    int firstOneOnStop = ~wholeOrMore & 0x80808080;
    if (firstOneOnStop == 0) {
      return readRawVarint40(wholeOrMore);
    }
    int bitsToKeep = Integer.numberOfTrailingZeros(firstOneOnStop) + 1;
    idx += bitsToKeep >> 3;
    int thisVarintMask = firstOneOnStop ^ (firstOneOnStop - 1);
    int wholeWithContinuations = wholeOrMore & thisVarintMask;
    // mix them up as per varint spec while dropping the continuation bits:
    // 0x7F007F isolate the first byte and the third byte dropping the continuation bits
    // 0x7F007F00 isolate the second byte and the fourth byte dropping the continuation bits
    // the second and fourth byte are shifted to the right by 1, filling the gaps left by the first and third byte
    // it means that the first and second bytes now occupy the first 14 bits (7 bits each)
    // and the third and fourth bytes occupy the next 14 bits (7 bits each), with a gap between the 2s of 2 bytes
    // and another gap of 2 bytes after the forth and third.
    wholeWithContinuations = (wholeWithContinuations & 0x7F007F) | ((wholeWithContinuations & 0x7F007F00) >> 1);
    // 0x3FFF isolate the first 14 bits i.e. the first and second bytes
    // 0x3FFF0000 isolate the next 14 bits i.e. the third and forth bytes
    // the third and forth bytes are shifted to the right by 2, filling the gaps left by the first and second bytes
    return (wholeWithContinuations & 0x3FFF) | ((wholeWithContinuations & 0x3FFF0000) >> 2);
  }

  private int readRawVarint40(int wholeOrMore) {
    byte lastByte;
    if (readableBytes() == 4 || (lastByte = buffer.getByte(idx + 4)) < 0) {
      throw new CorruptedFrameException("malformed varint.");
    }
    idx += 5;
    // add it to wholeOrMore
    return wholeOrMore & 0x7F |
      (((wholeOrMore >> 8) & 0x7F) << 7) |
      (((wholeOrMore >> 16) & 0x7F) << 14) |
      (((wholeOrMore >> 24) & 0x7F) << 21) |
      (lastByte << 28);
  }

  private int readRawVarint24() {
    if (!isReadable()) {
      return 0;
    }
    int mark = idx;

    byte tmp = buffer.getByte(idx++);
    if (tmp >= 0) {
      return tmp;
    }
    int result = tmp & 127;
    if (!isReadable()) {
      idx = mark;
      return 0;
    }
    if ((tmp = buffer.getByte(idx++)) >= 0) {
      return result | tmp << 7;
    }
    result |= (tmp & 127) << 7;
    if (!isReadable()) {
      idx = mark;
      return 0;
    }
    if ((tmp = buffer.getByte(idx++)) >= 0) {
      return result | tmp << 14;
    }
    return result | (tmp & 127) << 14;
  }
}
