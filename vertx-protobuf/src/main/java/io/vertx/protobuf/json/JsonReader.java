package io.vertx.protobuf.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.core.exc.InputCoercionException;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.jackson.JacksonCodec;
import io.vertx.protobuf.RecordVisitor;
import io.vertx.protobuf.schema.EnumType;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.well_known_types.FieldLiteral;
import io.vertx.protobuf.well_known_types.MessageLiteral;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Base64;
import java.util.OptionalInt;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;

public class JsonReader {

  private static final DateTimeFormatter f2 = new DateTimeFormatterBuilder()
    .parseCaseSensitive()
    .append(DateTimeFormatter.ISO_LOCAL_DATE)
    .appendLiteral('T')
    .parseStrict()
    .append(ISO_LOCAL_TIME)
    .toFormatter();

  private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
    .parseCaseSensitive()
    .append(f2)
    .parseStrict()
    .appendOffsetId()
    .parseStrict()
    .toFormatter();

  public static final long MIN_DURATION_SECONDS = -315_576_000_000L;
  public static final long MAX_DURATION_SECONDS = 315576000000L;
  public static final int MIN_DURATION_NANOS = -999_999_999;
  public static final int MAX_DURATION_NANOS = 999_999_999;
  public static final OffsetDateTime MIN_TIMESTAMP = OffsetDateTime.parse("0001-01-01T00:00:00Z", formatter);
  public static final OffsetDateTime MAX_TIMESTAMP = OffsetDateTime.parse("9999-12-31T23:59:59Z", formatter);
  public static final Instant MIN_TIMESTAMP_INSTANT = MIN_TIMESTAMP.toInstant();
  public static final Instant MAX_TIMESTAMP_INSTANT = MAX_TIMESTAMP.toInstant();
  public static final long MIN_TIMESTAMP_SECONDS = MIN_TIMESTAMP_INSTANT.getEpochSecond();
  public static final long MAX_TIMESTAMP_SECONDS = MAX_TIMESTAMP_INSTANT.getEpochSecond();

  public static boolean isValidDurationSeconds(long seconds) {
    return seconds >= MIN_DURATION_SECONDS && seconds <= MAX_DURATION_SECONDS;
  }

  public static boolean isValidDurationNanos(int nanos) {
    return nanos >= MIN_DURATION_NANOS && nanos <= MAX_DURATION_NANOS;
  }

  public static boolean isValidDuration(long seconds, int nanos) {
    return isValidDurationSeconds(seconds) && isValidDurationNanos(nanos);
  }

  public static boolean isValidTimestampSeconds(long seconds) {
    return seconds >= MIN_TIMESTAMP_SECONDS && seconds <= MAX_TIMESTAMP_SECONDS;
  }

  public static boolean isValidTimestampNanos(int nanos) {
    return nanos >= 0 && nanos <= 999_999_999;
  }

  public static boolean isValidTimestamp(long seconds, int nanos) {
    return isValidTimestampSeconds(seconds) && isValidTimestampNanos(nanos);
  }

