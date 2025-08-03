package io.vertx.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;

public class ProtobufDecoder {

  private final Buffer buffer;
  private int idx;
  private int len;
  private int fieldNumber;
  private int wireType;
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

  public boolean readTag() {
    int c = idx;
    int e = decodeVarInt32();
    // Can be branch-less
    if (idx > c) {
      fieldNumber = e >> 3;
      wireType = e & 0b0111;
      return true;
    } else {
      return false;
    }
  }

  public int fieldNumber() {
    return fieldNumber;
  }

  public int wireType() {
    return wireType;
  }

  public int intValue() {
    return intValue;
  }

  public long longValue() {
    return longValue;
  }

  public boolean readVarInt32() {
    int c = idx;
    intValue = decodeVarInt32();
    return idx > c;
  }

  public boolean readVarInt64() {
    int c = idx;
    longValue = decodeVarInt64();
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

  public int decodeVarInt32() {
    return (int) decodeRawVarInt();
  }

  public long decodeVarInt64() {
    return decodeRawVarInt();
  }

  private long decodeRawVarInt() {
    int i = idx;
    int l = idx + len;
    while (i < l) {
      byte b = buffer.getByte(i);
      i++;
      if ((b & 0x80) == 0) {
        long val = 0;
        int to = idx;
        idx = i;
        int from = idx - 1;
        while (from >= to) {
          val <<= 7;
          val += (buffer.getByte(from--) & 0x7F);
        }
        return val;
      }
    }
    throw new DecodeException();
  }
}
