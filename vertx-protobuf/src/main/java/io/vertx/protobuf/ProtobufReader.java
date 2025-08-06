package io.vertx.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.protobuf.schema.EnumType;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.ScalarType;
import io.vertx.protobuf.schema.TypeID;
import io.vertx.protobuf.schema.WireType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
      case ENUM:
      case UINT32:
      case INT32:
        assertTrue(decoder.readVarInt32());
        visitor.visitVarInt32(field, decoder.intValue());
        break;
      case SINT64:
      case INT64:
      case UINT64:
      case BOOL:
        assertTrue(decoder.readVarInt64());
        visitor.visitVarInt64(field, decoder.longValue());
        break;
      default:
        throw new UnsupportedOperationException("" + field.type());
    }
  }

  private static void parseUnknownLen(ProtobufDecoder decoder, MessageType messageType, int fieldNumber, RecordVisitor unknownFieldHandler) {
    assertTrue(decoder.readVarInt32());
    int len = decoder.intValue();
    byte[] data = decoder.readBytes(len);
    Field field = messageType.unknownField(fieldNumber, WireType.LEN);
    unknownFieldHandler.enter(field);
    unknownFieldHandler.visitBytes(field, data);
    unknownFieldHandler.leave(field);
  }

  private static void parseUnknownI32(ProtobufDecoder decoder, MessageType messageType, int fieldNumber, RecordVisitor unknownFieldHandler) {
    assertTrue(decoder.readI32());
    int v = decoder.intValue();
    unknownFieldHandler.visitI32(messageType.unknownField(fieldNumber, WireType.I32), v);
  }

  private static void parseUnknownI64(ProtobufDecoder decoder, MessageType messageType, int fieldNumber, RecordVisitor unknownFieldHandler) {
    assertTrue(decoder.readI64());
    long v = decoder.longValue();
    unknownFieldHandler.visitFixed64(messageType.unknownField(fieldNumber, WireType.I64), v);
  }

  private static void parseUnknownVarInt(ProtobufDecoder decoder, MessageType messageType, int fieldNumber, RecordVisitor unknownFieldHandler) {
    assertTrue(decoder.readVarInt64());
    long v = decoder.longValue();
    unknownFieldHandler.visitVarInt64(messageType.unknownField(fieldNumber, WireType.VARINT), v);
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

  private void checkCumulation(ParsingContext ctx, ProtobufDecoder decoder, MessageType type, RecordVisitor visitor) {
    int from = decoder.index();
    int to = decoder.len();
    for (Map.Entry<Integer, List<Region>> cumulation : ctx.cumulations.entrySet()) {
      Field f = type.field(cumulation.getKey());
      visitor.enterRepetition(f);
      for (Region l : cumulation.getValue()) {
        decoder.index(l.from);
        decoder.len(l.to);
        foo(decoder, f.type().wireType(), f, visitor);
      }
      visitor.leaveRepetition(f);
    }
    decoder.index(from);
    decoder.len(to);
  }

  private void parseLen(ProtobufDecoder decoder, Field field, RecordVisitor visitor) {
    assertTrue(decoder.readVarInt32());
    int len = decoder.intValue();
    if (field.type() instanceof MessageType) {
      int to = decoder.len();
      decoder.len(decoder.index() + len);
      depth++;
      visitor.enter(field);
      MessageType messageType = (MessageType) field.type();
      parse(decoder, messageType, visitor);
      ParsingContext ctx = contexts[depth];
      contexts[depth] = null;
      depth--;
      if (ctx != null) {
        checkCumulation(ctx, decoder, messageType, visitor);
      }
      decoder.len(to);
      visitor.leave(field);
    } else if (field.type() instanceof EnumType) {
      visitor.enterRepetition(field);
      parsePackedVarInt32(decoder, field, len, visitor);
      visitor.leaveRepetition(field);
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
          visitor.enterRepetition(field);
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
          visitor.leaveRepetition(field);
      }
    }
  }

  private void parsePackedVarInt32(ProtobufDecoder decoder, Field field, int len, RecordVisitor visitor) {
    int to = decoder.index() + len;
    while (decoder.index() < to) {
      parseVarInt(decoder, field, visitor);
    }
  }

  private void parsePackedI64(ProtobufDecoder decoder, Field field, int len, RecordVisitor visitor) {
    int to = decoder.index() + len;
    while (decoder.index() < to) {
      assertTrue(decoder.readI64());
      long v = decoder.longValue();
      visitor.visitI64(field, v);
    }
  }

  private void parsePackedI32(ProtobufDecoder decoder, Field field, int len, RecordVisitor visitor) {
    int to = decoder.index() + len;
    while (decoder.index() < to) {
      assertTrue(decoder.readI32());
      int v = decoder.intValue();
      visitor.visitI32(field, v);
    }
  }

  public static void parse(MessageType rootType, RecordVisitor visitor, Buffer buffer) {
    ProtobufReader reader = new ProtobufReader();
    ProtobufDecoder decoder = new ProtobufDecoder(buffer);
    visitor.init(rootType);
    reader.parse(decoder, rootType, visitor);
    ParsingContext ctx = reader.contexts[0];
    if (ctx != null) {
      reader.contexts[0] = null;
      reader.checkCumulation(ctx, decoder, rootType, visitor);
    }
    visitor.destroy();
  }

  private void parse(ProtobufDecoder decoder, MessageType type, RecordVisitor visitor) {
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
        if (field.isRepeated() && !field.isPacked()) {
          ParsingContext ctx = contexts[depth];
          if (ctx == null) {
            ctx = new ParsingContext();
            contexts[depth] = ctx;
          }
          List<Region> cumulation = ctx.cumulations.get(field.number());
          if (cumulation == null) {
            cumulation = new ArrayList<>();
            ctx.cumulations.put(field.number(), cumulation);
          }
          int from;
          Region region;
          switch (wireType) {
            case VARINT:
              from = decoder.index();
              assertTrue(decoder.readVarInt64());
              region = new Region(from, decoder.index());
              break;
            case I32:
              region = new Region(decoder.index(), decoder.index() + 4);
              decoder.skip(4);
              break;
            case I64:
              region = new Region(decoder.index(), decoder.index() + 8);
              decoder.skip(8);
              break;
            case LEN:
              int a = decoder.index();
              assertTrue(decoder.readVarInt32());
              int len = decoder.intValue();
              region = new Region(a, decoder.index() + len);
              decoder.skip(len);
              break;
            default:
              throw new UnsupportedOperationException();
          }
          cumulation.add(region);
        } else {
          foo(decoder, wireType, field, visitor);
        }
      }
    }
  }

  private void foo(ProtobufDecoder decoder, WireType wireType, Field field, RecordVisitor visitor) {
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

  private int depth = 0;
  private ParsingContext[] contexts = new ParsingContext[10];

  private static void assertTrue(boolean cond) {
    if (!cond) {
      throw new DecodeException();
    }
  }
}
