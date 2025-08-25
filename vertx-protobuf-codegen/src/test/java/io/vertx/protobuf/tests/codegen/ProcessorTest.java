package io.vertx.protobuf.tests.codegen;

import io.vertx.codegen.processor.Compiler;
import io.vertx.core.json.JsonObject;
import io.vertx.protobuf.ProtoStream;
import io.vertx.protobuf.ProtoVisitor;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.codegen.ProtoProcessor;
import io.vertx.protobuf.json.ProtoJsonReader;
import io.vertx.protobuf.json.ProtoJsonWriter;
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
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

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
    File sourceOutput = new File(target, "tests-" + name.getMethodName() );
    if (sourceOutput.exists()) {
      File dst;
      int idx = 0;
      while (true) {
        dst = new File(target, "tests-" + name.getMethodName() + "-" + idx);
        if (!dst.exists()) {
          break;
        }
        idx++;
      }
      if (!sourceOutput.renameTo(dst)) {
        throw new AssertionFailedError();
      }
    }
    if (!sourceOutput.mkdirs()) {
      throw new AssertionFailedError();
    }
    List<Diagnostic<? extends JavaFileObject>> errors = new ArrayList<>();
    Compiler compiler = new Compiler(new ProtoProcessor(), diagnostic -> {
      if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
        errors.add(diagnostic);
      }
    });
    compiler.setSourceOutput(sourceOutput);
    try {
      if (!compiler.compile(types)) {
        AssertionFailedError afe;
        if (!errors.isEmpty()) {
          Diagnostic<? extends JavaFileObject> error = errors.get(0);
          afe = new AssertionFailedError(error.toString());
        } else {
          afe = new AssertionFailedError();
        }
        throw afe;
      }
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
    Class<? extends ProtoVisitor> readerClazz = (Class<? extends ProtoVisitor>) loader.loadClass(DataTypes.class.getPackageName() + ".ProtoReader");
    ProtoVisitor protoReader = readerClazz.getDeclaredConstructor().newInstance();
    JsonObject json = new JsonObject()
      .put("stringField", "the-string")
      .put("enumField", "ANOTHER");
    ProtoJsonReader.parse(json.encode(), ml, protoReader);
    DataTypes o = (DataTypes) ((Deque) readerClazz.getField("stack").get(protoReader)).pop();
    assertEquals("the-string", o.getStringField());
    assertEquals(TestEnum.ANOTHER, o.getEnumField());
    ProtoStream protoStream = ((Function<Object, ProtoStream>) ml).apply(o);
    JsonObject res = ProtoJsonWriter.encode(protoStream);
    assertEquals(json, res);
  }
}
