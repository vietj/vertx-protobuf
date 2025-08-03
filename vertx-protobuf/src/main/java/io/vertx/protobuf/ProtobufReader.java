package io.vertx.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.protobuf.schema.EnumType;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.ScalarType;
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

  private static void parseI64(ProtobufDecoder decoder, Field field, RecordVisitor visitor) {
    assertTrue(decoder.readI64());
    long v = decoder.longValue();
    visitor.visitI64(field, v);
  }

  private static void parseI32(ProtobufDecoder decoder, Field field, RecordVisitor visitor) {
    assertTrue(decoder.readI32());
    int v = decoder.intValue();
    visitor.visitI32(field, v);
  }

  public static int decodeSInt32(int value) {
    return (value >>> 1) ^ - (value & 1);
  }

  public static long decodeSInt64(long value) {
    return (value >>> 1) ^ - (value & 1);
  }

  private static void parseVarInt(ProtobufDecoder decoder, Field field, RecordVisitor visitor) {
    switch (field.type().id()) {
      case SINT32:
      case BOOL:
      case ENUM:
      case UINT32:
      case INT32:
        assertTrue(decoder.readVarInt32());
        visitor.visitVarInt32(field, decoder.intValue());
        break;
      case SINT64:
      case INT64:
      case UINT64:
        assertTrue(decoder.readVarInt64());
        visitor.visitVarInt64(field, decoder.longValue());
        break;
      default:
        throw new UnsupportedOperationException("" + field.type());
    }
  }

  private static void parseLen(ProtobufDecoder decoder, MessageType messageType, int fieldNumber, UnknownRecordVisitor unknownFieldHandler) {
    assertTrue(decoder.readVarInt32());
    int len = decoder.intValue();
    byte[] data = decoder.readBytes(len);
    unknownFieldHandler.visitUnknownLengthDelimited(messageType, fieldNumber, Buffer.buffer(data));
    decoder.skip(len);
  }

  private static void parseI32(ProtobufDecoder decoder, MessageType messageType, int fieldNumber, UnknownRecordVisitor unknownFieldHandler) {
    assertTrue(decoder.readI32());
    int v = decoder.intValue();
    unknownFieldHandler.visitUnknownI32(messageType, fieldNumber, v);
  }

  private static void parseI64(ProtobufDecoder decoder, MessageType messageType, int fieldNumber, UnknownRecordVisitor unknownFieldHandler) {
    assertTrue(decoder.readI64());
    long v = decoder.longValue();
    unknownFieldHandler.visitUnknownI64(messageType, fieldNumber, v);
  }

  private static void parseVarInt(ProtobufDecoder decoder, MessageType messageType, int fieldNumber, UnknownRecordVisitor unknownFieldHandler) {
    assertTrue(decoder.readVarInt64());
    long v = decoder.longValue();
    unknownFieldHandler.visitUnknownVarInt(messageType, fieldNumber, v);
  }

  private static void parseLen(ProtobufDecoder decoder, Field field, RecordVisitor visitor, UnknownRecordVisitor unknownFieldHandler) {
    assertTrue(decoder.readVarInt32());
    int len = decoder.intValue();
    if (field.type() instanceof MessageType) {
      int to = decoder.len();
      decoder.len(decoder.index() + len);
      visitor.enter(field);
      parse(decoder, (MessageType) field.type(), visitor, unknownFieldHandler);
      visitor.leave(field);
      decoder.len(to);
    } else if (field.type() instanceof EnumType) {
      visitor.enter(field);
      parsePackedVarInt32(decoder, field, len, visitor);
      visitor.leave(field);
    } else {
      ScalarType builtInType = (ScalarType) field.type();
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
          visitor.enter(field);
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
              throw new UnsupportedOperationException("" + field.type());
          }
          visitor.leave(field);
      }
    }
  }

  private static void parsePackedVarInt32(ProtobufDecoder decoder, Field field, int len, RecordVisitor visitor) {
    int to = decoder.index() + len;
    while (decoder.index() < to) {
      parseVarInt(decoder, field, visitor);
    }
  }

  private static void parsePackedI64(ProtobufDecoder decoder, Field field, int len, RecordVisitor visitor) {
    int to = decoder.index() + len;
    while (decoder.index() < to) {
      assertTrue(decoder.readI64());
      long v = decoder.longValue();
      visitor.visitI64(field, v);
    }
  }

  private static void parsePackedI32(ProtobufDecoder decoder, Field field, int len, RecordVisitor visitor) {
    int to = decoder.index() + len;
    while (decoder.index() < to) {
      assertTrue(decoder.readI32());
      int v = decoder.intValue();
      visitor.visitI32(field, v);
    }
  }

  public static void parse(MessageType rootType, RecordVisitor visitor, Buffer buffer) {
    parse(rootType, visitor, new UnknownRecordVisitor() {
    }, buffer);
  }

  public static void parse(MessageType rootType, RecordVisitor visitor, UnknownRecordVisitor unknownFieldHandler, Buffer buffer) {
    ProtobufDecoder decoder = new ProtobufDecoder(buffer);
    visitor.init(rootType);
    parse(decoder, rootType, visitor, unknownFieldHandler);
    visitor.destroy();
  }

  private static void parse(ProtobufDecoder decoder, MessageType type, RecordVisitor visitor, UnknownRecordVisitor unknownFieldHandler) {
    while (decoder.isReadable()) {
      assertTrue(decoder.readTag());
      int fieldNumber  = decoder.fieldNumber();
      int decodedWireType = decoder.wireType();
      Field field = type.field(fieldNumber);
      WireType wireType = wireTypes[decodedWireType];
      if (wireType == null) {
        throw new DecodeException("Invalid wire type: " + decodedWireType);
      }
      if (field == null) {
        switch (wireType) {
          case LEN:
            parseLen(decoder, type, fieldNumber, unknownFieldHandler);
            break;
          case I32:
            parseI32(decoder, type, fieldNumber, unknownFieldHandler);
            break;
          case I64:
            parseI64(decoder, type, fieldNumber, unknownFieldHandler);
            break;
          case VARINT:
            parseVarInt(decoder, type, fieldNumber, unknownFieldHandler);
            break;
          default:
            throw new UnsupportedOperationException("Todo");
        }
      } else {
        switch (wireType) {
          case LEN:
            parseLen(decoder, field, visitor, unknownFieldHandler);
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
            throw new UnsupportedOperationException("Implement me " + field.type().wireType());
        }
      }
      }
  }

  private static void assertTrue(boolean cond) {
    if (!cond) {
      throw new DecodeException();
    }
  }
}
