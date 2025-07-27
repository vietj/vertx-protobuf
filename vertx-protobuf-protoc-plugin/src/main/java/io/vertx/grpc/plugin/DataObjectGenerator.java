package io.vertx.grpc.plugin;

import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class DataObjectGenerator {

  private final String javaPkgFqn;
  private final List<Descriptors.Descriptor> fileDesc;

  public DataObjectGenerator(String javaPkgFqn, List<Descriptors.Descriptor> fileDesc) {
    this.javaPkgFqn = javaPkgFqn;
    this.fileDesc = fileDesc;
  }

  List<PluginProtos.CodeGeneratorResponse.File> generate() {
    return fileDesc
      .stream()
      .map(mt -> buildFiles(javaPkgFqn, mt))
      .collect(Collectors.toList());
  }

  private PluginProtos.CodeGeneratorResponse.File buildFiles(String javaPkgFqn, Descriptors.Descriptor messageType) {
    StringBuilder content = new StringBuilder();
    content.append("package ").append(javaPkgFqn).append(";\r\n");
    content.append("@io.vertx.codegen.annotations.DataObject\r\n");
    content.append("public class ").append(messageType.getName()).append(" {\r\n");
    messageType.getFields().forEach(fd -> {
      String javaType = Utils.javaTypeOf(fd);
      if (javaType != null) {
        content.append("  private ").append(javaType).append(" ").append(fd.getJsonName());
        content.append(";\r\n");
      }
    });
    content.append("  public ").append(messageType.getName()).append(" init() {\r\n");
    messageType.getFields().forEach(field -> {
      if (field.getType() == Descriptors.FieldDescriptor.Type.ENUM && !field.isRepeated()) {
        content.append("    this.").append(field.getJsonName()).append(" = ").append(Utils.javaTypeOf(field)).append(".valueOf(0);\r\n");
      }
    });
    content.append("    return this;\r\n");
    content.append("  }\r\n");
    messageType.getFields().forEach(field -> {
      String javaType = Utils.javaTypeOf(field);
      if (javaType != null) {
        String getter = Utils.getterOf(field);
        String setter = Utils.setterOf(field);
        content.append("  public ")
          .append(javaType)
          .append(" ")
          .append(getter)
          .append("() { \r\n");
        content.append("    return ").append(field.getJsonName()).append(";\r\n");
        content.append("  };\r\n");
        content.append("  public ")
          .append(messageType.getName())
          .append(" ")
          .append(setter)
          .append("(")
          .append(javaType)
          .append(" ")
          .append(field.getJsonName())
          .append(") { \r\n");
        content.append("    this.").append(field.getJsonName()).append(" = ").append(field.getJsonName()).append(";\n");
        content.append("    return this;\r\n");
        content.append("  };\r\n");
      }
    });
    content.append("}\r\n");
    return Utils.buildFile(javaPkgFqn, messageType, content.toString());
  }
}
