package io.vertx.protobuf.tests.codegen;

import io.vertx.codegen.processor.Compiler;
import io.vertx.protobuf.codegen.ProtoProcessor;
import io.vertx.protobuf.schema.EnumType;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.ScalarType;
import io.vertx.protobuf.schema.TypeID;
import io.vertx.protobuf.tests.codegen.simple.DataTypes;
import io.vertx.protobuf.tests.codegen.simple.TestEnum;
import junit.framework.AssertionFailedError;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProcessorTest {

  @Rule
  public TestName name = new TestName();

  private File compile(Class<?>... types) {
    String sprop = System.getProperty("maven.project.build.directory");
    if (sprop == null) {
      throw new AssertionFailedError("Was expecting maven.project.build.directory system property to be set");
    }
    File target = new File(sprop);
    if (!target.exists() || !target.isDirectory()) {
      throw new AssertionFailedError();
    }
    File sourceOutput;
    for (int i = 0;;i++) {
      sourceOutput = new File(target, "tests-" + name.getMethodName() + i);
      if (!sourceOutput.exists()) {
        if (!sourceOutput.mkdirs()) {
          throw new AssertionFailedError();
        }
        break;
      }
    }
    Compiler compiler = new Compiler(new ProtoProcessor(), new DiagnosticListener<JavaFileObject>() {
      @Override
      public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
        System.out.println(diagnostic.getMessage(null));
      }
    });
    compiler.setSourceOutput(sourceOutput);
    try {
      assertTrue(compiler.compile(types));
    } catch (Exception e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
    return compiler.getClassOutput();
  }

  @Test
  public void testDataTypes() throws Exception {
    File dir = compile(DataTypes.class, TestEnum.class);
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
    Field f3 = ml.field(3);
    assertEquals("booleanField", f3.jsonName());
    assertEquals(ScalarType.BOOL, f3.type());
    Field f4 = ml.field(4);
    assertEquals("enumField", f4.jsonName());
    assertEquals(TypeID.ENUM, f4.type().id());
    EnumType enumType = (EnumType) f4.type();
    assertEquals("DEFAULT", enumType.nameOf(0));
    assertEquals("ANOTHER", enumType.nameOf(1));
  }
}
