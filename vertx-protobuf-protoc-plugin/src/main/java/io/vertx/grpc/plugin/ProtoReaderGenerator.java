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

    GenWriter out = new GenWriter();

    out.println(
      "package " + javaPkgFqn + ";",
      "",
      "import io.vertx.protobuf.Visitor;",
      "import io.vertx.protobuf.schema.MessageType;",
      "import io.vertx.protobuf.schema.Field;",
      "import java.util.Deque;",
      "import java.util.ArrayDeque;",
      "",
      "public class ProtoReader implements Visitor {",
      "",
      "  public final Deque<Object> stack;",
      "  private Visitor next;",
      "",
      "  public ProtoReader(Deque<Object> stack) {",
      "    this.stack = stack;", "  }",
      "", "  public ProtoReader() {",
      "    this(new ArrayDeque<>());",
      "  }");

    // **************
    // INIT
    // **************

    out.println(
      "",
      "  public void init(MessageType type) {");
    out.print("    ");
    for (Descriptors.Descriptor messageType : fileDesc.getMessageTypes()) {
      out.println(
        "if (type == SchemaLiterals." + Utils.schemaLiteralOf(messageType) + ") {",
        "      stack.push(new " + messageType.getName() + "());",
        "    } else ");
    }
    out.println(
      "if (next != null) {",
      "      next.init(type);",
      "    } else {",
      "      throw new UnsupportedOperationException();",
      "    }",
      "  }");

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
      out.println(
        "",
        "  public void " + visitMethod.methodStart + " {");
      out.print("    ");
      for (FieldDescriptor fd : collected.stream().filter(f -> visitMethod.types.contains(f.type)).collect(Collectors.toList())) {
        out.println("if (field == SchemaLiterals." + fd.identifier + ") {");
        if (fd.entry) {
          out.println("      stack.push(value);");
        } else if (fd.repeated) {
          out.println(
            "      " + fd.containingJavaType + " messageFields = (" + fd.containingJavaType + ")stack.peek()" + ";",
            "      if (messageFields." + fd.getterMethod + "() == null) {",
            "        messageFields." + fd.setterMethod + "(new java.util.ArrayList<>());",
            "      }",
            "      messageFields." + fd.getterMethod + "().add(" + fd.converter.apply("value") + ");");
        } else {
          out.println("      ((" + fd.containingJavaType + ")stack.peek())." + fd.setterMethod + "(" + fd.converter.apply("value") + ");");
        }
        out.print("    } else ");
      }
      out.println(
        "if (next != null) {",
        "      next." + visitMethod.next + ";",
        "    } else {",
        "      throw new UnsupportedOperationException();",
        "    }",
        "  }");
    }

    // **************
    // ENTER
    // **************

    out.println(
      "",
      "  public void enter(Field field) {");
    out.print("    ");

    messageFields
      .forEach(field -> {
        out.println("if (field == SchemaLiterals." + field.identifier + ") {");
        if (field.map) {
          out.println(
            "      " + field.containingJavaType + " container = (" + field.containingJavaType + ")stack.peek();",
            "      " + field.javaType + " map = container." + field.getterMethod + "();",
            "      if (map == null) {",
            "        map = new java.util.HashMap<>();",
            "        container." + field.setterMethod + "(map);",
            "      }",
            "      stack.push(map);");
        } else {
          if (field.imported) {
            out.println(
              "      Visitor v = new " + field.typePkgFqn + ".ProtoReader(stack);",
              "      v.init((MessageType)field.type);",
              "      next = v;");
          } else {
            String i_type;
            if (field.repeated) {
              i_type = field.javaType.replace("java.util.List", "java.util.ArrayList");
            } else {
              i_type = field.javaType;
            }
            out.println("      " + field.javaType + " v = new " + i_type + "();");
            out.println("      stack.push(v);");
          }
        }
        out.print("    } else ");
      });
    out.println(
      "if (next != null) {",
      "      next.enter(field);",
      "    } else {",
      "      throw new UnsupportedOperationException();",
      "    }",
      "  }");

    // **************
    // VISIT LEAVE
    // **************

    out.println(
      "",
      "  public void leave(Field field) {");
    out.print("    ");
    messageFields
      .forEach(field -> {
        out.println("if (field == SchemaLiterals." + field.identifier + ") {");
        if (field.map) {
          out.println(
            "      Object value = stack.pop();",
            "      Object key = stack.pop();",
            "      java.util.Map entries = (java.util.Map)stack.pop();",
            "      entries.put(key, value);");
        } else if (field.entry) {
          out.println("      //");
        } else {
          if (field.imported) {
            out.println(
              "      next.destroy();",
              "      next = null;");
          }
          out.println(
            "      " + field.javaType + " v = (" + field.javaType + ")stack.pop();",
            "      ((" + field.containingJavaType + ")stack.peek())." + field.setterMethod + "(v);");
        }
        out.print("    } else ");
    });
    out.println(
      "if (next != null) {",
      "      next.leave(field);",
      "    } else {",
      "      throw new UnsupportedOperationException();",
      "    }",
      "  }");

    // **************
    // DESTROY
    // **************

    out.println(
      "",
      "  public void destroy() {",
      "    if (next != null) {",
      "      next.destroy();", "    }",
      "  }");

    out.println("}");

    return PluginProtos.CodeGeneratorResponse.File
      .newBuilder()
      .setName(Utils.absoluteFileName(javaPkgFqn, "ProtoReader"))
      .setContent(out.toString())
      .build();
  }
}
