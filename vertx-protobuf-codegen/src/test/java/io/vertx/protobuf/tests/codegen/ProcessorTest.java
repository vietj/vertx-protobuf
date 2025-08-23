package io.vertx.protobuf.tests.codegen;

import io.vertx.codegen.processor.Compiler;
import io.vertx.protobuf.codegen.ProtoProcessor;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.ScalarType;
import io.vertx.protobuf.tests.codegen.simple.DataTypes;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProcessorTest {

  @Test
  public void testDataTypes() throws Exception {
    Compiler compiler = new Compiler(new ProtoProcessor());
    assertTrue(compiler.compile(DataTypes.class));
    File dir = compiler.getClassOutput();
    assertTrue(new File(dir, DataTypes.class.getPackageName().replace('.', File.separatorChar) + File.separator + "MessageLiteral.class").exists());
    URL url = dir.toURI().toURL();
    ClassLoader loader = new URLClassLoader(new URL[] {url}, Thread.currentThread().getContextClassLoader());
    Class<?> clazz = loader.loadClass(DataTypes.class.getPackageName() + ".MessageLiteral");
    MessageType ml = (MessageType) clazz.getField("DataTypes").get(null);
    Field f1 = ml.field(1);
    assertEquals("stringField", f1.jsonName());
    assertEquals(ScalarType.STRING, f1.type());
    Field f2 = ml.field(2);
    assertEquals("longField", f2.jsonName());
    assertEquals(ScalarType.INT64, f2.type());
  }
}