  private static void close(Closeable parser) {
    try {
      parser.close();
    } catch (IOException ignore) {
    }
  }

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
    JsonReader reader = new JsonReader(parser, visitor);
    try {
      reader.read(messageType);
    } finally {
      close(parser);
    }
  }

  private final JsonParser parser;
  private final RecordVisitor visitor;
  private boolean ignoreUnknownFields;

  public JsonReader(String json, RecordVisitor visitor) {
    this(JacksonCodec.createParser(json), visitor);
  }

  public JsonReader(JsonParser parser, RecordVisitor visitor) {
    this.parser = parser;
    this.visitor = visitor;
    this.ignoreUnknownFields = false;
  }

  public JsonReader ignoreUnknownFields(boolean ignoreUnknownFields) {
    this.ignoreUnknownFields = ignoreUnknownFields;
    return this;
  }

  public void read(MessageType messageType) {
    visitor.init(messageType);
    JsonToken remaining;
    try {
      parser.nextToken();
      readObject(messageType);
      remaining = parser.nextToken();
    } catch (IOException e) {
      throw new DecodeException(e.getMessage(), e);
    }
    if (remaining != null) {
      throw new DecodeException("Unexpected trailing token");
    }
    visitor.destroy();
  }

  private void readObject(MessageType type) throws IOException {
    if (!parser.hasToken(JsonToken.START_OBJECT)) {
      throw new DecodeException("Unexpected token " + parser.getCurrentToken());
    }
    while (parser.nextToken() == JsonToken.FIELD_NAME) {
      String key = parser.currentName();
      Field field = type.fieldByJsonName(key);
      if (field == null) {
        field = type.fieldByName(key);
      }
      if (field == null) {
        if (ignoreUnknownFields) {
          parser.nextToken();
          exhaustAny();
        } else {
          throw new DecodeException("Unknown field " + key);
        }
      } else {
        parser.nextToken();
        readAny(field);
      }
    }
  }

  private void readString(Field field) throws IOException, DecodeException {
    if (parser.currentTokenId() == JsonTokenId.ID_STRING) {
      visitor.visitString(field, parser.getText());
    } else {
      throw new DecodeException("Unexpected token " + parser.currentTokenId());
    }
  }

  private void readBytes(Field field) throws IOException, DecodeException {
    if (parser.currentTokenId() == JsonTokenId.ID_STRING) {
      visitor.visitBytes(field, Base64.getDecoder().decode(parser.getText()));
    } else {
      throw new DecodeException("Unexpected token " + parser.currentTokenId());
    }
  }

  private void readBoolean(Field field) throws IOException, DecodeException {
    if (parser.currentTokenId() == JsonTokenId.ID_TRUE || parser.currentTokenId() == JsonTokenId.ID_FALSE) {
      visitor.visitBool(field, parser.getBooleanValue());
    } else {
      throw new DecodeException("Unexpected token " + parser.currentTokenId());
    }
  }

  private void readEnum(Field field) throws IOException, DecodeException {
    EnumType enumType = (EnumType) field.type();
    switch (parser.currentTokenId()) {
      case JsonTokenId.ID_STRING:
        OptionalInt index = enumType.numberOf(parser.getText());
        if (index.isPresent()) {
          visitor.visitEnum(field, index.getAsInt());
        } else {
          throw new DecodeException("Unknown enum " + parser.getText());
        }
        break;
      case JsonTokenId.ID_NUMBER_INT:
        visitor.visitEnum(field, parser.getIntValue());
        break;
      default:
        throw new DecodeException("Unexpected token " + parser.currentTokenId());
    }
  }

  private int readInt() throws IOException, DecodeException {
    switch (parser.currentTokenId()) {
      case JsonTokenId.ID_NUMBER_FLOAT:
        if (parser.getDoubleValue() % 1 != 0D) {
          throw new DecodeException("Invalid number " + parser.getText());
        }
      case JsonTokenId.ID_NUMBER_INT:
        return parser.getIntValue();
      case JsonTokenId.ID_STRING:
        return parseInt(parser.getText());
      default:
        throw new DecodeException("Unexpected token " + parser.currentTokenId());
    }
  }

  private int readUInt32() throws IOException, DecodeException {
    switch (parser.currentTokenId()) {
      case JsonTokenId.ID_NUMBER_FLOAT:
        if (parser.getDoubleValue() % 1 != 0D) {
          throw new DecodeException("Invalid number " + parser.getText());
        }
      case JsonTokenId.ID_NUMBER_INT:
        try {
          return parser.getIntValue();
        } catch (InputCoercionException e) {
          // Fallback to parseUInt32
        }
      case JsonTokenId.ID_STRING:
        return parseUInt32(parser.getText());
      default:
        throw new DecodeException("Unexpected token " + parser.currentTokenId());
    }
  }

  private long readLong() throws IOException, DecodeException {
    switch (parser.currentTokenId()) {
      case JsonTokenId.ID_NUMBER_FLOAT:
        if (parser.getDoubleValue() % 1 != 0D) {
          throw new DecodeException("Invalid number " + parser.getText());
        }
      case JsonTokenId.ID_NUMBER_INT:
        return parser.getLongValue();
      case JsonTokenId.ID_STRING:
        return parseLong(parser.getText());
      default:
        throw new DecodeException("Unexpected token " + parser.currentTokenId());
    }
  }

  private long readUInt64() throws IOException, DecodeException {
    switch (parser.currentTokenId()) {
      case JsonTokenId.ID_NUMBER_FLOAT:
        if (parser.getDoubleValue() % 1 != 0D) {
          throw new DecodeException("Invalid number " + parser.getText());
        }
      case JsonTokenId.ID_NUMBER_INT:
        try {
          return parser.getLongValue();
        } catch (InputCoercionException e) {
          // Fallback to parseUInt64
        }
      case JsonTokenId.ID_STRING:
        return parseUInt64(parser.getText());
      default:
        throw new DecodeException("Unexpected token " + parser.currentTokenId());
    }
  }

  private double readDouble() throws IOException, DecodeException {
    double value;
    switch (parser.currentTokenId()) {
      case JsonTokenId.ID_NUMBER_INT:
      case JsonTokenId.ID_NUMBER_FLOAT:
        value = parser.getDoubleValue();
        break;
      case JsonTokenId.ID_STRING:
        try {
          value = Double.parseDouble(parser.getText());
        } catch (NumberFormatException e) {
          throw new DecodeException("Invalid number: " + e.getMessage());
        }
        break;
      default:
        throw new DecodeException("Unexpected token " + parser.currentTokenId());
    }
    String txt = parser.getText();
    if (Double.isInfinite(value) && !"Infinity".equals(txt) && !"-Infinity".equals(txt)) {
      throw new DecodeException("Invalid number: " + txt);
    }
    return value;
  }

  private float readFloat() throws IOException, DecodeException {
    float value;
    switch (parser.currentTokenId()) {
      case JsonTokenId.ID_NUMBER_INT:
      case JsonTokenId.ID_NUMBER_FLOAT:
        value = parser.getFloatValue();
        break;
      case JsonTokenId.ID_STRING:
        try {
          value = Float.parseFloat(parser.getText());
        } catch (NumberFormatException e) {
          throw new DecodeException("Invalid float: " + e.getMessage());
        }
        break;
      default:
        throw new DecodeException("Unexpected token " + parser.currentTokenId());
    }
    String txt = parser.getText();
    if (Float.isInfinite(value) && !"Infinity".equals(txt) && !"-Infinity".equals(txt)) {
      throw new DecodeException("Invalid number: " + txt);
    }
    return value;
  }

  private void readEmbedded(Field field) throws IOException, DecodeException {
    if (parser.currentTokenId() == JsonTokenId.ID_START_OBJECT) {
      visitor.enter(field);
      readObject((MessageType) field.type());
      visitor.leave(field);
    } else {
      throw new DecodeException("Unexpected token " + parser.currentTokenId());
    }
  }

  private void readNumber(Field field) throws IOException, DecodeException {
    switch (field.type().id()) {
      case INT32:
        visitor.visitInt32(field, readInt());
        break;
      case UINT32:
        visitor.visitUInt32(field, readUInt32());
        break;
      case SINT32:
        visitor.visitSInt32(field, readInt());
        break;
      case INT64:
        visitor.visitInt64(field, readLong());
        break;
      case UINT64:
        visitor.visitUInt64(field, readUInt64());
        break;
      case SINT64:
        visitor.visitSInt64(field, readLong());
        break;
      case FIXED32:
        visitor.visitFixed32(field, readUInt32());
        break;
      case SFIXED32:
        visitor.visitSFixed32(field, readInt());
        break;
      case FLOAT:
        visitor.visitFloat(field, readFloat());
        break;
      case FIXED64:
        visitor.visitFixed64(field, readUInt64());
        break;
      case SFIXED64:
        visitor.visitSFixed64(field, readLong());
        break;
      case DOUBLE:
        visitor.visitDouble(field, readDouble());
        break;
      default:
        throw new UnsupportedOperationException("Unsupported " + field.type());
    }
  }

  private void readAny(Field field) throws IOException, DecodeException {
    if (parser.currentTokenId() == JsonTokenId.ID_NULL) {
      if (field.type() == MessageLiteral.Value) {
        visitor.enter(field);
        visitor.visitEnum(FieldLiteral.Value_null_value, 0);
        visitor.leave(field);
      } else {
        // Use default value
      }
    } else {
      if (field.isMap()) {
        readObjectAsMap(field);
      } else if (field.isRepeated()) {
        readRepeated(field);
      } else {
        readSingleAny(field);
      }
    }
  }

  private void readRepeated(Field field) throws IOException, DecodeException {
    if (parser.currentTokenId() == JsonTokenId.ID_START_ARRAY) {
      while (parser.nextToken() != JsonToken.END_ARRAY) {
        readSingleAny(field);
      }
    } else {
      throw new DecodeException("Unexpected token " + parser.currentTokenId());
    }
  }

  private void readSingleAny(Field field) throws IOException, DecodeException {
    if (field.type() instanceof MessageLiteral) {
      switch ((MessageLiteral)field.type()) {
        case Struct:
          visitor.enter(field);
          StructParser.parseObject(parser, visitor);
          visitor.leave(field);
          break;
        case Value:
          visitor.enter(field);
          StructParser.parseValue(parser, visitor);
          visitor.leave(field);
          break;
        case ListValue:
          visitor.enter(field);
          StructParser.parseArray(parser, visitor);
          visitor.leave(field);
          break;
        case DoubleValue:
          visitor.enter(field);
          readNumber((FieldLiteral.DoubleValue_value));
          visitor.leave(field);
          break;
        case FloatValue:
          visitor.enter(field);
          readNumber(FieldLiteral.FloatValue_value);
          visitor.leave(field);
          break;
        case Int64Value:
          visitor.enter(field);
          readNumber(FieldLiteral.Int64Value_value);
          visitor.leave(field);
          break;
        case UInt64Value:
          visitor.enter(field);
          readNumber(FieldLiteral.UInt64Value_value);
          visitor.leave(field);
          break;
        case Int32Value:
          visitor.enter(field);
          readNumber(FieldLiteral.Int32Value_value);
          visitor.leave(field);
          break;
        case UInt32Value:
          visitor.enter(field);
          readNumber(FieldLiteral.UInt32Value_value);
          visitor.leave(field);
          break;
        case BoolValue:
          visitor.enter(field);
          readBoolean(FieldLiteral.BoolValue_value);
          visitor.leave(field);
          break;
        case StringValue:
          visitor.enter(field);
          readString(FieldLiteral.StringValue_value);
          visitor.leave(field);
          break;
        case BytesValue:
          visitor.enter(field);
          readBytes(FieldLiteral.BytesValue_value);
          visitor.leave(field);
          break;
        case Duration:
          if (parser.currentTokenId() != JsonTokenId.ID_STRING) {
            throw new DecodeException();
          }
          String durationText = parser.getText();
          io.vertx.protobuf.well_known_types.Duration duration = parseDuration(durationText);
          if (duration == null || !isValidDuration(duration.getSeconds(), duration.getNanos())) {
            throw new DecodeException("Invalid duration " + durationText);
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
          if (parser.currentTokenId() != JsonTokenId.ID_STRING) {
            throw new DecodeException();
          }



          String timestampText = parser.getText();
          OffsetDateTime odt;
          try {
            odt = OffsetDateTime.parse(timestampText, formatter);
          } catch (Exception e) {
            throw new DecodeException("Failed to parse timestamp: " + e.getMessage());
          }
          Instant i = odt.toInstant();
          if (i.compareTo(MIN_TIMESTAMP_INSTANT) < 0) {
            throw new DecodeException();
          }
          visitor.enter(field);
          if (i.getEpochSecond() != 0) {
            visitor.visitInt64(FieldLiteral.Timestamp_seconds, i.getEpochSecond());
          }
          if (i.getNano() != 0) {
            visitor.visitInt32(FieldLiteral.Timestamp_nanos, i.getNano());
          }
          visitor.leave(field);
          break;
        case FieldMask:
          if (parser.currentTokenId() != JsonTokenId.ID_STRING) {
            throw new DecodeException();
          }
          String text = parser.getText();
          String[] paths = text.isEmpty() ? new String[0] : text.split(",");
          visitor.enter(field);
          for (String path : paths) {
            visitor.visitString(FieldLiteral.FieldMask_paths, lowerCamelToSnake(path));
          }
          visitor.leave(field);
          break;
        case Any:
//          JsonObject entries = new JsonObject(JacksonCodec.parseObject(parser));
//          String type = entries.getString("@type");
//          if ("type.googleapis.com/google.protobuf.Struct".equals(type)) {
//            Object value = entries.getValue("value");
//            StructParser.parseValue();
//          }
          throw new UnsupportedOperationException();
        default:
          throw new UnsupportedOperationException("Unsupported " + field.type());
      }
    } else {
      switch (field.type().id()) {
        case STRING:
          readString(field);
          break;
        case BYTES:
          readBytes(field);
          break;
        case INT32:
        case UINT32:
        case SINT32:
        case INT64:
        case UINT64:
        case SINT64:
        case FIXED32:
        case SFIXED32:
        case FLOAT:
        case FIXED64:
        case SFIXED64:
        case DOUBLE:
          readNumber(field);
          break;
        case BOOL:
          readBoolean(field);
          break;
        case ENUM:
          readEnum(field);
          break;
        case MESSAGE:
          readEmbedded(field);
          break;
        default:
          throw new UnsupportedOperationException("" + field.type());
      }
    }
  }

  private void readObjectAsMap(Field field) throws IOException {
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
      readSingleAny(valueField);
      visitor.leave(field);
    }
  }

  private int parseInt(String s) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      try {
        return new BigDecimal(s).toBigIntegerExact().intValueExact();
      } catch (Exception ex) {
        throw new DecodeException(e.getMessage());
      }
    }
  }

  private long parseLong(String s) {
    try {
      return Long.parseLong(s);
    } catch (NumberFormatException e) {
      try {
        return new BigDecimal(s).toBigIntegerExact().longValueExact();
      } catch (Exception ex) {
        throw new DecodeException(e.getMessage());
      }
    }
  }

  private static int parseUInt32(String value) {
    BigInteger parsed;
    try {
      parsed = new BigDecimal(value).toBigIntegerExact();
    } catch (ArithmeticException | NumberFormatException e) {
      throw new DecodeException("Invalid uint32: " + e.getMessage());
    }
    if (parsed.compareTo(BigInteger.ZERO) < 0 || parsed.compareTo(MAX_UINT32) > 0) {
      throw new DecodeException("Invalid uint64 value");
    }
    return parsed.intValue();
  }

  private static long parseUInt64(String value) {
    BigInteger parsed;
    try {
      parsed = new BigDecimal(value).toBigIntegerExact();
    } catch (ArithmeticException | NumberFormatException e) {
      throw new DecodeException("Invalid uint64: " + e.getMessage());
    }
    if (parsed.compareTo(BigInteger.ZERO) < 0 || parsed.compareTo(MAX_UINT64) > 0) {
      throw new DecodeException("Invalid uint64 value");
    }
    return parsed.longValue();
  }

  private void exhaustAny() throws IOException, DecodeException {
    switch (parser.currentTokenId()) {
      case JsonTokenId.ID_START_OBJECT:
        exhaustObject();
        break;
      case JsonTokenId.ID_START_ARRAY:
        exhaustArray();
        break;
      case JsonTokenId.ID_STRING:
      case JsonTokenId.ID_NUMBER_FLOAT:
      case JsonTokenId.ID_NUMBER_INT:
      case JsonTokenId.ID_TRUE:
      case JsonTokenId.ID_FALSE:
      case JsonTokenId.ID_NULL:
        break;
      default:
        throw new DecodeException("Unexpected token");
    }
  }

  private void exhaustObject() throws IOException {
    assert parser.hasToken(JsonToken.START_OBJECT);
    while (parser.nextToken() == JsonToken.FIELD_NAME) {
      parser.nextToken();
      exhaustAny();
    }
  }

  private void exhaustArray() throws IOException {
    assert parser.hasToken(JsonToken.START_ARRAY);
    while (parser.nextToken() != JsonToken.END_ARRAY) {
      exhaustAny();
    }
  }

  public static String lowerCamelToSnake(String s) {
    StringBuilder sb = new StringBuilder();
    char prev = 'a'; // Whatever
    for (int i = 0;i < s.length();i++) {
      char c = s.charAt(i);
      if (c >= 'A' && c <= 'Z') {
        if (i > 0 && prev != '_') {
          sb.append('_');
        }
        c += 'a' - 'A';
      } /*else if (i == 0 && c >= 'a' && c <= 'z') {

      }*/
      sb.append(c);
      prev = c;
    }
    return sb.toString();
  }

  public static String snakeToLowerCamel(String fieldName) {
    StringBuilder sb = new StringBuilder(fieldName.length());
    char prev = ' ';
    for (int i = 0;i < fieldName.length();i++) {
      char ch = fieldName.charAt(i);
      boolean upperCase = ch >= 'a' && ch <= 'z' && (prev == '_');
      prev = ch;
      if (ch != '_') {
        ch = (char)(upperCase ? ch + ('A' - 'a') : ch);
        sb.append(ch);
      }
    }
    return sb.toString();
  }
}
