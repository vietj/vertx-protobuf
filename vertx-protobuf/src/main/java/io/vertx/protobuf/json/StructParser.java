package io.vertx.protobuf.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import io.vertx.core.json.DecodeException;
import io.vertx.protobuf.RecordVisitor;
import io.vertx.protobuf.well_known_types.FieldLiteral;

import java.io.IOException;

class StructParser {

  static void parseObject(JsonParser parser, RecordVisitor visitor) throws IOException {
    assert parser.hasToken(JsonToken.START_OBJECT);
    visitor.enterRepetition(FieldLiteral.Struct_fields);
    while (parser.nextToken() == JsonToken.FIELD_NAME) {
      String key = parser.currentName();
      visitor.enter(FieldLiteral.Struct_fields);
      visitor.visitString(FieldLiteral.FieldsEntry_key, key);
      parser.nextToken();
      visitor.enter(FieldLiteral.FieldsEntry_value);
      parseAny(parser, visitor);
      visitor.leave(FieldLiteral.FieldsEntry_value);
      visitor.leave(FieldLiteral.Struct_fields);
    }
    visitor.leaveRepetition(FieldLiteral.Struct_fields);
  }

  private static void parseAny(JsonParser parser, RecordVisitor visitor) throws IOException, DecodeException {
    switch (parser.currentTokenId()) {
      case JsonTokenId.ID_START_OBJECT:
        visitor.enter(FieldLiteral.Value_struct_value);
        parseObject(parser, visitor);
        visitor.leave(FieldLiteral.Value_struct_value);
        break;
      case JsonTokenId.ID_START_ARRAY:
        visitor.enter(FieldLiteral.Value_list_value);
        parseArray(parser, visitor);
        visitor.leave(FieldLiteral.Value_list_value);
        break;
      case JsonTokenId.ID_STRING:
        String text = parser.getText();
        visitor.visitString(FieldLiteral.Value_string_value, text);
        break;
      case JsonTokenId.ID_NUMBER_FLOAT:
      case JsonTokenId.ID_NUMBER_INT:
        double number = parser.getDoubleValue();
        visitor.visitDouble(FieldLiteral.Value_number_value, number);
        break;
      case JsonTokenId.ID_TRUE:
        visitor.visitBool(FieldLiteral.Value_bool_value, true);
        break;
      case JsonTokenId.ID_FALSE:
        visitor.visitBool(FieldLiteral.Value_bool_value, false);
        break;
      case JsonTokenId.ID_NULL:
        visitor.visitEnum(FieldLiteral.Value_null_value, 0);
        break;
      default:
        throw new DecodeException("Unexpected token"/*, parser.getCurrentLocation()*/);
    }
  }

  private static void parseArray(JsonParser parser, RecordVisitor visitor) throws IOException {
    assert parser.hasToken(JsonToken.START_ARRAY);
    visitor.enterRepetition(FieldLiteral.ListValue_values);
    while (parser.nextToken() != JsonToken.END_ARRAY) {
      visitor.enter(FieldLiteral.ListValue_values);
      parseAny(parser, visitor);
      visitor.leave(FieldLiteral.ListValue_values);
    }
    visitor.leaveRepetition(FieldLiteral.ListValue_values);
  }
}
