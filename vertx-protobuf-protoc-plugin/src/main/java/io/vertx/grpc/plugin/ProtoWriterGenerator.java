package io.vertx.grpc.plugin;

import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class ProtoWriterGenerator {

  static class Bilto {
    final String visitMethod;
    final Function<String, String> fn;
    Bilto(String visitMethod, Function<String, String> fn) {
      this.visitMethod = visitMethod;
      this.fn = fn;
    }
    Bilto(String visitMethod) {
      this.visitMethod = visitMethod;
      this.fn = Function.identity();
    }
  }

  private static final Map<Descriptors.FieldDescriptor.Type, Bilto> TYPE_TO = new HashMap<>();

  static {
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.BYTES, new Bilto("visitBytes", s -> s + ".getBytes()"));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.FLOAT, new Bilto("visitFloat"));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.DOUBLE, new Bilto("visitDouble"));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.STRING, new Bilto("visitString"));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.BOOL, new Bilto("visitVarInt32", s -> s + "? 1 : 0"));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.ENUM, new Bilto("visitVarInt32", s -> s + ".index()"));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.INT32, new Bilto("visitVarInt32"));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.UINT32, new Bilto("visitVarInt32"));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.INT64, new Bilto("visitVarInt64"));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.UINT64, new Bilto("visitVarInt64"));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.SINT32, new Bilto("visitVarInt32"));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.SINT64, new Bilto("visitVarInt64"));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.FIXED32, new Bilto("visitFixed32"));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.FIXED64, new Bilto("visitFixed64"));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.SFIXED32, new Bilto("visitSFixed32"));
    TYPE_TO.put(Descriptors.FieldDescriptor.Type.SFIXED64, new Bilto("visitSFixed64"));
  }

  private final Descriptors.FileDescriptor fileDesc;

  public ProtoWriterGenerator(Descriptors.FileDescriptor fileDesc) {
    this.fileDesc = fileDesc;
  }

  PluginProtos.CodeGeneratorResponse.File generate() {

    String javaPkgFqn = Utils.extractJavaPkgFqn(fileDesc.toProto());

    StringBuilder content = new StringBuilder();

    content.append("package ").append(javaPkgFqn).append(";\r\n");
    content.append("import io.vertx.protobuf.Visitor;\r\n");
    content.append("import io.vertx.protobuf.schema.MessageType;\r\n");
    content.append("import io.vertx.protobuf.schema.Field;\r\n");
    content.append("public class ProtoWriter {\r\n");

    for (Descriptors.Descriptor d : fileDesc.getMessageTypes()) {
      content.append("  public static void emit(").append(d.getName()).append(" value, Visitor visitor) {\r\n");
      content.append("    visitor.init(SchemaLiterals.").append(Utils.schemaLiteralOf(d)).append(");\r\n");
      content.append("    visit(value, visitor);\r\n");
      content.append("    visitor.destroy();\r\n");
      content.append("  }\r\n");
    }

    for (Descriptors.Descriptor d : fileDesc.getMessageTypes()) {
      content.append("  public static void visit(").append(d.getName()).append(" value, Visitor visitor) {\r\n");
      for (Descriptors.FieldDescriptor field : d.getFields()) {
        content.append("    if (value.").append(Utils.getterOf(field)).append("() != null) {\r\n");
        content.append("      ").append(Utils.javaTypeOf(field)).append(" v = value.").append(Utils.getterOf(field)).append("();\r\n");
        switch (field.getType()) {
          case MESSAGE:
            if (field.isMapField()) {
              content.append("      for (java.util.Map.Entry<").append(Utils.javaTypeOf(field.getMessageType().getFields().get(0))).append(", ").append(Utils.javaTypeOf(field.getMessageType().getFields().get(1))).append("> entry : v.entrySet()) {\r\n");
              content.append("        visitor.enter(SchemaLiterals.").append(Utils.schemaLiteralOf(field)).append(");\r\n");
              switch (field.getMessageType().getFields().get(0).getType()) {
                default:
                  Bilto res = TYPE_TO.get(field.getMessageType().getFields().get(0).getType());
                  if (res == null) {
                    throw new UnsupportedOperationException("Handle me " + field.getMessageType().getFields().get(0).getType() + " not mapped");
                  }
                  content.append("        visitor.").append(res.visitMethod).append("(SchemaLiterals.").append(Utils.schemaLiteralOf(field.getMessageType().getFields().get(0))).append(", ").append(res.fn.apply("entry.getKey()")).append(");\r\n");
              }
              switch (field.getMessageType().getFields().get(1).getType()) {
                case MESSAGE:
                  content.append("        visitor.enter(SchemaLiterals.").append(Utils.schemaLiteralOf(field.getMessageType().getFields().get(1))).append(");\r\n");
                  content.append("        visit(entry.getValue(), visitor);\r\n");
                  content.append("        visitor.leave(SchemaLiterals.").append(Utils.schemaLiteralOf(field.getMessageType().getFields().get(1))).append(");\r\n");
                  break;
                default:
                  Bilto res = TYPE_TO.get(field.getMessageType().getFields().get(1).getType());
                  if (res == null) {
                    throw new UnsupportedOperationException("Not found " + field.getMessageType().getFields().get(1).getType());
                  } else {
                    content.append("        visitor.").append(res.visitMethod).append("(SchemaLiterals.").append(Utils.schemaLiteralOf(field.getMessageType().getFields().get(1))).append(", ").append(res.fn.apply("entry.getValue()")).append(");\r\n");
                  }
              }
              content.append("        visitor.leave(SchemaLiterals.").append(Utils.schemaLiteralOf(field)).append(");\r\n");
              content.append("      }\r\n");
            } else {
              if (field.isRepeated()) {
                content.append("      for (").append(Utils.javaTypeOfInternal(field)).append(" c : v) {\r\n");
                content.append("        visitor.enter(SchemaLiterals.").append(Utils.schemaLiteralOf(field)).append(");\r\n");
                content.append("        ").append(field.getMessageType().getFile().getOptions().getJavaPackage()).append(".ProtoWriter.visit(c, visitor);\r\n");
                content.append("        visitor.leave(SchemaLiterals.").append(Utils.schemaLiteralOf(field)).append(");\r\n");
                content.append("      }\r\n");
              } else {
                content.append("      visitor.enter(SchemaLiterals.").append(Utils.schemaLiteralOf(field)).append(");\r\n");
                content.append("      ").append(field.getMessageType().getFile().getOptions().getJavaPackage()).append(".ProtoWriter.visit(v, visitor);\r\n");
                content.append("      visitor.leave(SchemaLiterals.").append(Utils.schemaLiteralOf(field)).append(");\r\n");
              }
            }
            break;
          default:
            Bilto res = TYPE_TO.get(field.getType());
            if (res != null) {
              if (field.isRepeated()) {
                content.append("      for (").append(Utils.javaTypeOfInternal(field)).append(" c : v) {\r\n");
                content.append("        visitor.").append(res.visitMethod).append("(SchemaLiterals.").append(Utils.schemaLiteralOf(field)).append(", ").append(res.fn.apply("c")).append(");\r\n");
                content.append("      }\r\n");
              } else {
                content.append("      visitor.").append(res.visitMethod).append("(SchemaLiterals.").append(Utils.schemaLiteralOf(field)).append(", ").append(res.fn.apply("v")).append(");\r\n");
              }
            } else {
              content.append("      // Handle field name=").append(field.getName()).append(" type=").append(field.getType()).append("\r\n");
            }
            break;
        }
        content.append("    }\r\n");
      }
      content.append("  }\r\n");
    }

    content.append("}\r\n");
    return PluginProtos.CodeGeneratorResponse.File
      .newBuilder()
      .setName(Utils.absoluteFileName(javaPkgFqn, "ProtoWriter"))
      .setContent(content.toString())
      .build();
  }

}
