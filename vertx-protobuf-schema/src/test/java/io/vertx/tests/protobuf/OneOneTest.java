package io.vertx.tests.protobuf;

import io.vertx.protobuf.schema.DefaultField;
import io.vertx.protobuf.schema.DefaultMessageType;
import io.vertx.protobuf.schema.OneOf;
import io.vertx.protobuf.schema.SchemaCompiler;
import io.vertx.tests.protobuf.schema.MessageWithOneOf;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class OneOneTest {

  @Test
  public void testFields() {
    DefaultMessageType type = new SchemaCompiler().compile(MessageWithOneOf.getDescriptor());
    DefaultField f2 = type.field(2);
    DefaultField f3 = type.field(3);
    OneOf oneOf = f2.oneOf();
    assertEquals("union", oneOf.name());
    assertSame(f3.oneOf(), oneOf);
  }
}
