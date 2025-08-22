package io.vertx.tests.protobuf;

import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MapField;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.OneOf;
import io.vertx.protobuf.schema.ScalarType;
import io.vertx.protobuf.schema.SchemaCompiler;
import io.vertx.tests.protobuf.schema.MessageWithMap;
import io.vertx.tests.protobuf.schema.MessageWithOneOf;
import org.junit.Test;

import static org.junit.Assert.*;

public class MapTest {

  @Test
  public void testFields() {
    MessageType type = new SchemaCompiler().compile(MessageWithMap.getDescriptor());
    Field f1 = type.field(1);
    assertTrue(f1.isMap());
    MapField mapField = (MapField) f1;
    assertTrue(mapField.key().isMapKey());
    assertSame(ScalarType.STRING, mapField.key().type());
    assertTrue(mapField.value().isMapValue());
    assertSame(ScalarType.INT32, mapField.value().type());
  }
}
