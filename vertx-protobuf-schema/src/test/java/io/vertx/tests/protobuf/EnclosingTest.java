package io.vertx.tests.protobuf;

import io.vertx.protobuf.schema.DefaultEnumType;
import io.vertx.protobuf.schema.DefaultMessageType;
import io.vertx.protobuf.schema.SchemaCompiler;
import io.vertx.tests.protobuf.schema.EnclosingMessage;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class EnclosingTest {

  @Test
  public void testEnclosedMessage() {
    SchemaCompiler compiler = new SchemaCompiler();
    DefaultMessageType enclosingType = compiler.compile(EnclosingMessage.getDescriptor());
    DefaultMessageType enclosedType = compiler.compile(EnclosingMessage.EnclosedMessage.getDescriptor());
    assertSame(enclosingType, enclosedType.enclosingType());
    DefaultEnumType enclosedEnum = compiler.compile(EnclosingMessage.EnclosedEnum.getDescriptor());
    assertSame(enclosingType, enclosedEnum.enclosingType());
  }
}
