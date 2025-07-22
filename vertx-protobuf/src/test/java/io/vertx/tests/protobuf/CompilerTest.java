package io.vertx.tests.protobuf;

import com.google.protobuf.Struct;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.SchemaCompiler;
import org.junit.Test;

public class CompilerTest {

  @Test
  public void testCompile() {

    MessageType mt = new SchemaCompiler().compile(Struct.getDescriptor());

    System.out.println("mt = " + mt);

  }

}
