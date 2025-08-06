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

import java.io.Closeable;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;

public class JsonReader {

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

    //
    visitor.init(messageType);

    Object res;
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
        visitor.enter(field);
        parseObject(parser, (MessageType) field.type(), visitor);
        visitor.leave(field);
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
//            visitor.enter(field);
            visitor.visitString(field, text);
//            visitor.leave(field);
            break;
          case BYTES:
//            visitor.enter(field);
            visitor.visitBytes(field, Base64.getDecoder().decode(text));
//            visitor.leave(field);
            break;
          case FIXED64:
            visitor.visitFixed64(field, Long.parseLong(text));
            break;
          case SFIXED64:
            visitor.visitSFixed64(field, Long.parseLong(text));
            break;
          case INT64:
            visitor.visitInt64(field, Long.parseLong(text));
            break;
          case SINT64:
            visitor.visitSInt64(field, Long.parseLong(text));
            break;
          case UINT64:
            visitor.visitUInt64(field, Long.parseLong(text));
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
          case DOUBLE:
            visitor.visitDouble(field, number.doubleValue());
            break;
          default:
            throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
      default:
        throw new DecodeException("Unexpected token"/*, parser.getCurrentLocation()*/);
    }
  }

  private static void parseObject(JsonParser parser, MessageType type, RecordVisitor visitor) throws IOException {
    assert parser.hasToken(JsonToken.START_OBJECT);

    // Check Struct
    // check is not great ... but well for now it's fine
    if (type.name().equals("Struct")) {
      throw new UnsupportedOperationException("Not yet implemented");
    }

    while (parser.nextToken() == JsonToken.FIELD_NAME) {
      String key = parser.currentName();
      Field field = type.fieldByJsonName(key);
      if (field == null) {
        field = type.fieldByName(key);
      }
      if (field == null) {
        throw new UnsupportedOperationException("Unknown field " + key);
      }
      parser.nextToken();
      parseAny(parser, field, visitor);
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
}
