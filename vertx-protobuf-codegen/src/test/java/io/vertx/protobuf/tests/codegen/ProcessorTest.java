package io.vertx.protobuf.tests.codegen;

import io.vertx.codegen.processor.Compiler;
import io.vertx.protobuf.codegen.ProtoProcessor;
import io.vertx.protobuf.tests.codegen.simple.SimpleBean;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class ProcessorTest {

  @Test
  public void testSimple() throws Exception {
    Compiler compiler = new Compiler(new ProtoProcessor());
    assertTrue(compiler.compile(SimpleBean.class));
    File dir = compiler.getSourceOutput();
    assertTrue(new File(dir, "test" + File.separator + "proto" + File.separator + "MessageLiteral.java").exists());
  }
}
