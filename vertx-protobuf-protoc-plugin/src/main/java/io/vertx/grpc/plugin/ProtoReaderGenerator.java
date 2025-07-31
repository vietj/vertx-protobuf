package io.vertx.grpc.plugin;

import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

class ProtoReaderGenerator {

  private final String javaPkgFqn;
  private final List<Descriptors.Descriptor> fileDesc;

  public ProtoReaderGenerator(String javaPkgFqn, List<Descriptors.Descriptor> fileDesc) {
    this.javaPkgFqn = javaPkgFqn;
    this.fileDesc = fileDesc;
  }

  public enum VisitorKind {
    String,
    Bytes,
    VarInt32,
    Double,
    Fixed64,
    SFixed64,
    Fixed32,
    SFixed32,
    Float,
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
    public boolean packed;
    public String getterMethod;
    public String setterMethod;
    public String containingJavaType;
    public String typePkgFqn;
    public Function<String, String> converter;
    public boolean imported;
    public boolean oneOf;
  }

  public PluginProtos.CodeGeneratorResponse.File generate() {

    List<FieldDescriptor> collected = new ArrayList<>();

    for (Descriptors.Descriptor mt : fileDesc) {

      Map<Descriptors.FieldDescriptor, Descriptors.OneofDescriptor> oneOfMap = new HashMap<>();
      List<Descriptors.FieldDescriptor> fields = mt.getFields();
      Utils.oneOfs(mt).forEach(oneOf -> {
        oneOf.getFields().forEach(field -> {
          oneOfMap.put(field, oneOf);
        });
      });

      for (Descriptors.FieldDescriptor fd : fields) {

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
          case UINT32:
          case SINT32:
          case INT64:
          case UINT64:
          case SINT64:
            kind = VisitorKind.VarInt32;
            break;
          case STRING:
            kind = VisitorKind.String;
            break;
          case DOUBLE:
            kind = VisitorKind.Double;
            break;
          case FLOAT:
            kind = VisitorKind.Float;
            break;
          case SFIXED32:
            kind = VisitorKind.SFixed32;
            break;
          case FIXED32:
            kind = VisitorKind.Fixed32;
            break;
          case FIXED64:
            kind = VisitorKind.Fixed64;
            break;
          case SFIXED64:
            kind = VisitorKind.SFixed64;
            break;
          case MESSAGE:
            kind = VisitorKind.Message;
            break;
          default:
            continue;
        }
        final Function<String, String> converter;
        switch (fd.getType()) {
          case BOOL:
            converter = s -> "value == 1";
            break;
          case ENUM:
            converter = s -> Utils.javaTypeOfInternal(fd) + ".valueOf(" + s + ")";
            break;
          case BYTES:
            converter = s -> "io.vertx.core.buffer.Buffer.buffer(" + s + ")";
            break;
          // Temporary workaround
          case INT64:
          case UINT64:
          case SINT64:
            converter = s -> "(long)" + s;
            break;
          default:
            converter = Function.identity();
            break;
        }
        descriptor.type = fd.getType();
        descriptor.kind = kind;
        descriptor.map = fd.isMapField();
        descriptor.entry = fd.getContainingType().toProto().getOptions().getMapEntry();
        descriptor.identifier = Utils.schemaLiteralOf(fd);
        descriptor.javaType = Utils.javaTypeOf(fd);
        descriptor.repeated = fd.isRepeated();
        descriptor.packed = fd.isPacked();
        descriptor.containingJavaType = Utils.javaTypeOf(fd.getContainingType());
        descriptor.typePkgFqn = fd.getType() == Descriptors.FieldDescriptor.Type.MESSAGE ? Utils.extractJavaPkgFqn(fd.getMessageType().getFile()) : null;
        descriptor.imported = fd.getType() == Descriptors.FieldDescriptor.Type.MESSAGE && !Utils.extractJavaPkgFqn(fd.getMessageType().getFile()).equals(javaPkgFqn);

        Descriptors.OneofDescriptor oneOf = oneOfMap.get(fd);
        if (oneOf != null) {
          descriptor.oneOf = true;
          descriptor.getterMethod = "/* TODO */";
          descriptor.setterMethod = Utils.setterOf(oneOf);
          descriptor.converter = s -> Utils.javaTypeOf(oneOf) + ".of" + Utils.oneOfTypeName(fd) + "(" + converter.apply(s) + ")";
        } else {
          descriptor.oneOf = false;
          descriptor.getterMethod = Utils.getterOf(fd);
          descriptor.setterMethod = Utils.setterOf(fd);
          descriptor.converter = converter;
        }

        collected.add(descriptor);
//
//        if (descriptor.packed) {
//          throw new UnsupportedOperationException("Handle me " + descriptor.javaType);
//        }
//
      }
    }

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
    for (Descriptors.Descriptor messageType : fileDesc) {
      out.println(
        "if (type == SchemaLiterals." + Utils.schemaLiteralOf(messageType) + ") {",
        "      stack.push(new " + Utils.javaTypeOf(messageType) + "().init());",
        "    } else ");
    }
    out.println(
      "if (next != null) {",
      "      next.init(type);",
      "    } else {",
      "      throw new IllegalArgumentException(\"\");",
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
      new VisitMethod("visitString(Field field, String value)", "visitString(field, value)", Descriptors.FieldDescriptor.Type.STRING),
      new VisitMethod("visitBytes(Field field, byte[] value)", "visitBytes(field, value)", Descriptors.FieldDescriptor.Type.BYTES),
      new VisitMethod("visitFixed32(Field field, int value)", "visitFixed32(field, value)", Descriptors.FieldDescriptor.Type.FIXED32),
      new VisitMethod("visitFixed64(Field field, long value)", "visitFixed64(field, value)", Descriptors.FieldDescriptor.Type.FIXED64),
      new VisitMethod("visitSFixed32(Field field, int value)", "visitSFixed32(field, value)", Descriptors.FieldDescriptor.Type.SFIXED32),
      new VisitMethod("visitSFixed64(Field field, long value)", "visitSFixed64(field, value)", Descriptors.FieldDescriptor.Type.SFIXED64),
      new VisitMethod("visitFloat(Field field, float value)", "visitFloat(field, value)", Descriptors.FieldDescriptor.Type.FLOAT),
      new VisitMethod("visitDouble(Field field, double value)", "visitDouble(field, value)", Descriptors.FieldDescriptor.Type.DOUBLE),
      new VisitMethod("visitVarInt32(Field field, int value)", "visitVarInt32(field, value)",
        Descriptors.FieldDescriptor.Type.BOOL,
        Descriptors.FieldDescriptor.Type.ENUM,
        Descriptors.FieldDescriptor.Type.INT32,
        Descriptors.FieldDescriptor.Type.SINT32,
        Descriptors.FieldDescriptor.Type.UINT32,
        Descriptors.FieldDescriptor.Type.INT64,
        Descriptors.FieldDescriptor.Type.SINT64,
        Descriptors.FieldDescriptor.Type.UINT64
        )
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
        "      throw new IllegalArgumentException(\"Invalid field \" + field);",
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

    collected
      .stream()
      .filter(field -> field.type == Descriptors.FieldDescriptor.Type.MESSAGE || field.packed)
      .forEach(field -> {
        out.println("if (field == SchemaLiterals." + field.identifier + ") {");
        if (field.packed) {
          out.println("      //");
        } else {
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
              String initExpression;
              if (field.repeated) {
                initExpression = "new java.util.ArrayList<>()";
              } else {
                initExpression = "new " + field.javaType + "().init()";
              }
              out.println("      " + field.javaType + " v = " + initExpression + ";");
              out.println("      stack.push(v);");
            }
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
    collected
      .stream()
      .filter(field -> field.type == Descriptors.FieldDescriptor.Type.MESSAGE || field.packed)
      .forEach(field -> {
        out.println("if (field == SchemaLiterals." + field.identifier + ") {");
        if (field.packed) {
          out.println("      //");
        } else {
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
              "      ((" + field.containingJavaType + ")stack.peek())." + field.setterMethod + "(" + field.converter.apply("v") + ");");
          }
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
