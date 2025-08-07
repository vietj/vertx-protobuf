package io.vertx.protobuf.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import io.vertx.core.json.DecodeException;
import io.vertx.protobuf.RecordVisitor;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;

import java.io.IOException;

class StructParser {

  static void parseObject(JsonParser parser, MessageType type, RecordVisitor visitor) throws IOException {
    assert parser.hasToken(JsonToken.START_OBJECT);
    Field fieldsEntry = type.field(1);
    visitor.enterRepetition(fieldsEntry);
    while (parser.nextToken() == JsonToken.FIELD_NAME) {
      String key = parser.currentName();
      visitor.enter(fieldsEntry);
      Field keyField = ((MessageType) fieldsEntry.type()).field(1);
      Field valueField = ((MessageType) fieldsEntry.type()).field(2);
      visitor.visitString(keyField, key);
      parser.nextToken();
      visitor.enter(valueField);
      parseAny(parser, (MessageType) valueField.type(), visitor);
      visitor.leave(valueField);
      visitor.leave(fieldsEntry);
    }
    visitor.leaveRepetition(fieldsEntry);
  }

  private static void parseAny(JsonParser parser, MessageType valueType, RecordVisitor visitor) throws IOException, DecodeException {
    switch (parser.currentTokenId()) {
      case JsonTokenId.ID_START_OBJECT:
        Field f2 = valueType.field(5);
        visitor.enter(f2);
        parseObject(parser, (MessageType) f2.type(), visitor);
        visitor.leave(f2);
        break;
      case JsonTokenId.ID_START_ARRAY:
        Field f = valueType.field(6);
        visitor.enter(f);
        parseArray(parser, (MessageType) f.type(), visitor);
        visitor.leave(f);
        break;
      case JsonTokenId.ID_STRING:
        String text = parser.getText();
        visitor.visitString(valueType.field(3), text);
        break;
      case JsonTokenId.ID_NUMBER_FLOAT:
      case JsonTokenId.ID_NUMBER_INT:
        double number = parser.getDoubleValue();
        visitor.visitDouble(valueType.field(2), number);
        break;
      case JsonTokenId.ID_TRUE:
        visitor.visitBool(valueType.field(4), true);
        break;
      case JsonTokenId.ID_FALSE:
        visitor.visitBool(valueType.field(4), false);
        break;
      case JsonTokenId.ID_NULL:
        visitor.visitEnum(valueType.field(1), 0);
        break;
      default:
        throw new DecodeException("Unexpected token"/*, parser.getCurrentLocation()*/);
    }
  }

  private static void parseArray(JsonParser parser, MessageType type, RecordVisitor visitor) throws IOException {
    assert parser.hasToken(JsonToken.START_ARRAY);
    Field values = type.field(1);
    visitor.enterRepetition(values);
    while (parser.nextToken() != JsonToken.END_ARRAY) {
      visitor.enter(values);
      parseAny(parser, (MessageType) values.type(), visitor);
      visitor.leave(values);
    }
    visitor.leaveRepetition(values);
  }
}
