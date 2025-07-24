package io.vertx.grpc.plugin;

import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;

import java.util.Map;

class SchemaGenerator {

  private final Descriptors.FileDescriptor file;

  public SchemaGenerator(Descriptors.FileDescriptor file) {
    this.file = file;
  }

  PluginProtos.CodeGeneratorResponse.File generate() {

    String javaPkgFqn = Utils.extractJavaPkgFqn(file.toProto());

    StringBuilder content = new StringBuilder();

    content.append("package ").append(javaPkgFqn).append(";\r\n");
    content.append("import io.vertx.protobuf.schema.Schema;\r\n");
    content.append("import io.vertx.protobuf.schema.MessageType;\r\n");
    content.append("import io.vertx.protobuf.schema.ScalarType;\r\n");
    content.append("import io.vertx.protobuf.schema.EnumType;\r\n");
    content.append("import io.vertx.protobuf.schema.Field;\r\n");
    content.append("public class SchemaLiterals {\r\n");

    content.append("  public static final Schema SCHEMA = new Schema();\r\n");

    Map<String, Descriptors.Descriptor> all = Utils.transitiveClosure(file.getMessageTypes());

    all.values().forEach(messageType -> {

      content.append("  public static final MessageType ").append(Utils.schemaLiteralOf(messageType)).append(" = SCHEMA.of(\"").append(messageType.getName()).append("\");\r\n");
      messageType.getFields().forEach(field -> {
        switch (field.getType()) {
          case DOUBLE:
            content.append("  public static final Field ").append(Utils.schemaLiteralOf(field)).append(" = ").append(Utils.schemaLiteralOf(messageType)).append(".addField(").append(field.getNumber()).append(", ScalarType.DOUBLE);\r\n");
            break;
          case BOOL:
            content.append("  public static final Field ").append(Utils.schemaLiteralOf(field)).append(" = ").append(Utils.schemaLiteralOf(messageType)).append(".addField(").append(field.getNumber()).append(", ScalarType.BOOL);\r\n");
            break;
          case STRING:
            content.append("  public static final Field ").append(Utils.schemaLiteralOf(field)).append(" = ").append(Utils.schemaLiteralOf(messageType)).append(".addField(").append(field.getNumber()).append(", ScalarType.STRING);\r\n");
            break;
          case ENUM:
            content.append("  public static final Field ").append(Utils.schemaLiteralOf(field)).append(" = ").append(Utils.schemaLiteralOf(messageType)).append(".addField(").append(field.getNumber()).append(", new EnumType());\r\n");
            break;
          case BYTES:
            content.append("  public static final Field ").append(Utils.schemaLiteralOf(field)).append(" = ").append(Utils.schemaLiteralOf(messageType)).append(".addField(").append(field.getNumber()).append(", ScalarType.BYTES);\r\n");
            break;
          case INT32:
            content.append("  public static final Field ").append(Utils.schemaLiteralOf(field)).append(" = ").append(Utils.schemaLiteralOf(messageType)).append(".addField(").append(field.getNumber()).append(", ScalarType.INT32);\r\n");
            break;
          case MESSAGE:
            String prefix;
            if (field.getMessageType().getFile() != file) {
              prefix = field.getMessageType().getFile().getOptions().getJavaPackage() + ".SchemaLiterals.";
            } else {
              prefix = "";
            }
            content.append("  public static final Field ").append(Utils.schemaLiteralOf(field)).append(" = ").append(Utils.schemaLiteralOf(messageType)).append(".addField(").append(field.getNumber()).append(", ").append(prefix).append("SCHEMA.of(\"").append(field.getMessageType().getName()).append("\"));\r\n");
            break;
        }
      });

    });

    content.append("}\r\n");

    return PluginProtos.CodeGeneratorResponse.File
      .newBuilder()
      .setName(Utils.absoluteFileName(javaPkgFqn, "SchemaLiterals"))
      .setContent(content.toString())
      .build();
  }
}
