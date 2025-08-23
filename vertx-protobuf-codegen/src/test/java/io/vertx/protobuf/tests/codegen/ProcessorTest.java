package io.vertx.protobuf.tests.codegen;

import io.vertx.codegen.processor.Compiler;
import io.vertx.protobuf.codegen.ProtoProcessor;
import io.vertx.protobuf.tests.codegen.simple.SimpleBean;
import org.junit.Test;

public class ProcessorTest {

  @Test
  public void testSimple() throws Exception {

    Compiler compiler = new Compiler(new ProtoProcessor());

    compiler.compile(SimpleBean.class);

  }

}
