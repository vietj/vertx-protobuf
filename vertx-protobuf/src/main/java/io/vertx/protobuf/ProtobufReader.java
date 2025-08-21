package io.vertx.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.protobuf.schema.EnumType;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.ScalarType;
import io.vertx.protobuf.schema.WireType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

  private static void parseI64(ProtobufDecoder decoder, Field field, ProtoVisitor visitor) {
    assertTrue(decoder.readI64());
    long v = decoder.longValue();
    dispatchI64(field, v, visitor);
  }

  private static void dispatchI64(Field field, long value, ProtoVisitor visitor) {
    switch (field.type().id()) {
      case FIXED64:
        visitor.visitFixed64(field, value);
        break;
      case SFIXED64:
        visitor.visitSFixed64(field, value);
        break;
      case DOUBLE:
        visitor.visitDouble(field, Double.longBitsToDouble(value));
        break;
      default:
        throw new UnsupportedOperationException();
    }
  }

  private static void dispatchI32(Field field, int value, ProtoVisitor visitor) {
    switch (field.type().id()) {
      case FIXED32:
        visitor.visitFixed32(field, value);
        break;
      case SFIXED32:
        visitor.visitSFixed32(field, value);
        break;
      case FLOAT:
        visitor.visitFloat(field, Float.intBitsToFloat(value));
        break;
      default:
        throw new UnsupportedOperationException();
    }
  }

  private static void parseI32(ProtobufDecoder decoder, Field field, ProtoVisitor visitor) {
    assertTrue(decoder.readI32());
    int v = decoder.intValue();
    dispatchI32(field, v, visitor);
  }

  public static int decodeSInt32(int value) {
    return (value >>> 1) ^ - (value & 1);
  }

  public static long decodeSInt64(long value) {
    return (value >>> 1) ^ - (value & 1);
  }

  private static void parseVarInt(ProtobufDecoder decoder, Field field, ProtoVisitor visitor) {
    switch (field.type().id()) {
      case SINT32:
        assertTrue(decoder.readVarInt32());
        visitor.visitSInt32(field, decodeSInt32(decoder.intValue()));
        break;
      case ENUM:
        assertTrue(decoder.readVarInt32());
        visitor.visitEnum(field, decoder.intValue());
        break;
      case UINT32:
        assertTrue(decoder.readVarInt32());
        visitor.visitUInt32(field, decoder.intValue());
        break;
      case INT32:
        assertTrue(decoder.readVarInt32());
        visitor.visitInt32(field, decoder.intValue());
        break;
      case SINT64:
        assertTrue(decoder.readVarInt64());
        visitor.visitSInt64(field, decodeSInt64(decoder.longValue()));
        break;
      case INT64:
        assertTrue(decoder.readVarInt64());
        visitor.visitInt64(field, decoder.longValue());
        break;
      case UINT64:
        assertTrue(decoder.readVarInt64());
        visitor.visitUInt64(field, decoder.longValue());
        break;
      case BOOL:
        assertTrue(decoder.readVarInt64());
        visitor.visitBool(field, decoder.longValue() != 0);
        break;
      default:
        throw new UnsupportedOperationException("" + field.type());
    }
  }

  private static void parseUnknownLen(ProtobufDecoder decoder, MessageType messageType, int fieldNumber, ProtoVisitor unknownFieldHandler) {
    assertTrue(decoder.readVarInt32());
    int len = decoder.intValue();
    byte[] data = decoder.readBytes(len);
    Field field = messageType.unknownField(fieldNumber, WireType.LEN);
    unknownFieldHandler.visitBytes(field, data);
  }

  private static void parseUnknownI32(ProtobufDecoder decoder, MessageType messageType, int fieldNumber, ProtoVisitor unknownFieldHandler) {
    assertTrue(decoder.readI32());
    int v = decoder.intValue();
    unknownFieldHandler.visitFixed32(messageType.unknownField(fieldNumber, WireType.I32), v);
  }

  private static void parseUnknownI64(ProtobufDecoder decoder, MessageType messageType, int fieldNumber, ProtoVisitor unknownFieldHandler) {
    assertTrue(decoder.readI64());
    long v = decoder.longValue();
    unknownFieldHandler.visitFixed64(messageType.unknownField(fieldNumber, WireType.I64), v);
  }

  private static void parseUnknownVarInt(ProtobufDecoder decoder, MessageType messageType, int fieldNumber, ProtoVisitor unknownFieldHandler) {
    assertTrue(decoder.readVarInt64());
    long v = decoder.longValue();
    unknownFieldHandler.visitInt64(messageType.unknownField(fieldNumber, WireType.VARINT), v);
  }

  private static class Region {
    final int from;
    final int to;
    Region(int from, int to) {
      this.from = from;
      this.to = to;
    }
  }

  private static class ParsingContext {
    Map<Integer, List<Region>> cumulations = new LinkedHashMap<>();
  }

  private void parseLen(ProtobufDecoder decoder, Field field, ProtoVisitor visitor) {
    assertTrue(decoder.readVarInt32());
    int len = decoder.intValue();
    if (field.type() instanceof MessageType) {
      int to = decoder.len();
      decoder.len(decoder.index() + len);
      visitor.enter(field);
      MessageType messageType = (MessageType) field.type();
      parse(decoder, messageType, visitor);
      decoder.len(to);
      visitor.leave(field);
    } else if (field.type() instanceof EnumType) {
      visitor.enterPacked(field);
      parsePackedVarInt32(decoder, field, len, visitor);
      visitor.leavePacked(field);
    } else {
      ScalarType builtInType = (ScalarType) field.type();
      switch (builtInType.id()) {
        case STRING:
          String s = decoder.readString(len);
//          visitor.enter(field);
          visitor.visitString(field, s);
//          visitor.leave(field);
          break;
        case BYTES:
          byte[] bytes = decoder.readBytes(len);
//          visitor.enter(field);
          visitor.visitBytes(field, bytes);
//          visitor.leave(field);
          break;
        default:
          // Packed
          visitor.enterPacked(field);
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
          visitor.leavePacked(field);
      }
    }
  }

  private void parsePackedVarInt32(ProtobufDecoder decoder, Field field, int len, ProtoVisitor visitor) {
    int to = decoder.index() + len;
    while (decoder.index() < to) {
      parseVarInt(decoder, field, visitor);
    }
  }

  private void parsePackedI64(ProtobufDecoder decoder, Field field, int len, ProtoVisitor visitor) {
    int to = decoder.index() + len;
    while (decoder.index() < to) {
      assertTrue(decoder.readI64());
      long v = decoder.longValue();
      dispatchI64(field, v, visitor);
    }
  }

  private void parsePackedI32(ProtobufDecoder decoder, Field field, int len, ProtoVisitor visitor) {
    int to = decoder.index() + len;
    while (decoder.index() < to) {
      assertTrue(decoder.readI32());
      int v = decoder.intValue();
      dispatchI32(field, v, visitor);
    }
  }

  public static void parse(MessageType rootType, ProtoVisitor visitor, Buffer buffer) {
    ProtobufReader reader = new ProtobufReader();
    ProtobufDecoder decoder = new ProtobufDecoder(buffer);
    visitor.init(rootType);
    reader.parse(decoder, rootType, visitor);
    visitor.destroy();
  }

  private void parse(ProtobufDecoder decoder, MessageType type, ProtoVisitor visitor) {
    while (decoder.isReadable()) {
      assertTrue(decoder.readTag());
      int fieldNumber  = decoder.fieldNumber();
      if (fieldNumber == 0) {
        throw new DecodeException();
      }
      int decodedWireType = decoder.wireType();
      Field field = type.field(fieldNumber);
      WireType wireType = wireTypes[decodedWireType];
      if (wireType == null) {
        throw new DecodeException("Invalid wire type: " + decodedWireType);
      }
      if (field == null) {
        switch (wireType) {
          case LEN:
            parseUnknownLen(decoder, type, fieldNumber, visitor);
            break;
          case I32:
            parseUnknownI32(decoder, type, fieldNumber, visitor);
            break;
          case I64:
            parseUnknownI64(decoder, type, fieldNumber, visitor);
            break;
          case VARINT:
            parseUnknownVarInt(decoder, type, fieldNumber, visitor);
            break;
          default:
            throw new UnsupportedOperationException("Todo");
        }
      } else {
        foo(decoder, wireType, field, visitor);
      }
    }
  }

  private void foo(ProtobufDecoder decoder, WireType wireType, Field field, ProtoVisitor visitor) {
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
        throw new UnsupportedOperationException("Implement me " + field.type().wireType());
    }
  }

  private static void assertTrue(boolean cond) {
    if (!cond) {
      throw new DecodeException();
    }
  }
}
