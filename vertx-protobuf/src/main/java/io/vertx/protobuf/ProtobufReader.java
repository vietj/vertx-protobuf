package io.vertx.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.ScalarType;

public class ProtobufReader {

  private static void parseFixed(ProtobufDecoder decoder, Field field, Visitor visitor) {
    ScalarType bt = (ScalarType) field.type;
    switch (bt.id()) {
      case DOUBLE:
        assertTrue(decoder.readDouble());
        double d = decoder.doubleValue();
        visitor.visitDouble(field, d);
        break;
      default:
        throw new UnsupportedOperationException();
    }
  }

  private static int decodeSint32(int value) {
    if ((value & 1) == 0) {
      return value >> 1;
    } else {
      return (value + 1) / -2;
    }
  }

  private static long decodeSint64(long value) {
    if ((value & 1) == 0) {
      return value >> 1;
    } else {
      return (value + 1) / -2;
    }
  }

  private static void parseVarInt(ProtobufDecoder decoder, Field field, Visitor visitor) {
    assertTrue(decoder.readVarInt());
    int value = decoder.int32Value();
    switch (field.type.id()) {
      case SINT32:
        value = decodeSint32(value);
      case BOOL:
      case ENUM:
      case UINT32:
      case INT32:
        visitor.visitVarInt32(field, value);
        break;
      case SINT64:
        value = decodeSint32(value);
      case INT64:
      case UINT64:
        visitor.visitVarInt64(field, value);
        break;
      default:
        throw new UnsupportedOperationException("" + field.type);
    }
  }

  private static void parseLen(ProtobufDecoder decoder, Field field, Visitor visitor) {
    assertTrue(decoder.readVarInt());
    int len = decoder.int32Value();
    if (field.type instanceof MessageType) {
      int to = decoder.len();
      decoder.len(decoder.index() + len);
      visitor.enter(field);
      parse(decoder, (MessageType) field.type, visitor);
      visitor.leave(field);
      decoder.len(to);
    } else {
      ScalarType builtInType = (ScalarType) field.type;
      switch (builtInType.id()) {
        case STRING:
          String s = decoder.readString(len);
          visitor.visitString(field, s);
          break;
        case BYTES:
          byte[] bytes = decoder.readBytes(len);
          visitor.visitBytes(field, bytes);
          break;
        default:
          throw new UnsupportedOperationException("" + field.type);
      }
    }
  }

  public static void parse(MessageType rootType, Visitor visitor, Buffer buffer) {
    ProtobufDecoder decoder = new ProtobufDecoder(buffer);
    visitor.init(rootType);
    parse(decoder, rootType, visitor);
    visitor.destroy();
  }

  private static void parse(ProtobufDecoder decoder, MessageType type, Visitor visitor) {
    while (decoder.isReadable()) {
      assertTrue(decoder.readTag());
      int fieldNumber  = decoder.fieldNumber();
      Field field = type.field(fieldNumber);
      if (field == null) {
        throw new DecodeException();
      }
      switch (field.type.wireType()) {
        case LEN:
          parseLen(decoder, field, visitor);
          break;
        case I64:
          parseFixed(decoder, field, visitor);
          break;
        case VARINT:
          parseVarInt(decoder, field, visitor);
          break;
        default:
          throw new UnsupportedOperationException("Implement me " + field.type.wireType());
      }
    }
  }

  private static void assertTrue(boolean cond) {
    if (!cond) {
      throw new DecodeException();
    }
  }
}
