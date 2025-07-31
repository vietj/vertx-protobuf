package io.vertx.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.protobuf.schema.EnumType;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.ScalarType;
import io.vertx.protobuf.schema.TypeID;
import io.vertx.protobuf.schema.WireType;

public class ProtobufReader {

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

  private static void parseI64(ProtobufDecoder decoder, Field field, Visitor visitor) {
    assertTrue(decoder.readI64());
    long v = decoder.longValue();
    visitor.visitI64(field, v);
  }

  private static void parseI32(ProtobufDecoder decoder, Field field, Visitor visitor) {
    assertTrue(decoder.readI32());
    int v = decoder.intValue();
    visitor.visitI32(field, v);
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
    int value = decoder.intValue();
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
    int len = decoder.intValue();
    if (field.type instanceof MessageType) {
      int to = decoder.len();
      decoder.len(decoder.index() + len);
      visitor.enter(field);
      parse(decoder, (MessageType) field.type, visitor);
      visitor.leave(field);
      decoder.len(to);
    } else if (field.type instanceof EnumType) {
      parsePackedVarInt32(decoder, field, len, visitor);
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
          // Packed
          switch (builtInType.wireType()) {
            case VARINT:
              parsePackedVarInt32(decoder, field, len, visitor);
              break;
            case I64:
              parsePackedI64(decoder, field, len, visitor);
              break;
            case I32:
              parsePackedI32(decoder, field, len, visitor);
              break;
            default:
              throw new UnsupportedOperationException("" + field.type);
          }
      }
    }
  }

  private static void parsePackedVarInt32(ProtobufDecoder decoder, Field field, int len, Visitor visitor) {
    int to = decoder.index() + len;
    while (decoder.index() < to) {
      assertTrue(decoder.readVarInt());
      int v = decoder.intValue();
      if (field.type.id() == TypeID.SINT32) {
        v = decodeSint32(v);
      } else if (field.type.id() == TypeID.SINT64) {
        v = (int)decodeSint64((long)v);
      }
      visitor.visitVarInt32(field, v);
    }
  }

  private static void parsePackedI64(ProtobufDecoder decoder, Field field, int len, Visitor visitor) {
    int to = decoder.index() + len;
    while (decoder.index() < to) {
      assertTrue(decoder.readI64());
      long v = decoder.longValue();
      visitor.visitI64(field, v);
    }
  }

  private static void parsePackedI32(ProtobufDecoder decoder, Field field, int len, Visitor visitor) {
    int to = decoder.index() + len;
    while (decoder.index() < to) {
      assertTrue(decoder.readI32());
      int v = decoder.intValue();
      visitor.visitI32(field, v);
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
        throw new DecodeException("Unknown field  " + fieldNumber + " for message " + type.name());
      }
      int decodedWireType = decoder.wireType();
      WireType wireType = wireTypes[decodedWireType];
      if (wireType == null) {
        throw new DecodeException("Invalid wire type: " + decodedWireType);
      }
      switch (wireType) {
        case LEN:
          parseLen(decoder, field, visitor);
          break;
        case I64:
          parseI64(decoder, field, visitor);
          break;
        case I32:
          parseI32(decoder, field, visitor);
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
