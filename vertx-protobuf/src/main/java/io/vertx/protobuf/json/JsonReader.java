package io.vertx.protobuf.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.jackson.JacksonCodec;
import io.vertx.protobuf.RecordVisitor;
import io.vertx.protobuf.schema.EnumType;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.TypeID;
import io.vertx.protobuf.well_known_types.MessageLiteral;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Objects;
import java.util.OptionalInt;

public class JsonReader {

  private static final BigInteger MAX_UINT32 = new BigInteger("FFFFFFFF", 16);
  private static final BigInteger MAX_UINT64 = new BigInteger("FFFFFFFFFFFFFFFF", 16);

  public static void parse(String json, MessageType messageType, RecordVisitor visitor) {
    JsonParser parser = JacksonCodec.createParser(json);
    try {
      parse(parser, messageType, visitor);
    } finally {
      try {
        parser.close();
      } catch (IOException ignore) {
      }
    }
  }

  public static void parse(JsonParser parser, MessageType messageType, RecordVisitor visitor) throws DecodeException {
    visitor.init(messageType);
    JsonToken remaining;
    try {
      parser.nextToken();
      parseObject(parser, messageType, visitor);
      remaining = parser.nextToken();
    } catch (IOException e) {
      throw new DecodeException(e.getMessage(), e);
    } finally {
      close(parser);
    }
    if (remaining != null) {
      throw new DecodeException("Unexpected trailing token");
    }

    visitor.destroy();
  }

  private static void parseAny(JsonParser parser, Field field, RecordVisitor visitor) throws IOException, DecodeException {
    switch (parser.currentTokenId()) {
      case JsonTokenId.ID_START_OBJECT:
        if (field.isMap()) {
          parseObjectAsMap(parser, field, visitor);
        } else {
          visitor.enter(field);
          parseObject(parser, (MessageType) field.type(), visitor);
          visitor.leave(field);
        }
        break;
      case JsonTokenId.ID_START_ARRAY:
        if (field.isRepeated()) {
          parseArray(parser, field, visitor);
        } else {
          throw new UnsupportedOperationException();
        }
        break;
      case JsonTokenId.ID_STRING:
        String text = parser.getText();
        switch (field.type().id()) {
          case STRING:
            visitor.visitString(field, text);
            break;
          case BYTES:
            visitor.visitBytes(field, Base64.getDecoder().decode(text));
            break;
          case FIXED32:
            visitor.visitFixed32(field, Integer.parseInt(text));
            break;
          case SFIXED32:
            visitor.visitSFixed32(field, Integer.parseInt(text));
            break;
          case FLOAT:
            visitor.visitFloat(field, Float.parseFloat(text));
            break;
          case FIXED64:
            visitor.visitFixed64(field, Long.parseLong(text));
            break;
          case SFIXED64:
            visitor.visitSFixed64(field, Long.parseLong(text));
            break;
          case DOUBLE:
            visitor.visitDouble(field, Double.parseDouble(text));
            break;
          case INT32:
            visitor.visitInt32(field, Integer.parseInt(text));
            break;
          case SINT32:
            visitor.visitSInt32(field, Integer.parseInt(text));
            break;
          case UINT32:
            visitor.visitUInt32(field, parseUInt32(text));
            break;
          case INT64:
            visitor.visitInt64(field, Long.parseLong(text));
            break;
          case SINT64:
            visitor.visitSInt64(field, Long.parseLong(text));
            break;
          case UINT64:
            visitor.visitUInt64(field, parseUInt64(text));
            break;
          case ENUM:
            OptionalInt index = ((EnumType) field.type()).numberOf(text);
            if (index.isPresent()) {
              visitor.visitEnum(field, index.getAsInt());
            } else {
              throw new UnsupportedOperationException();
            }
            break;
          default:
            throw new UnsupportedOperationException();
        }
        break;
      case JsonTokenId.ID_NUMBER_FLOAT:
      case JsonTokenId.ID_NUMBER_INT:
        Number number = parser.getNumberValue();
        switch (field.type().id()) {
          case INT32:
            visitor.visitInt32(field, number.intValue());
            break;
          case SINT32:
            visitor.visitSInt32(field, number.intValue());
            break;
          case UINT32:
            visitor.visitUInt32(field, number.intValue());
            break;
          case FIXED32:
            visitor.visitFixed32(field, number.intValue());
            break;
          case SFIXED32:
            visitor.visitSFixed32(field, number.intValue());
            break;
          case FLOAT:
            visitor.visitFloat(field, number.floatValue());
            break;
          case INT64:
            visitor.visitInt64(field, number.longValue());
            break;
          case SINT64:
            visitor.visitSInt64(field, number.longValue());
            break;
          case UINT64:
            visitor.visitUInt64(field, number.longValue());
            break;
          case FIXED64:
            visitor.visitFixed64(field, number.longValue());
            break;
          case SFIXED64:
            visitor.visitSFixed64(field, number.longValue());
            break;
          case DOUBLE:
            visitor.visitDouble(field, number.doubleValue());
            break;
          default:
            throw new UnsupportedOperationException("Invalid type " + field.type().id());
        }
        break;
      case JsonTokenId.ID_TRUE:
        if (Objects.requireNonNull(field.type().id()) == TypeID.BOOL) {
          visitor.visitBool(field, true);
        } else {
          throw new UnsupportedOperationException();
        }
        break;
      case JsonTokenId.ID_FALSE:
        if (Objects.requireNonNull(field.type().id()) == TypeID.BOOL) {
          visitor.visitBool(field, false);
        } else {
          throw new UnsupportedOperationException();
        }
        break;
      case JsonTokenId.ID_NULL:
        break;
      default:
        throw new DecodeException("Unexpected token"/*, parser.getCurrentLocation()*/);
    }
  }

