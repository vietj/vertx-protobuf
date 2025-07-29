package io.vertx.grpc.plugin;

import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

class ProtoWriterGenerator {

  static class Bilto {
    final String visitMethod;
    final Function<String, String> fn;
    final Descriptors.FieldDescriptor.Type type;
    Bilto(String visitMethod, Function<String, String> fn, Descriptors.FieldDescriptor.Type type) {
      this.visitMethod = visitMethod;
      this.fn = fn;
      this.type = type;
    }
    Bilto(String visitMethod, Descriptors.FieldDescriptor.Type type) {
      this.visitMethod = visitMethod;
      this.fn = Function.identity();
      this.type = type;
    }
  }

  private static final Map<Descriptors.FieldDescriptor.Type, Bilto> TYPE_TO = new HashMap<>();

  static {
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.BYTES, new Bilto("visitBytes", s -> s + ".getBytes()", Descriptors.FieldDescriptor.Type.BYTES));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.FLOAT, new Bilto("visitFloat", Descriptors.FieldDescriptor.Type.FLOAT));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.DOUBLE, new Bilto("visitDouble", Descriptors.FieldDescriptor.Type.DOUBLE));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.STRING, new Bilto("visitString", Descriptors.FieldDescriptor.Type.STRING));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.BOOL, new Bilto("visitVarInt32", s -> s + "? 1 : 0", Descriptors.FieldDescriptor.Type.BOOL));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.ENUM, new Bilto("visitVarInt32", s -> s + ".index()", Descriptors.FieldDescriptor.Type.ENUM));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.INT32, new Bilto("visitVarInt32", Descriptors.FieldDescriptor.Type.INT32));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.UINT32, new Bilto("visitVarInt32", Descriptors.FieldDescriptor.Type.UINT32));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.INT64, new Bilto("visitVarInt64", Descriptors.FieldDescriptor.Type.INT64));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.UINT64, new Bilto("visitVarInt64", Descriptors.FieldDescriptor.Type.UINT64));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.SINT32, new Bilto("visitVarInt32", Descriptors.FieldDescriptor.Type.SINT32));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.SINT64, new Bilto("visitVarInt64", Descriptors.FieldDescriptor.Type.SINT64));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.FIXED32, new Bilto("visitFixed32", Descriptors.FieldDescriptor.Type.FIXED32));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.FIXED64, new Bilto("visitFixed64", Descriptors.FieldDescriptor.Type.FIXED64));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.SFIXED32, new Bilto("visitSFixed32", Descriptors.FieldDescriptor.Type.SFIXED32));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.SFIXED64, new Bilto("visitSFixed64", Descriptors.FieldDescriptor.Type.SFIXED64));
  }

  private final String javaPkgFqn;
  private final List<Descriptors.Descriptor> fileDesc;

  public ProtoWriterGenerator(String javaPkgFqn, List<Descriptors.Descriptor> fileDesc) {
    this.javaPkgFqn = javaPkgFqn;
    this.fileDesc = fileDesc;
  }

  static class FieldDescriptor {
    public Bilto typeTo;
    public boolean map;
    public String identifier;
    public String keyJavaType;
    public Bilto keyTypeTo;
    public String keyIdentifier;
    public String valueJavaType;
    public Bilto valueTypeTo;
    public String valueIdentifier;
    public String getterMethod;
    public String setterMethod;
    public String javaType;
    public String javaTypeInternal;
    public String protoWriterFqn;
    private boolean repeated;
  }

  PluginProtos.CodeGeneratorResponse.File generate() {

    List<Descriptors.Descriptor> all = new ArrayList<>(fileDesc);

    GenWriter content = new GenWriter();

    content.println(
      "package " + javaPkgFqn + ";",
      "import io.vertx.protobuf.Visitor;",
      "import io.vertx.protobuf.schema.MessageType;",
      "import io.vertx.protobuf.schema.Field;",
      "",
      "public class ProtoWriter {");

    for (Descriptors.Descriptor d : all) {
      content.println(
        "  public static void emit(" + Utils.javaTypeOf(d) + " value, Visitor visitor) {",
        "    visitor.init(SchemaLiterals." + Utils.schemaLiteralOf(d) + ");",
        "    visit(value, visitor);",
        "    visitor.destroy();",
        "  }");
    }

    for (Descriptors.Descriptor d : all) {

      List<FieldDescriptor> fields = new ArrayList<>();
      for (Descriptors.FieldDescriptor fd : Utils.actualFields(d)) {
        FieldDescriptor field = new FieldDescriptor();
        field.identifier = Utils.schemaLiteralOf(fd);
        field.typeTo = TYPE_TO.get(fd.getType());
        field.javaType = Utils.javaTypeOf(fd);
        field.javaTypeInternal = Utils.javaTypeOfInternal(fd);
        field.getterMethod = Utils.getterOf(fd);
        field.setterMethod = Utils.setterOf(fd);
        field.repeated = fd.isRepeated();
        field.protoWriterFqn = fd.getType() == Descriptors.FieldDescriptor.Type.MESSAGE ? Utils.extractJavaPkgFqn(fd.getMessageType().getFile()) + ".ProtoWriter" : null;

        if (fd.isMapField()) {
          field.map = true;
          field.keyJavaType = Utils.javaTypeOf(fd.getMessageType().getFields().get(0));
          field.keyTypeTo = TYPE_TO.get(fd.getMessageType().getFields().get(0).getType());
          field.keyIdentifier = Utils.schemaLiteralOf(fd.getMessageType().getFields().get(0));
          field.valueJavaType = Utils.javaTypeOf(fd.getMessageType().getFields().get(1));
          field.valueTypeTo = TYPE_TO.get(fd.getMessageType().getFields().get(1).getType());
          field.valueIdentifier = Utils.schemaLiteralOf(fd.getMessageType().getFields().get(1));
        } else {
          field.map = false;
        }

        fields.add(field);
      }

      content.println("  public static void visit(" + Utils.javaTypeOf(d) + " value, Visitor visitor) {");
      for (FieldDescriptor field : fields) {
        content.println("    if (value." + field.getterMethod + "() != null) {");
        content.println("      ", field.javaType + " v = value." + field.getterMethod + "();");
        if (field.typeTo == null) {
          // Message
          if (field.map) {
            content.println("      for (java.util.Map.Entry<" + field.keyJavaType + ", " + field.valueJavaType + "> entry : v.entrySet()) {");
            content.println("        visitor.enter(SchemaLiterals." + field.identifier + ");");
            content.println("        visitor." + field.keyTypeTo.visitMethod + "(SchemaLiterals." + field.keyIdentifier + ", " + field.keyTypeTo.fn.apply("entry.getKey()") + ");");
            if (field.valueTypeTo == null) {
              // Message
              content.println(
                "        visitor.enter(SchemaLiterals." + field.valueIdentifier + ");",
                "        visit(entry.getValue(), visitor);",
                "        visitor.leave(SchemaLiterals." + field.valueIdentifier + ");");
            } else {
              content.println("        visitor." + field.valueTypeTo.visitMethod + "(SchemaLiterals." + field.valueIdentifier + ", " + field.valueTypeTo.fn.apply("entry.getValue()") + ");");
            }
            content.println(
              "        visitor.leave(SchemaLiterals." + field.identifier + ");",
              "      }");
          } else {
            if (field.repeated) {
              content.println(
                "      for (" + field.javaTypeInternal + " c : v) {",
                "        visitor.enter(SchemaLiterals." + field.identifier + ");",
                "        " + field.protoWriterFqn + ".visit(c, visitor);",
                "        visitor.leave(SchemaLiterals." + field.identifier + ");",
                "      }");
            } else {
              content.println(
                "      visitor.enter(SchemaLiterals." + field.identifier + ");",
                "      " + field.protoWriterFqn + ".visit(v, visitor);",
                "      visitor.leave(SchemaLiterals." + field.identifier + ");");
            }
          }
        } else {
          if (field.repeated) {
            content.println(
              "      for (" + field.javaTypeInternal + " c : v) {",
              "        visitor." + field.typeTo.visitMethod + "(SchemaLiterals." + field.identifier + ", " + field.typeTo.fn.apply("c") + ");",
              "      }");
          } else {
            content.println("      visitor." + field.typeTo.visitMethod + "(SchemaLiterals." + field.identifier + ", " + field.typeTo.fn.apply("v") + ");");
          }
        }
        content.println("    }");
      }
      content.println("  }");
    }

    content.println("}");
    return PluginProtos.CodeGeneratorResponse.File
      .newBuilder()
      .setName(Utils.absoluteFileName(javaPkgFqn, "ProtoWriter"))
      .setContent(content.toString())
      .build();
  }

}
