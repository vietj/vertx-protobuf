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
    final boolean lengthDelimited;
    Bilto(String visitMethod, Function<String, String> fn, Descriptors.FieldDescriptor.Type type) {
      this.visitMethod = visitMethod;
      this.fn = fn;
      this.type = type;
      this.lengthDelimited = (type == Descriptors.FieldDescriptor.Type.BYTES) || (type == Descriptors.FieldDescriptor.Type.STRING) || (type == Descriptors.FieldDescriptor.Type.MESSAGE);
    }
    Bilto(String visitMethod, Descriptors.FieldDescriptor.Type type) {
      this(visitMethod, Function.identity(), type);
    }
  }

  private static final Map<Descriptors.FieldDescriptor.Type, Bilto> TYPE_TO = new HashMap<>();

  static {
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.BYTES, new Bilto("visitBytes", s -> s + ".getBytes()", Descriptors.FieldDescriptor.Type.BYTES));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.FLOAT, new Bilto("visitFloat", Descriptors.FieldDescriptor.Type.FLOAT));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.DOUBLE, new Bilto("visitDouble", Descriptors.FieldDescriptor.Type.DOUBLE));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.STRING, new Bilto("visitString", Descriptors.FieldDescriptor.Type.STRING));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.BOOL, new Bilto("visitBool", Descriptors.FieldDescriptor.Type.BOOL));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.ENUM, new Bilto("visitEnum", s -> s + ".number()", Descriptors.FieldDescriptor.Type.ENUM));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.INT32, new Bilto("visitInt32", Descriptors.FieldDescriptor.Type.INT32));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.UINT32, new Bilto("visitUInt32", Descriptors.FieldDescriptor.Type.UINT32));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.INT64, new Bilto("visitInt64", Descriptors.FieldDescriptor.Type.INT64));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.UINT64, new Bilto("visitUInt64", Descriptors.FieldDescriptor.Type.UINT64));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.SINT32, new Bilto("visitSInt32", Descriptors.FieldDescriptor.Type.SINT32));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.SINT64, new Bilto("visitSInt64", Descriptors.FieldDescriptor.Type.SINT64));
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

  static class Property {
    public String getterMethod;
    public String setterMethod;
    public String fieldName;
    public String javaType;
    public String javaTypeInternal;
    public Function<String, String> defaultValueChecker = s -> s + "." + this.getterMethod + "()" + " != null";
  }

  static class FieldProperty extends Property {
    public Bilto typeTo;
    public boolean map;
    public String identifier;
    public String keyJavaType;
    public Bilto keyTypeTo;
    public String keyIdentifier;
    public String valueJavaType;
    public Bilto valueTypeTo;
    public String valueIdentifier;
    public String protoWriterFqn;
    private boolean repeated;
    private boolean packed;

    // OneOf
    public String discriminant;
    public String typeName;
  }

  static class OneofProperty extends Property {
    final List<FieldProperty> fields = new ArrayList<>();
  }

  PluginProtos.CodeGeneratorResponse.File generate() {

    List<Descriptors.Descriptor> all = new ArrayList<>(fileDesc);

    GenWriter content = new GenWriter();

    content.println(
      "package " + javaPkgFqn + ";",
      "import io.vertx.protobuf.RecordVisitor;",
      "import io.vertx.protobuf.schema.MessageType;",
      "import io.vertx.protobuf.schema.Field;",
      "",
      "public class ProtoWriter {");

    for (Descriptors.Descriptor d : all) {
      content.println(
        "  public static void emit(" + Utils.javaTypeOf(d) + " value, RecordVisitor visitor) {",
        "    visitor.init(SchemaLiterals.MessageLiteral." + Utils.literalIdentifier(d) + ");",
        "    visit(value, visitor);",
        "    visitor.destroy();",
        "  }");
    }

    for (Descriptors.Descriptor d : all) {

      Map<Descriptors.OneofDescriptor, OneofProperty> blah = new HashMap<>();
      Map<Descriptors.FieldDescriptor, OneofProperty> oneOfs__ = new HashMap<>();
      Utils.oneOfs(d).forEach(oneOf -> oneOf.getFields().forEach(f -> {
        oneOfs__.put(f, blah.computeIfAbsent(oneOf, k -> new OneofProperty()));
      }));

      List<Property> props = new ArrayList<>();
      for (Descriptors.FieldDescriptor fd : d.getFields()) {
        FieldProperty field = new FieldProperty();
        field.identifier = Utils.literalIdentifier(fd);
        field.typeTo = TYPE_TO.get(fd.getType());
        field.javaType = Utils.javaTypeOf(fd);
        field.javaTypeInternal = Utils.javaTypeOfInternal(fd);
        field.getterMethod = Utils.getterOf(fd);
        field.setterMethod = Utils.setterOf(fd);
        field.fieldName = fd.getJsonName();
        field.repeated = fd.isRepeated();
        field.packed = fd.isPacked();
        field.protoWriterFqn = fd.getType() == Descriptors.FieldDescriptor.Type.MESSAGE ? Utils.extractJavaPkgFqn(fd.getMessageType().getFile()) + ".ProtoWriter" : null;

        if (fd.isMapField()) {
          field.map = true;
          field.keyJavaType = Utils.javaTypeOf(fd.getMessageType().getFields().get(0));
          field.keyTypeTo = TYPE_TO.get(fd.getMessageType().getFields().get(0).getType());
          field.keyIdentifier = Utils.literalIdentifier(fd.getMessageType().getFields().get(0));
          field.valueJavaType = Utils.javaTypeOf(fd.getMessageType().getFields().get(1));
          field.valueTypeTo = TYPE_TO.get(fd.getMessageType().getFields().get(1).getType());
          field.valueIdentifier = Utils.literalIdentifier(fd.getMessageType().getFields().get(1));
        } else {
          field.map = false;
          if (fd.isRepeated()) {
            field.defaultValueChecker = s -> "!" + s + "." + field.getterMethod + "().isEmpty()";
          } else {
            if (Utils.isOptional(fd)) {
              field.defaultValueChecker = s -> s + "." + field.fieldName + " != null";
            } else {
              switch (fd.getType()) {
                case INT32:
                case UINT32:
                case SINT32:
                case FIXED32:
                case SFIXED32:
                  field.defaultValueChecker = s -> s + "." + field.getterMethod + "() != 0";
                  break;
                case INT64:
                case UINT64:
                case SINT64:
                case FIXED64:
                case SFIXED64:
                  field.defaultValueChecker = s -> s + "." + field.getterMethod + "() != 0L";
                  break;
                case FLOAT:
                  field.defaultValueChecker = s -> s + "." + field.getterMethod + "() != 0F";
                  break;
                case DOUBLE:
                  field.defaultValueChecker = s -> s + "." + field.getterMethod + "() != 0D";
                  break;
                case STRING:
                  field.defaultValueChecker = s -> "!" + s + "." + field.getterMethod + "().isEmpty()";
                  break;
                case BOOL:
                  field.defaultValueChecker = s -> s + "." + field.getterMethod + "()";
                  break;
                case ENUM:
                  field.defaultValueChecker = s -> s + "." + field.getterMethod + "() != " + Utils.javaTypeOf(fd) + "." + Utils.defaultEnumValue(fd.getEnumType()).getName();
                  break;
                case BYTES:
                  field.defaultValueChecker = s -> s + "." + field.getterMethod + "().length() != 0";
                  break;
              }
            }
          }
        }

        OneofProperty oneOf = oneOfs__.get(fd);
        if (oneOf != null) {
          field.discriminant = fd.getName().toUpperCase();
          field.typeName = Utils.oneOfTypeName(fd);
          oneOf.fields.add((field));
        } else {
          props.add(field);
        }
      }
      blah.forEach((a, b) -> {
        b.getterMethod = Utils.getterOf(a);
        b.setterMethod = Utils.setterOf(a);
        b.javaType = "";
        b.javaTypeInternal = "";
        props.add(b);
      });

      content.println("  public static void visit(" + Utils.javaTypeOf(d) + " value, RecordVisitor visitor) {");
      for (Property property : props) {
        content.println("    if (" + property.defaultValueChecker.apply("value") + ") {");
        if (property instanceof FieldProperty) {
          FieldProperty field = (FieldProperty) property;
          content.println("      " + field.javaType + " v = value." + field.getterMethod + "();");
          gen(content, field);
        } else {
          OneofProperty oneof = (OneofProperty)property;
          content.println("      switch (value." + property.getterMethod + "().discriminant()) {");
          oneof.fields.forEach(field -> {
            content.println("        case " + field.discriminant + ": {");
            content.println("          " + field.javaType + " v = value." + property.getterMethod + "().as" + field.typeName + "().get();");
            content.margin(4);
            gen(content, field);
            content.margin(0);
            content.println("          break;");
            content.println("        }");
          });
          content.println("        default:");
          content.println("          throw new AssertionError();");
          content.println("        }");
        }
        content.println("    }");
      }

      content.println(
        "    java.util.Map<io.vertx.protobuf.schema.Field, java.util.List<Object>> unknownFields = value.unknownFields;",
        "    if (unknownFields != null) {",
        "      for (java.util.Map.Entry<io.vertx.protobuf.schema.Field, java.util.List<Object>> unknownField : unknownFields.entrySet()) {",
        "        for (Object o : unknownField.getValue()) {",
        "          io.vertx.protobuf.schema.Field field = unknownField.getKey();",
        "          switch (field.type().wireType()) {",
        "            case LEN:",
//        "              visitor.enter(field);",
        "              visitor.visitBytes(field, ((io.vertx.core.buffer.Buffer)o).getBytes());",
//        "              visitor.leave(field);",
        "              break;",
        "            case I32:",
        "              visitor.visitI32(field, (Integer)o);",
        "              break;",
        "            case I64:",
        "              visitor.visitI64(field, (Long)o);",
        "              break;",
        "            case VARINT:",
        "              visitor.visitVarInt64(field, (Long)o);",
        "              break;",
        "          }",
        "        }",
        "      }",
        "    }");

      content.println("  }");
    }

    content.println("}");
    return PluginProtos.CodeGeneratorResponse.File
      .newBuilder()
      .setName(Utils.absoluteFileName(javaPkgFqn, "ProtoWriter"))
      .setContent(content.toString())
      .build();
  }

  private void gen(GenWriter content, FieldProperty field) {
    if (field.typeTo == null) {
      // Message
      if (field.map) {
        content.println("      for (java.util.Map.Entry<" + field.keyJavaType + ", " + field.valueJavaType + "> entry : v.entrySet()) {");
        content.println("        visitor.enter(SchemaLiterals.FieldLiteral." + field.identifier + ");");
//        if (field.keyTypeTo.lengthDelimited) {
//          content.println("        visitor.enter(SchemaLiterals.FieldLiteral." + field.keyIdentifier + ");");
//        }
        content.println("        visitor." + field.keyTypeTo.visitMethod + "(SchemaLiterals.FieldLiteral." + field.keyIdentifier + ", " + field.keyTypeTo.fn.apply("entry.getKey()") + ");");
//        if (field.keyTypeTo.lengthDelimited) {
//          content.println("        visitor.leave(SchemaLiterals.FieldLiteral." + field.keyIdentifier + ");");
//        }
        if (field.valueTypeTo == null) {
          // Message
          content.println(
            "        visitor.enter(SchemaLiterals.FieldLiteral." + field.valueIdentifier + ");",
            "        visit(entry.getValue(), visitor);",
            "        visitor.leave(SchemaLiterals.FieldLiteral." + field.valueIdentifier + ");");
        } else {
//          if (field.valueTypeTo.lengthDelimited) {
//            content.println("        visitor.enter(SchemaLiterals.FieldLiteral." + field.valueIdentifier + ");");
//          }
          content.println("        visitor." + field.valueTypeTo.visitMethod + "(SchemaLiterals.FieldLiteral." + field.valueIdentifier + ", " + field.valueTypeTo.fn.apply("entry.getValue()") + ");");
//          if (field.valueTypeTo.lengthDelimited) {
//            content.println("        visitor.leave(SchemaLiterals.FieldLiteral." + field.valueIdentifier + ");");
//          }
        }
        content.println(
          "        visitor.leave(SchemaLiterals.FieldLiteral." + field.identifier + ");",
          "      }");
      } else {
        if (field.repeated) {
          content.println(
            "      for (" + field.javaTypeInternal + " c : v) {",
            "        visitor.enter(SchemaLiterals.FieldLiteral." + field.identifier + ");",
            "        " + field.protoWriterFqn + ".visit(c, visitor);",
            "        visitor.leave(SchemaLiterals.FieldLiteral." + field.identifier + ");",
            "      }");
        } else {
          content.println(
            "      visitor.enter(SchemaLiterals.FieldLiteral." + field.identifier + ");",
            "      " + field.protoWriterFqn + ".visit(v, visitor);",
            "      visitor.leave(SchemaLiterals.FieldLiteral." + field.identifier + ");");
        }
      }
    } else {
      if (field.repeated) {
        if (field.packed) {
          content.println("visitor.enter(SchemaLiterals.FieldLiteral." + field.identifier + ");");
        }
        content.println("      for (" + field.javaTypeInternal + " c : v) {");
//        if (field.typeTo.lengthDelimited) {
//          content.println("        visitor.enter(SchemaLiterals.FieldLiteral." + field.identifier + ");");
//        }
        content.println("        visitor." + field.typeTo.visitMethod + "(SchemaLiterals.FieldLiteral." + field.identifier + ", " + field.typeTo.fn.apply("c") + ");");
//        if (field.typeTo.lengthDelimited) {
//          content.println("        visitor.leave(SchemaLiterals.FieldLiteral." + field.identifier + ");");
//        }
        content.println("      }");
        if (field.packed) {
          content.println("visitor.leave(SchemaLiterals.FieldLiteral." + field.identifier + ");");
        }
      } else {
//        if (field.typeTo.lengthDelimited) {
//          content.println("      visitor.enter(SchemaLiterals.FieldLiteral." + field.identifier + ");");
//        }
        content.println("      visitor." + field.typeTo.visitMethod + "(SchemaLiterals.FieldLiteral." + field.identifier + ", " + field.typeTo.fn.apply("v") + ");");
//        if (field.typeTo.lengthDelimited) {
//          content.println("      visitor.leave(SchemaLiterals.FieldLiteral." + field.identifier + ");");
//        }
      }
    }
  }

}
