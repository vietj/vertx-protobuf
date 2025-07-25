package io.vertx.grpc.plugin;

import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

class ProtoReaderGenerator {

  private final Descriptors.FileDescriptor fileDesc;

  public ProtoReaderGenerator(Descriptors.FileDescriptor fileDesc) {
    this.fileDesc = fileDesc;
  }

  public enum VisitorKind {
    String,
    Bytes,
    VarInt32,
    Double,
    Message
  }

  public static class FieldDescriptor {
    public Descriptors.FieldDescriptor.Type type;
    public VisitorKind kind;
    public boolean map;
    public boolean entry;
    public String identifier;
    public String javaType;
    public boolean repeated;
    public String getterMethod;
    public String setterMethod;
    public String containingJavaType;
    public String typePkgFqn;
    public Function<String, String> converter;
    public boolean imported;
  }

  public PluginProtos.CodeGeneratorResponse.File generate() {

    String javaPkgFqn = Utils.extractJavaPkgFqn(fileDesc.toProto());

    Map<String, Descriptors.Descriptor> all = Utils.transitiveClosure(fileDesc.getMessageTypes());

    List<FieldDescriptor> collected = new ArrayList<>();
    for (Descriptors.Descriptor mt : all.values()) {
      for (Descriptors.FieldDescriptor fd : mt.getFields()) {

        FieldDescriptor descriptor = new FieldDescriptor();

        VisitorKind kind;
        switch (fd.getType()) {
          // Bytes
          case BYTES:
            kind = VisitorKind.Bytes;
            break;
          // VarInt32
          case BOOL:
          case ENUM:
          case INT32:
            kind = VisitorKind.VarInt32;
            break;
          // String
          case STRING:
            kind = VisitorKind.String;
            break;
          // Double
          case DOUBLE:
            kind = VisitorKind.Double;
            break;
          case MESSAGE:
            kind = VisitorKind.Message;
            break;
          default:
            continue;
        }
        Function<String, String> converter = Function.identity();
        switch (fd.getType()) {
          case BOOL:
            converter = s -> "value == 1";
            break;
          case ENUM:
            converter = s -> Utils.javaTypeOf(fd) + ".valueOf(" + s + ")";
            break;
          case BYTES:
            converter = s -> "io.vertx.core.buffer.Buffer.buffer(" + s + ")";
            break;
        }
        descriptor.type = fd.getType();
        descriptor.kind = kind;
        descriptor.map = fd.isMapField();
        descriptor.entry = fd.getContainingType().toProto().getOptions().getMapEntry();
        descriptor.identifier = Utils.schemaLiteralOf(fd);
        descriptor.javaType = Utils.javaTypeOf(fd);
        descriptor.repeated = fd.isRepeated();
        descriptor.getterMethod = Utils.getterOf(fd);
        descriptor.setterMethod = Utils.setterOf(fd);
        descriptor.containingJavaType = Utils.javaTypeOf(fd.getContainingType());
        descriptor.typePkgFqn = fd.getType() == Descriptors.FieldDescriptor.Type.MESSAGE ? fd.getMessageType().getFile().getOptions().getJavaPackage() : null;
        descriptor.converter = converter;
        descriptor.imported = fd.getType() == Descriptors.FieldDescriptor.Type.MESSAGE && fd.getMessageType().getFile() != fileDesc;
        collected.add(descriptor);
      }
    }

    List<FieldDescriptor> messageFields = collected
      .stream()
      .filter(field -> field.type == Descriptors.FieldDescriptor.Type.MESSAGE)
      .collect(Collectors.toList());

    StringWriter out = new StringWriter();
    PrintWriter content = new PrintWriter(out);

    content.println("package " + javaPkgFqn + ";");
    content.println();
    content.println("import io.vertx.protobuf.Visitor;");
    content.println("import io.vertx.protobuf.schema.MessageType;");
    content.println("import io.vertx.protobuf.schema.Field;");
    content.println("import java.util.Deque;");
    content.println("import java.util.ArrayDeque;");
    content.println();
    content.println("public class ProtoReader implements Visitor {");
    content.println();
    content.println("  public final Deque<Object> stack;");
    content.println("  private Visitor next;");
    content.println();
    content.println("  public ProtoReader(Deque<Object> stack) {");
    content.println("    this.stack = stack;");
    content.println("  }");
    content.println();
    content.println("  public ProtoReader() {");
    content.println("    this(new ArrayDeque<>());");
    content.println("  }");

    // **************
    // INIT
    // **************

    content.println();
    content.println("  public void init(MessageType type) {");
    content.print("    ");
    for (Descriptors.Descriptor messageType : fileDesc.getMessageTypes()) {
      content.println("if (type == SchemaLiterals." + Utils.schemaLiteralOf(messageType) + ") {");
      content.println("      stack.push(new " + messageType.getName() + "());");
      content.print("    } else ");
    }
    content.println("if (next != null) {");
    content.println("      next.init(type);");
    content.println("    } else {");
    content.println("      throw new UnsupportedOperationException();");
    content.println("    }");
    content.println("  }");

    // **************
    // VISIT STRING
    // **************

    class VisitMethod {
      final String methodStart;
      final Set<Descriptors.FieldDescriptor.Type> types;
      final String next;

      VisitMethod(String methodStart, String next, Descriptors.FieldDescriptor.Type... types) {
        this.methodStart = methodStart;
        this.types = new HashSet<>(Arrays.asList(types));
        this.next = next;
      }
    }

    VisitMethod[] visitMethods = {
      new VisitMethod("visitBytes(Field field, byte[] value)", "visitBytes(field, value)", Descriptors.FieldDescriptor.Type.BYTES),
      new VisitMethod("visitString(Field field, String value)", "visitString(field, value)", Descriptors.FieldDescriptor.Type.STRING),
      new VisitMethod("visitDouble(Field field, double value)", "visitDouble(field, value)", Descriptors.FieldDescriptor.Type.DOUBLE),
      new VisitMethod("visitVarInt32(Field field, int value)", "visitVarInt32(field, value)", Descriptors.FieldDescriptor.Type.BOOL, Descriptors.FieldDescriptor.Type.ENUM, Descriptors.FieldDescriptor.Type.INT32)
    };

    for (VisitMethod visitMethod : visitMethods) {
      content.println();
      content.println("  public void " + visitMethod.methodStart + " {");
      content.print("    ");
      for (FieldDescriptor fd : collected.stream().filter(f -> visitMethod.types.contains(f.type)).collect(Collectors.toList())) {
        content.println("if (field == SchemaLiterals." + fd.identifier + ") {");
        if (fd.entry) {
          content.println("      stack.push(value);");
        } else if (fd.repeated) {
          content.println("      " + fd.containingJavaType + " messageFields = (" + fd.containingJavaType + ")stack.peek()" + ";");
          content.println("      if (messageFields." + fd.getterMethod + "() == null) {");
          content.println("        messageFields." + fd.setterMethod + "(new java.util.ArrayList<>());");
          content.println("      }");
          content.println("      messageFields." + fd.getterMethod + "().add(" + fd.converter.apply("value") + ");");
        } else {
          content.println("      ((" + fd.containingJavaType + ")stack.peek())." + fd.setterMethod + "(" + fd.converter.apply("value") + ");");
        }
        content.print("    } else ");
      }
      content.println("if (next != null) {");
      content.println("      next." + visitMethod.next + ";");
      content.println("    } else {");
      content.println("      throw new UnsupportedOperationException();");
      content.println("    }");
      content.println("  }");
    }

    // **************
    // ENTER
    // **************

    content.println();
    content.println("  public void enter(Field field) {");
    content.print("    ");

    messageFields
      .forEach(field -> {
        content.println("if (field == SchemaLiterals." + field.identifier + ") {");
        if (field.map) {
          content.println("      " + field.containingJavaType + " container = (" + field.containingJavaType + ")stack.peek();");
          content.println("      " + field.javaType + " map = container." + field.getterMethod + "();");
          content.println("      if (map == null) {");
          content.println("        map = new java.util.HashMap<>();");
          content.println("        container." + field.setterMethod + "(map);");
          content.println("      }");
          content.println("      stack.push(map);");
        } else {
          if (field.imported) {
            content.println("      Visitor v = new " + field.typePkgFqn + ".ProtoReader(stack);");
            content.println("      v.init((MessageType)field.type);");
            content.println("      next = v;");
          } else {
            String i_type;
            if (field.repeated) {
              i_type = field.javaType.replace("java.util.List", "java.util.ArrayList");
            } else {
              i_type = field.javaType;
            }
            content.println("      " + field.javaType + " v = new " + i_type + "();");
            content.println("      stack.push(v);");
          }
        }
        content.print("    } else ");
      });
    content.println("if (next != null) {");
    content.println("      next.enter(field);");
    content.println("    } else {");
    content.println("      throw new UnsupportedOperationException();");
    content.println("    }");
    content.println("  }");

    // **************
    // VISIT LEAVE
    // **************

    content.println();
    content.println("  public void leave(Field field) {");
    content.print("    ");
    messageFields
      .forEach(field -> {
        content.println("if (field == SchemaLiterals." + field.identifier + ") {");
        if (field.map) {
          content.println("      Object value = stack.pop();");
          content.println("      Object key = stack.pop();");
          content.println("      java.util.Map entries = (java.util.Map)stack.pop();");
          content.println("      entries.put(key, value);");
        } else if (field.entry) {
          content.println("      //");
        } else {
          if (field.imported) {
            content.println("      next.destroy();");
            content.println("      next = null;");
          }
          content.println("      " + field.javaType + " v = (" + field.javaType + ")stack.pop();");
          content.println("      ((" + field.containingJavaType + ")stack.peek())." + field.setterMethod + "(v);");
        }
        content.print("    } else ");
    });
    content.println("if (next != null) {");
    content.println("      next.leave(field);");
    content.println("    } else {");
    content.println("      throw new UnsupportedOperationException();");
    content.println("    }");
    content.println("  }");

    // **************
    // DESTROY
    // **************

    content.println();
    content.println("  public void destroy() {");
    content.println("    if (next != null) {");
    content.println("      next.destroy();");
    content.println("    }");
    content.println("  }");

    content.println("}");

    content.close();
    return PluginProtos.CodeGeneratorResponse.File
      .newBuilder()
      .setName(Utils.absoluteFileName(javaPkgFqn, "ProtoReader"))
      .setContent(out.toString())
      .build();
  }
}
