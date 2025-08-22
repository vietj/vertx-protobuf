package io.vertx.tests.protobuf;

import com.google.protobuf.Struct;
import io.vertx.protobuf.schema.EnumType;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.ScalarType;
import io.vertx.protobuf.schema.SchemaCompiler;
import io.vertx.protobuf.schema.Type;
import io.vertx.protobuf.schema.TypeID;
import org.junit.Test;

import static org.junit.Assert.*;

public class CompilerTest {

  @Test
  public void testCompileStruct() {

    MessageType struct = new SchemaCompiler().compile(Struct.getDescriptor());

    assertEquals("Struct", struct.name());

    Field fieldsField = struct.field(1);
    assertEquals(1, fieldsField.number());
    assertEquals("fields", fieldsField.name());
    assertEquals("fields", fieldsField.jsonName());
    assertTrue(fieldsField.isMap());
    assertFalse(fieldsField.isMapKey());
    assertFalse(fieldsField.isMapValue());
    assertTrue(fieldsField.isRepeated());
    assertSame(struct, fieldsField.owner());

    MessageType fieldsEntry = (MessageType) fieldsField.type();
    assertEquals("FieldsEntry", fieldsEntry.name());

    Field fieldsEntryKeyField = fieldsEntry.field(1);
    assertEquals(1, fieldsEntryKeyField.number());
    assertEquals("key", fieldsEntryKeyField.name());
    assertEquals("key", fieldsEntryKeyField.jsonName());
    assertSame(ScalarType.STRING, fieldsEntryKeyField.type());
    assertFalse(fieldsEntryKeyField.isMap());
    assertTrue(fieldsEntryKeyField.isMapKey());
    assertFalse(fieldsEntryKeyField.isMapValue());
    assertFalse(fieldsEntryKeyField.isRepeated());
    assertSame(fieldsEntry, fieldsEntryKeyField.owner());

    Field fieldsEntryValueField = fieldsEntry.field(2);
    MessageType value = (MessageType) fieldsEntryValueField.type();
    assertEquals(2, fieldsEntryValueField.number());
    assertEquals("value", fieldsEntryValueField.name());
    assertEquals("value", fieldsEntryValueField.jsonName());
    assertFalse(fieldsEntryValueField.isMap());
    assertFalse(fieldsEntryValueField.isMapKey());
    assertTrue(fieldsEntryValueField.isMapValue());
    assertFalse(fieldsEntryValueField.isRepeated());
    assertSame(fieldsEntry, fieldsEntryValueField.owner());

    assertEquals("Value", value.name());
    Field nullValueField = value.field(1);
    Field numberValueField = value.field(2);
    Field stringValueField = value.field(3);
    Field boolValueField = value.field(4);
    Field structValueField = value.field(5);
    Field listValueField = value.field(6);

    assertEquals(1, nullValueField.number());
    assertEquals("null_value", nullValueField.name());
    assertEquals("nullValue", nullValueField.jsonName());
    assertFalse(nullValueField.isMap());
    assertFalse(nullValueField.isMapKey());
    assertFalse(nullValueField.isMapValue());
    assertFalse(nullValueField.isRepeated());
    assertSame(value, nullValueField.owner());

    EnumType nullValueEnum = (EnumType) nullValueField.type();
    assertEquals("NullValue", nullValueEnum.name());
    assertEquals("NULL_VALUE", nullValueEnum.nameOf(0));

    assertEquals(2, numberValueField.number());
    assertEquals("number_value", numberValueField.name());
    assertEquals("numberValue", numberValueField.jsonName());
    assertSame(ScalarType.DOUBLE, numberValueField.type());
    assertFalse(numberValueField.isMap());
    assertFalse(numberValueField.isMapKey());
    assertFalse(numberValueField.isMapValue());
    assertFalse(numberValueField.isRepeated());
    assertSame(value, numberValueField.owner());

    assertEquals(3, stringValueField.number());
    assertEquals("string_value", stringValueField.name());
    assertEquals("stringValue", stringValueField.jsonName());
    assertSame(ScalarType.STRING, stringValueField.type());
    assertFalse(stringValueField.isMap());
    assertFalse(stringValueField.isMapKey());
    assertFalse(stringValueField.isMapValue());
    assertFalse(stringValueField.isRepeated());
    assertSame(value, stringValueField.owner());

    assertEquals(4, boolValueField.number());
    assertEquals("bool_value", boolValueField.name());
    assertEquals("boolValue", boolValueField.jsonName());
    assertSame(ScalarType.BOOL, boolValueField.type());
    assertFalse(boolValueField.isMap());
    assertFalse(boolValueField.isMapKey());
    assertFalse(boolValueField.isMapValue());
    assertFalse(boolValueField.isRepeated());
    assertSame(value, boolValueField.owner());

    assertEquals(5, structValueField.number());
    assertEquals("struct_value", structValueField.name());
    assertEquals("structValue", structValueField.jsonName());
    assertSame(struct, structValueField.type());
    assertFalse(structValueField.isMap());
    assertFalse(structValueField.isMapKey());
    assertFalse(structValueField.isMapValue());
    assertFalse(structValueField.isRepeated());
    assertSame(value, structValueField.owner());

    assertEquals(6, listValueField.number());
    assertEquals("list_value", listValueField.name());
    assertEquals("listValue", listValueField.jsonName());
    MessageType listValue = (MessageType) listValueField.type();
    assertFalse(listValueField.isMap());
    assertFalse(listValueField.isMapKey());
    assertFalse(listValueField.isMapValue());
    assertFalse(structValueField.isRepeated());
    assertSame(value, listValueField.owner());

    assertEquals("ListValue", listValue.name());

    Field valuesField = listValue.field(1);

    assertEquals(1, valuesField.number());
    assertEquals("values", valuesField.name());
    assertEquals("values", valuesField.jsonName());
    assertSame(value, valuesField.type());
    assertFalse(valuesField.isMap());
    assertFalse(valuesField.isMapKey());
    assertFalse(valuesField.isMapValue());
    assertTrue(valuesField.isRepeated());
    assertSame(listValue, valuesField.owner());
  }
}
