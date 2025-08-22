package io.vertx.tests.protobuf;

import io.vertx.protobuf.schema.DefaultField;
import io.vertx.protobuf.schema.DefaultMessageType;
import io.vertx.protobuf.schema.SchemaCompiler;
import io.vertx.tests.protobuf.schema.MessageWithOptional;
import org.junit.Test;

import static org.junit.Assert.*;

public class OptionalTest {

  @Test
  public void testFields() {
    DefaultMessageType type = new SchemaCompiler().compile(MessageWithOptional.getDescriptor());
    DefaultField f1 = type.field(1);
    DefaultField f2 = type.field(2);
    DefaultField f3 = type.field(3);
    DefaultField f4 = type.field(4);
    assertNull(f1.oneOf());
    assertFalse(f1.isOptional());
    assertNull(f2.oneOf());
    assertTrue(f2.isOptional());
    assertNotNull(f3.oneOf());
    assertTrue(f3.isOptional());
    assertNotNull(f4.oneOf());
    assertTrue(f4.isOptional());
  }
}
