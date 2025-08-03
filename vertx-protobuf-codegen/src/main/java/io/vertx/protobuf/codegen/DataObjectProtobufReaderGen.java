package io.vertx.protobuf.codegen;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.codegen.processor.DataObjectModel;
import io.vertx.codegen.processor.Generator;
import io.vertx.codegen.processor.Model;
import io.vertx.codegen.processor.ModuleModel;
import io.vertx.codegen.processor.writer.CodeWriter;
import io.vertx.protobuf.codegen.annotations.ProtobufGen;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class DataObjectProtobufReaderGen extends Generator<Model> {

  public DataObjectProtobufReaderGen() {
    kinds = Set.of("dataObject", "module");
    name = "data_object_converters";
  }

  @Override
  public Collection<Class<? extends Annotation>> annotations() {
    return Arrays.asList(ModuleGen.class, DataObject.class);
  }

  private List<DataObjectModel> dataObjects = new ArrayList<>();

  @Override
  public String filename(Model model) {
    if (model instanceof ModuleModel) {
      return model.getFqn() + ".ProtoReader.java";
    } else if (model instanceof DataObjectModel) {
      dataObjects.add((DataObjectModel) model);
    }
    return null;
  }

  @Override
  public String render(Model model, int index, int size, Map<String, Object> session) {
    return renderProto((ModuleModel) model, true, index, size, session);
  }

  public String renderProto(ModuleModel model, boolean isPublic, int index, int size, Map<String, Object> session) {
    StringWriter buffer = new StringWriter();
    PrintWriter writer = new PrintWriter(buffer);
    CodeWriter code = new CodeWriter(writer);

    code.codeln("package " + model.getModule().getPackageName() + ";");
    code.newLine();

    code.codeln("import io.vertx.protobuf.schema.Field;");
    code.codeln("import io.vertx.protobuf.schema.MessageType;");
    code.codeln("import io.vertx.protobuf.RecordVisitor;");
    code.codeln("import java.util.ArrayDeque;");
    code.codeln("import java.util.Deque;");
    code.newLine();

    code
      .codeln("public class ProtoReader implements RecordVisitor {"
      ).newLine();

    //
    code.indent();

    code.codeln("private final Deque<Object> stack = new ArrayDeque<>();");

    code.codeln("public void init(MessageType mt) {");
    code.codeln("}");

    code.codeln("public void visitVarInt32(Field field, int v) {");
    code.codeln("}");

    code.codeln("public void visitString(Field field, String s) {");
    code.codeln("}");

    code.codeln("public void visitBytes(Field field, byte[] s) {");
    code.codeln("}");

    code.codeln("public void visitDouble(Field field, double d) {");
    code.codeln("}");

    code.codeln("public void enter(Field field) {");
    code.codeln("}");

    code.codeln("public void leave(Field field) {");
    code.codeln("}");

    code.codeln("public void destroy() {");
    code.codeln("}");

    code.unindent();


    code.codeln("}").newLine();

    return buffer.toString();
  }
}
