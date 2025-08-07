package io.vertx.grpc.plugin;

import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public String mapJavaType;
    public boolean mapKeyEntry;
    public Descriptors.FieldDescriptor.Type mapKeyType;
    public boolean mapValueEntry;
    public Descriptors.FieldDescriptor.Type mapValueType;
    public String mapValueMessageIdentifier;
    public String mapValueEnumJavaType;
    public String mapValueEnumConstant;
    public String mapValueJavaType;
    public String identifier;
    public String javaType;
    public String javaTypeInternal;
    public boolean repeated;
    public String getterMethod;
    public String setterMethod;
    public String containingJavaType;
    public String protoReaderJavaType;
    public Function<String, String> wrapper;
    public Function<String, String> unwrapper;
    public boolean imported;
    public boolean oneOf;
    public String oneOfJavaType;
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
          case ENUM:
            converter = s -> Utils.javaTypeOfInternal(fd) + ".valueOf(" + s + ")";
            break;
          case BYTES:
            converter = s -> "io.vertx.core.buffer.Buffer.buffer(" + s + ")";
            break;
          default:
            converter = Function.identity();
            break;
        }
        descriptor.type = fd.getType();
        descriptor.kind = kind;
        descriptor.identifier = Utils.literalIdentifier(fd);
        descriptor.javaType = Utils.javaTypeOf(fd);
        descriptor.javaTypeInternal = Utils.javaTypeOfInternal(fd);
        descriptor.repeated = fd.isRepeated();
        descriptor.containingJavaType = Utils.javaTypeOf(fd.getContainingType());

        if (fd.getType() == Descriptors.FieldDescriptor.Type.MESSAGE) {
          if (Utils.isStruct(fd.getMessageType()) && Utils.useJsonObject(fd.getFile())) {
            descriptor.protoReaderJavaType = "io.vertx.protobuf.json.ProtoReader";
          } else {
            descriptor.protoReaderJavaType = Utils.extractJavaPkgFqn(fd.getMessageType().getFile()) + ".ProtoReader";
          }
          descriptor.imported = !Utils.extractJavaPkgFqn(fd.getMessageType().getFile()).equals(javaPkgFqn);
        } else {
          descriptor.protoReaderJavaType = null;
          descriptor.imported = false;
        }

        if (fd.isMapField()) {
          Descriptors.FieldDescriptor blah = fd.getMessageType().getFields().get(1);
          Descriptors.FieldDescriptor.Type mapValueType = blah.getType();
          descriptor.map = true;
          descriptor.mapJavaType = Utils.javaTypeOfInternal(fd);
          descriptor.mapKeyType = fd.getMessageType().getFields().get(0).getType();
          descriptor.mapValueType = mapValueType;
          descriptor.mapValueJavaType = Utils.javaTypeOf(blah);
          switch (mapValueType) {
            case MESSAGE:
              descriptor.mapValueMessageIdentifier = Utils.literalIdentifier(blah.getMessageType());
              break;
            case ENUM:
              descriptor.mapValueEnumJavaType = Utils.javaTypeOf(blah);
              descriptor.mapValueEnumConstant = Utils.defaultEnumValue(blah.getEnumType()).getName();
              break;
            default:
              break;
          }
        } else {
          descriptor.map = false;
        }

        if (fd.getContainingType().toProto().getOptions().getMapEntry()) {
          descriptor.mapJavaType = Utils.javaTypeOf(fd.getContainingType());
          if (fd.getContainingType().getFields().get(0) == fd) {
            descriptor.mapKeyEntry = true;
          } else if (fd.getContainingType().getFields().get(1) == fd) {
            descriptor.mapValueEntry = true;
          }
        }

        Descriptors.OneofDescriptor oneOf = oneOfMap.get(fd);
        if (oneOf != null) {
          descriptor.oneOf = true;
          descriptor.oneOfJavaType = Utils.javaTypeOf(oneOf);
          descriptor.getterMethod = Utils.getterOf(oneOf);
          descriptor.setterMethod = Utils.setterOf(oneOf);
          descriptor.wrapper = s -> Utils.javaTypeOf(oneOf) + ".of" + Utils.oneOfTypeName(fd) + "(" + converter.apply(s) + ")";
          descriptor.unwrapper = s -> s + ".as" + Utils.oneOfTypeName(fd) + "().orElse(null)";
        } else {
          descriptor.oneOf = false;
          descriptor.getterMethod = Utils.getterOf(fd);
          descriptor.setterMethod = Utils.setterOf(fd);
          descriptor.wrapper = converter;
          descriptor.unwrapper = Function.identity();
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
      "import io.vertx.protobuf.RecordVisitor;",
      "import io.vertx.protobuf.schema.MessageType;",
      "import io.vertx.protobuf.schema.Field;",
      "import java.util.Deque;",
      "import java.util.ArrayDeque;",
      "",
      "public class ProtoReader implements RecordVisitor {",
      "",
      "  public final Deque<Object> stack;",
      "  private RecordVisitor next;",
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
      "  public void init(MessageType type) {",
      "    if (type instanceof MessageLiteral) {",
      "      MessageLiteral literal = (MessageLiteral)type;",
      "      switch (literal) {"
    );
    for (Descriptors.Descriptor messageType : fileDesc) {
      out.println(
        "        case " + Utils.literalIdentifier(messageType) + ": {",
        "          stack.push(new " + Utils.javaTypeOf(messageType) + "().init());",
        "          break;",
        "        }");
    }
    out.println(
      "        default:",
      "          throw new UnsupportedOperationException();",
      "        }",
      "    } else if (next != null) {",
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
      final Descriptors.FieldDescriptor.Type type;
      final String next;
      final boolean allowsUnkown;

      VisitMethod(String methodStart, String next, Descriptors.FieldDescriptor.Type type, boolean allowsUnkown) {
        this.methodStart = methodStart;
        this.type = type;
        this.next = next;
        this.allowsUnkown = allowsUnkown;
      }
    }

    VisitMethod[] visitMethods = {
      new VisitMethod("visitString(Field field, String value)", "visitString(field, value)", Descriptors.FieldDescriptor.Type.STRING, false),
      new VisitMethod("visitBytes(Field field, byte[] value)", "visitBytes(field, value)", Descriptors.FieldDescriptor.Type.BYTES, true),
      new VisitMethod("visitFixed32(Field field, int value)", "visitFixed32(field, value)", Descriptors.FieldDescriptor.Type.FIXED32, true),
      new VisitMethod("visitFixed64(Field field, long value)", "visitFixed64(field, value)", Descriptors.FieldDescriptor.Type.FIXED64, true),
      new VisitMethod("visitSFixed32(Field field, int value)", "visitSFixed32(field, value)", Descriptors.FieldDescriptor.Type.SFIXED32, false),
      new VisitMethod("visitSFixed64(Field field, long value)", "visitSFixed64(field, value)", Descriptors.FieldDescriptor.Type.SFIXED64, false),
      new VisitMethod("visitFloat(Field field, float value)", "visitFloat(field, value)", Descriptors.FieldDescriptor.Type.FLOAT, false),
      new VisitMethod("visitDouble(Field field, double value)", "visitDouble(field, value)", Descriptors.FieldDescriptor.Type.DOUBLE, false),
      new VisitMethod("visitInt32(Field field, int value)", "visitInt32(field, value)", Descriptors.FieldDescriptor.Type.INT32, false),
      new VisitMethod("visitUInt32(Field field, int value)", "visitUInt32(field, value)", Descriptors.FieldDescriptor.Type.UINT32, false),
      new VisitMethod("visitSInt32(Field field, int value)", "visitSInt32(field, value)", Descriptors.FieldDescriptor.Type.SINT32, false),
      new VisitMethod("visitBool(Field field, boolean value)", "visitBool(field, value)", Descriptors.FieldDescriptor.Type.BOOL, false),
      new VisitMethod("visitEnum(Field field, int value)", "visitEnum(field, value)", Descriptors.FieldDescriptor.Type.ENUM, false),
      new VisitMethod("visitInt64(Field field, long value)", "visitInt64(field, value)", Descriptors.FieldDescriptor.Type.INT64, true),
      new VisitMethod("visitSInt64(Field field, long value)", "visitSInt64(field, value)", Descriptors.FieldDescriptor.Type.SINT64, false),
      new VisitMethod("visitUInt64(Field field, long value)", "visitUInt64(field, value)", Descriptors.FieldDescriptor.Type.UINT64, false)
    };

    for (VisitMethod visitMethod : visitMethods) {
      out.println(
        "",
        "  public void " + visitMethod.methodStart + " {",
        "    if (field instanceof FieldLiteral) {");
      out.println("      FieldLiteral fieldLiteral = (FieldLiteral)field;");
      out.println("      switch (fieldLiteral) {");
      for (FieldDescriptor fd : collected.stream().filter(f -> visitMethod.type == f.type).collect(Collectors.toList())) {
        out.println("        case " + fd.identifier + ": {");
        if (fd.mapKeyEntry) {
          out.println("          " + fd.mapJavaType + " entry = (" + fd.mapJavaType + ")stack.peek();");
          out.println("          entry.setKey(" + fd.wrapper.apply("value") + ");");
        } else if (fd.mapValueEntry) {
          out.println("          " + fd.mapJavaType + " entry = (" + fd.mapJavaType + ")stack.peek();");
          out.println("          entry.setValue(" + fd.wrapper.apply("value") + ");");
        } else if (fd.repeated) {
          out.println(
            "          " + fd.containingJavaType + " messageFields = (" + fd.containingJavaType + ")stack.peek()" + ";",
            "          if (messageFields." + fd.getterMethod + "() == null) {",
            "            messageFields." + fd.setterMethod + "(new java.util.ArrayList<>());",
            "          }",
            "          messageFields." + fd.getterMethod + "().add(" + fd.wrapper.apply("value") + ");");
        } else {
          out.println("          ((" + fd.containingJavaType + ")stack.peek())." + fd.setterMethod + "(" + fd.wrapper.apply("value") + ");");
        }
        out.println("          break;");
        out.println("        }");
      }
      out.println(
        "        default:",
        "          throw new IllegalArgumentException(\"Invalid field \" + field);",
        "      }");
      if (visitMethod.allowsUnkown) {
        out.println(
          "    } else if (field.isUnknown()) {",
          "      " + javaPkgFqn + ".MessageBase base = (" + javaPkgFqn + ".MessageBase)stack.peek();");
        switch (visitMethod.type) {
          case BYTES:
            out.println("      base.unknownField(field).add(io.vertx.core.buffer.Buffer.buffer(value));");
            break;
          case FIXED32:
            out.println("      base.unknownField(field).add(value);");
            break;
          case FIXED64:
            out.println("      base.unknownField(field).add(value);");
            break;
          case INT64:
            out.println("      base.unknownField(field).add(value);");
            break;
        }
      }
      out.println(
        "    } else if (next != null) {",
        "      next." + visitMethod.next + ";",
        "    } else {",
        "      throw new UnsupportedOperationException();",
        "    }");
      out.println("  }");
    }

    // **************
    // ENTER
    // **************

    out.println(
      "",
      "  public void enter(Field field) {",
      "    if (field instanceof FieldLiteral) {",
      "      FieldLiteral literal = (FieldLiteral)field;",
      "      switch (literal) {");

    collected
      .stream()
      .filter(field -> field.type == Descriptors.FieldDescriptor.Type.MESSAGE)
      .forEach(field -> {
        out.println("        case " + field.identifier + ": {");
        if (field.map) {
          out.println("          " + field.mapJavaType + " entry = new " + field.mapJavaType + "();");
          out.println("          stack.push(entry);");
        } else {
          if (field.imported) {
            out.println(
              "          RecordVisitor v = new " + field.protoReaderJavaType + "(stack);",
              "          v.init((MessageType)field.type());",
              "          next = v;");
          } else {
            if (field.repeated) {
              String initExpression = field.javaTypeInternal;
              out.println("          " + initExpression + " v = " + "new " + initExpression + "().init()" + ";");
            } else {
              String initExpression = field.javaType;
              out.println("          " + field.containingJavaType + " container = (" + field.containingJavaType + ")stack.peek();");
              out.println("          " + initExpression + " v;");
              if (field.oneOf) {
                out.println("          " + field.oneOfJavaType + "<?> oneOf = container." + field.getterMethod + "();");
                out.println("          v = oneOf != null ? " + field.unwrapper.apply("oneOf") + " : " + "null;");
              } else {
                out.println("          v = container." + field.getterMethod + "();");
              }
              out.println("          if (v == null) {");
              out.println("            v = " + "new " + initExpression + "().init()" + ";");
              out.println("          }");
            }
            out.println("          stack.push(v);");
          }
        }
        out.println("          break;");
        out.println("        }");
      });
    out.println("        default:");
    out.println("          throw new UnsupportedOperationException();");
    out.println("      }");
    out.println(
      "    } else if (field.isUnknown()) {",
      "    } else if (next != null) {",
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
      "  public void leave(Field field) {",
      "    if (field instanceof FieldLiteral) {",
      "      FieldLiteral literal = (FieldLiteral)field;",
      "      switch (literal) {");
    collected
      .stream()
      .filter(field -> field.type == Descriptors.FieldDescriptor.Type.MESSAGE)
      .forEach(field -> {
        out.println("        case " + field.identifier + ": {");
        if (field.map) {
          out.println(
            "          " + field.mapJavaType + " entry = (" + field.mapJavaType + ")stack.pop();",
            "          " + field.mapValueJavaType + " value = entry.getValue();");
          if (field.mapValueType == Descriptors.FieldDescriptor.Type.MESSAGE) {
            out.println(
              "          if (value == null) {",
              "            value = new " + field.mapValueJavaType + "().init();",
              "}");
          }
          out.println("          " + field.containingJavaType + " container = (" + field.containingJavaType + ")stack.peek();",
            "          " + field.javaType + " entries = container." + field.getterMethod + "();",
            "          if (entries == null) {",
            "            entries = new java.util.HashMap<>();",
            "            container." + field.setterMethod + "(entries);",
            "          }",
            "          entries.put(entry.getKey(), value);");
        } else if (field.mapKeyEntry) {
          out.println(
            "          " + field.javaType + " v = (" + field.javaType + ")stack.pop();",
            "          " + field.mapJavaType + " entry = (" + field.mapJavaType + ")stack.peek();",
            "          entry.setKey(v);");
        } else if (field.mapValueEntry) {
          out.println(
            "          " + field.javaType + " v = (" + field.javaType + ")stack.pop();",
            "          " + field.mapJavaType + " entry = (" + field.mapJavaType + ")stack.peek();",
            "          entry.setValue(v);");
        } else {
          if (field.imported) {
            out.println(
              "          next.destroy();",
              "          next = null;");
          }
          if (field.repeated) {
            out.println(
              "          " + field.javaTypeInternal + " value = (" + field.javaTypeInternal + ") stack.pop();",
              "          " + field.containingJavaType + " container = (" + field.containingJavaType + ")stack.peek();",
              "          container." + field.getterMethod + "().add(value);"
            );
          } else {
            out.println(
              "          " + field.javaType + " v = (" + field.javaType + ")stack.pop();",
              "          ((" + field.containingJavaType + ")stack.peek())." + field.setterMethod + "(" + field.wrapper.apply("v") + ");");
          }
        }
        out.println("          break;");
        out.println("        }");
    });
    out.println(
      "        default:",
      "          throw new UnsupportedOperationException();",
      "      }",
      "    } else if (field.isUnknown()) {",
      "    } else if (next != null) {",
      "      next.leave(field);",
      "    } else {",
      "      throw new UnsupportedOperationException();",
      "    }",
      "  }");

    // **************
    // LEAVE REPETITION
    // **************

    out.println(
      "",
      "  public void enterRepetition(Field field) {",
      "    if (field instanceof FieldLiteral) {",
      "      FieldLiteral literal = (FieldLiteral)field;",
      "      switch (literal) {");

    collected
      .stream()
      .filter(field -> field.repeated)
      .forEach(field -> {
        out.println("        case " + field.identifier + ": {");
        out.println("          break;");
        out.println("        }");
      });
    out.println("        default:");
    out.println("          throw new UnsupportedOperationException();");
    out.println("      }");
    out.println(
      "    } else if (next != null) {",
      "      next.enterRepetition(field);",
      "    } else {",
      "      throw new UnsupportedOperationException();",
      "    }",
      "  }");

    // **************
    // LEAVE REPETITION
    // **************

    out.println(
      "",
      "  public void leaveRepetition(Field field) {",
      "    if (field instanceof FieldLiteral) {",
      "      FieldLiteral literal = (FieldLiteral)field;",
      "      switch (literal) {");
    collected
      .stream()
      .filter(field -> field.repeated)
      .forEach(field -> {
        out.println("        case " + field.identifier + ": {");
        out.println("          break;");
        out.println("        }");
      });
    out.println(
      "        default:",
      "          throw new UnsupportedOperationException();",
      "      }",
      "    } else if (next != null) {",
      "      next.leaveRepetition(field);",
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
      "      next.destroy();",
      "    }",
      "  }");

    out.println("}");

    return PluginProtos.CodeGeneratorResponse.File
      .newBuilder()
      .setName(Utils.absoluteFileName(javaPkgFqn, "ProtoReader"))
      .setContent(out.toString())
      .build();
  }
}