  private static void parseObjectAsMap(JsonParser parser, Field field, RecordVisitor visitor) throws IOException {
    assert parser.hasToken(JsonToken.START_OBJECT);
    MessageType mt = (MessageType) field.type();
    Field keyField = mt.field(1);
    Field valueField = mt.field(2);
    while (parser.nextToken() == JsonToken.FIELD_NAME) {
      String key = parser.currentName();
      parser.nextToken();
      visitor.enter(field);
      switch (keyField.type().id()) {
        case BOOL:
          visitor.visitBool(keyField, Boolean.parseBoolean(key));
          break;
        case INT32:
          visitor.visitInt32(keyField, Integer.parseInt(key));
          break;
        case INT64:
          visitor.visitInt64(keyField, Long.parseLong(key));
          break;
        case UINT32:
          visitor.visitUInt32(keyField, Integer.parseInt(key));
          break;
        case UINT64:
          visitor.visitUInt64(keyField, parseUInt64(key));
          break;
        case SINT32:
          visitor.visitSInt32(keyField, Integer.parseInt(key));
          break;
        case SINT64:
          visitor.visitSInt64(keyField, Long.parseLong(key));
          break;
        case STRING:
          visitor.visitString(keyField, key);
          break;
        case FIXED64:
          visitor.visitFixed64(keyField, Long.parseLong(key));
          break;
        case SFIXED64:
          visitor.visitSFixed64(keyField, Long.parseLong(key));
          break;
        case FIXED32:
          visitor.visitFixed32(keyField, Integer.parseInt(key));
          break;
        case SFIXED32:
          visitor.visitSFixed32(keyField, Integer.parseInt(key));
          break;
        default:
          throw new UnsupportedOperationException();
      }
      parseAny(parser, valueField, visitor);
      visitor.leave(field);
    }
  }

  private static void parseObject(JsonParser parser, MessageType type, RecordVisitor visitor) throws IOException {
    assert parser.hasToken(JsonToken.START_OBJECT);
    if (type == MessageLiteral.Struct) {
      StructParser.parseObject(parser, visitor);
    } else {
      while (parser.nextToken() == JsonToken.FIELD_NAME) {
        String key = parser.currentName();
        Field field = type.fieldByJsonName(key);
        if (field == null) {
          field = type.fieldByName(key);
        }
        if (field == null) {
          throw new DecodeException("Unknown field " + key);
        }
        parser.nextToken();
        parseAny(parser, field, visitor);
      }
    }
  }

  private static void parseArray(JsonParser parser, Field field, RecordVisitor visitor) throws IOException {
    assert parser.hasToken(JsonToken.START_ARRAY);
    while (parser.nextToken() != JsonToken.END_ARRAY) {
      parseAny(parser, field, visitor);
    }
  }

  static void close(Closeable parser) {
    try {
      parser.close();
    } catch (IOException ignore) {
    }
  }

  private static int parseUInt32(String value) {
    BigInteger parsed = new BigDecimal(value).toBigIntegerExact();
    if (parsed.compareTo(BigInteger.ZERO) < 0 || parsed.compareTo(MAX_UINT32) > 0) {
      throw new DecodeException("Invalid uint64 value");
    }
    return parsed.intValue();
  }

  private static long parseUInt64(String value) {
    BigInteger parsed = new BigDecimal(value).toBigIntegerExact();
    if (parsed.compareTo(BigInteger.ZERO) < 0 || parsed.compareTo(MAX_UINT64) > 0) {
      throw new DecodeException("Invalid uint64 value");
    }
    return parsed.longValue();
  }
}
