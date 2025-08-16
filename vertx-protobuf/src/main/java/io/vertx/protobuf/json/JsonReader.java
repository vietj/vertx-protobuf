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
import io.vertx.protobuf.well_known_types.FieldLiteral;
import io.vertx.protobuf.well_known_types.MessageLiteral;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonReader {

  public static io.vertx.protobuf.well_known_types.Duration parseDuration(String s) {
    Matcher matcher = DURATION.matcher(s);
    if (!matcher.matches()) {
      return null;
    }
    boolean negative = matcher.group(1) != null;
    long seconds = Long.parseLong(matcher.group(2));
    int nano = 0;
    String nanoText = matcher.group(3);
    if (nanoText != null) {
      // Optimize this later
      nanoText = "0." + nanoText;
      BigDecimal bd = new BigDecimal(nanoText);
      BigInteger bi = bd.multiply(BigDecimal.valueOf(1000_000_000)).toBigInteger();
      nano = bi.intValue();
    }
    if (negative)  {
      seconds = -seconds;
      nano = -nano;
    }
    return new io.vertx.protobuf.well_known_types.Duration().setSeconds(seconds).setNanos(nano);
  }

  private static final Pattern DURATION = Pattern.compile("(-)?([0-9]+)(?:\\.([0-9]+))?s");

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
          if (field.type() == MessageLiteral.Value) {
            visitor.enter(field);
            StructParser.parseValue(parser, visitor);
            visitor.leave(field);
          } else {
            visitor.enter(field);
            parseObject(parser, (MessageType) field.type(), visitor);
            visitor.leave(field);
          }
        }
        break;
      case JsonTokenId.ID_START_ARRAY:
        if (field.isRepeated()) {
          parseArray(parser, field, visitor);
        } else if (field.type() == MessageLiteral.Value) {
          visitor.enter(field);
          StructParser.parseValue(parser, visitor);
          visitor.leave(field);
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
          case MESSAGE:
            if (field.type() instanceof MessageLiteral) {
              MessageLiteral messageLiteral = (MessageLiteral) field.type();
              switch (messageLiteral) {
                case Duration:
                  io.vertx.protobuf.well_known_types.Duration duration = parseDuration(text);
                  if (duration == null) {
                    throw new DecodeException("Invalid duration " + text);
                  }
                  visitor.enter(field);
                  if (duration.getSeconds() != 0) {
                    visitor.visitInt64(FieldLiteral.Duration_seconds, duration.getSeconds());
                  }
                  if (duration.getNanos() != 0) {
                    visitor.visitInt32(FieldLiteral.Duration_nanos, duration.getNanos());
                  }
                  visitor.leave(field);
                  break;
                case Timestamp:
                  OffsetDateTime odt = OffsetDateTime.parse(text, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                  Instant i = odt.toInstant();
                  visitor.enter(field);
                  if (i.getEpochSecond() != 0) {
                    visitor.visitInt64(FieldLiteral.Timestamp_seconds, i.getEpochSecond());
                  }
                  if (i.getNano() != 0) {
                    visitor.visitInt32(FieldLiteral.Timestamp_nanos, i.getNano());
                  }
                  visitor.leave(field);
                  break;
                case Int64Value:
                  visitor.enter(field);
                  visitor.visitInt64(FieldLiteral.Int64Value_value, Long.parseLong(text));
                  visitor.leave(field);
                  break;
                case UInt64Value:
                  visitor.enter(field);
                  visitor.visitUInt64(FieldLiteral.UInt64Value_value, Long.parseLong(text));
                  visitor.leave(field);
                  break;
                case StringValue:
                  visitor.enter(field);
                  visitor.visitString(FieldLiteral.StringValue_value, text);
                  visitor.leave(field);
                  break;
                case BytesValue:
                  visitor.enter(field);
                  visitor.visitBytes(FieldLiteral.BytesValue_value, Base64.getDecoder().decode(text));
                  visitor.leave(field);
                  break;
                case Value:
                  visitor.enter(field);
                  visitor.visitString(FieldLiteral.Value_string_value, text);
                  visitor.leave(field);
                  break;
                default:
                  throw new UnsupportedOperationException();
              }
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
          case MESSAGE:
            if (field.type() instanceof MessageLiteral) {
              switch ((MessageLiteral)field.type()) {
                case DoubleValue:
                  visitor.enter(field);
                  visitor.visitDouble(FieldLiteral.DoubleValue_value, number.doubleValue());
                  visitor.leave(field);
                  break;
                case FloatValue:
                  visitor.enter(field);
                  visitor.visitFloat(FieldLiteral.FloatValue_value, number.floatValue());
                  visitor.leave(field);
                  break;
                case Int32Value:
                  visitor.enter(field);
                  visitor.visitInt32(FieldLiteral.Int32Value_value, number.intValue());
                  visitor.leave(field);
                  break;
                case UInt32Value:
                  visitor.enter(field);
                  visitor.visitUInt32(FieldLiteral.UInt32Value_value, number.intValue());
                  visitor.leave(field);
                  break;
                case Value:
                  visitor.enter(field);
                  visitor.visitDouble(FieldLiteral.Value_number_value, number.doubleValue());
                  visitor.leave(field);
                  break;
                default:
                  throw new DecodeException();
              }
              break;
            } else {
              throw new DecodeException();
            }
          default:
            throw new UnsupportedOperationException("Invalid type " + field.type().id());
        }
        break;
      case JsonTokenId.ID_TRUE:
        parseBoolean(field, true, visitor);
        break;
      case JsonTokenId.ID_FALSE:
        parseBoolean(field, false, visitor);
        break;
      case JsonTokenId.ID_NULL:
        if (field.type() == MessageLiteral.Value) {
          visitor.enter(field);
          visitor.visitEnum(FieldLiteral.Value_null_value, 0);
          visitor.leave(field);
        } else {
          // Use default value
        }
        break;
      default:
        throw new DecodeException("Unexpected token"/*, parser.getCurrentLocation()*/);
    }
  }

  private static void parseBoolean(Field field, boolean value, RecordVisitor visitor) {
    if (field.type() == MessageLiteral.BoolValue) {
      visitor.enter(field);
      visitor.visitBool(FieldLiteral.BoolValue_value, value);
      visitor.leave(field);
    } else if (field.type() == MessageLiteral.Value) {
      visitor.enter(field);
      visitor.visitBool(FieldLiteral.Value_bool_value, value);
      visitor.leave(field);
    } else if (Objects.requireNonNull(field.type().id()) == TypeID.BOOL) {
      visitor.visitBool(field, true);
    } else {
      throw new UnsupportedOperationException();
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
