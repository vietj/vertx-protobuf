package io.vertx.tests.protobuf;

import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.OneOf;
import io.vertx.protobuf.schema.SchemaCompiler;
import io.vertx.tests.protobuf.schema.MessageWithOneOf;
import org.junit.Test;

import static org.junit.Assert.*;

public class OneOneTest {

  @Test
  public void testFields() {
    MessageType type = new SchemaCompiler().compile(MessageWithOneOf.getDescriptor());
    Field f2 = type.field(2);
    Field f3 = type.field(3);
    OneOf oneOf = f2.oneOf();
    assertEquals("union", oneOf.name());
    assertSame(f3.oneOf(), oneOf);
    assertSame(oneOf, type.oneOf(oneOf.name()));
    assertEquals(1, type.oneOfs().size());
    assertTrue(type.oneOfs().contains(oneOf));
  }
}
