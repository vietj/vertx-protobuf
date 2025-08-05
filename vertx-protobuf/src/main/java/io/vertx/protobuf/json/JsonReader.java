package io.vertx.protobuf.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import io.vertx.core.json.DecodeException;
import io.vertx.protobuf.RecordVisitor;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.Type;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonReader {

  public static void fromParser(JsonParser parser, MessageType messageType, RecordVisitor visitor) throws DecodeException {

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
  }

  private static void parseAny(JsonParser parser, Field field, RecordVisitor visitor) throws IOException, DecodeException {
    switch (parser.currentTokenId()) {
      case JsonTokenId.ID_START_OBJECT:
//        parseObject(parser, (MessageType) field.type(), visitor);
//        break;
        throw new UnsupportedOperationException();
      case JsonTokenId.ID_START_ARRAY:
//        parseArray(parser, visitor);
        if (field.isRepeated()) {
          parseArray(parser, field, visitor);
        } else {
          throw new UnsupportedOperationException();
        }
        break;
      case JsonTokenId.ID_STRING:
        String text = parser.getText();
        visitor.visitString(field, text);
        break;
      case JsonTokenId.ID_NUMBER_FLOAT:
      case JsonTokenId.ID_NUMBER_INT:
        Number number = parser.getNumberValue();
        switch (field.type().id()) {
          case INT32:
            visitor.visitInt32(field, number.intValue());
            break;
          default:
            throw new UnsupportedOperationException();
        }
        break;
      case JsonTokenId.ID_TRUE:
        // Boolean.TRUE;
//        break;
        throw new UnsupportedOperationException();
      case JsonTokenId.ID_FALSE:
        // return Boolean.FALSE;
//        break;
        throw new UnsupportedOperationException();
      case JsonTokenId.ID_NULL:
        // return null;
//        break;
        throw new UnsupportedOperationException();
      default:
        throw new DecodeException("Unexpected token"/*, parser.getCurrentLocation()*/);
    }
  }

  private static void parseObject(JsonParser parser, MessageType type, RecordVisitor visitor) throws IOException {
    String key = parser.nextFieldName();
    if (key == null) {
      return;
    }

    do {
      parser.nextToken();
      Field field = type.field(key);
      if (field == null) {
        throw new UnsupportedOperationException();
      }
      visitor.enter(field);
      parseAny(parser, field, visitor);
      visitor.leave(field);
      key = parser.nextFieldName();
    } while (key != null);
  }

  private static void parseArray(JsonParser parser, Field field, RecordVisitor visitor) throws IOException {
    while (true) {
      parser.nextToken();
      int tokenId = parser.currentTokenId();
      if (tokenId == JsonTokenId.ID_FIELD_NAME) {
        throw new UnsupportedOperationException();
      } else if (tokenId == JsonTokenId.ID_END_ARRAY) {
        return;
      }
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
