package io.vertx.grpc.plugin;

import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;

import java.util.List;
import java.util.stream.Collectors;

class EnumGenerator {

  private final Descriptors.FileDescriptor fileDesc;

  public EnumGenerator(Descriptors.FileDescriptor fileDesc) {
    this.fileDesc = fileDesc;
  }

  public List<PluginProtos.CodeGeneratorResponse.File> generate() {
    String javaPkgFqn = Utils.extractJavaPkgFqn(fileDesc.toProto());
    return fileDesc.getEnumTypes()
      .stream()
      .map(mt -> buildFiles(javaPkgFqn, mt))
      .collect(Collectors.toList());
  }

  private PluginProtos.CodeGeneratorResponse.File buildFiles(String javaPkgFqn, Descriptors.EnumDescriptor enumType) {
    StringBuilder content = new StringBuilder();
    content.append("package ").append(javaPkgFqn).append(";\r\n");
    content.append("public enum ").append(enumType.getName()).append(" {\r\n");
    boolean first = true;
    for (Descriptors.EnumValueDescriptor enumValue : enumType.getValues()) {
      if (first) {
        first = false;
      } else {
        content.append(",\r\n");
      }
      content.append("  ").append(enumValue.getName()).append("(").append(enumValue.getIndex()).append(")");
    }
    if (!first) {
      content.append(";\r\n");
    }
    content.append("  private static final java.util.Map<Integer, ").append(enumType.getName()).append("> BY_INDEX = new java.util.HashMap<>();\r\n");
    content.append("  public static ").append(enumType.getName()).append(" valueOf(int index) {\r\n");
    content.append("    return BY_INDEX.get(index);\r\n");
    content.append("  }\r\n");
    content.append("  static {\r\n");
    for (Descriptors.EnumValueDescriptor enumValue : enumType.getValues()) {
      content.append("    BY_INDEX.put(").append(enumValue.getIndex()).append(",").append(enumValue.getName()).append(");\r\n");
    }
    content.append("  }\r\n");
    content.append("  private final int index;\r\n");
    content.append("  ").append(enumType.getName()).append("(int index) {\r\n");
    content.append("    this.index = index;\r\n");
    content.append("  }\r\n");
    content.append("  public int index() {\r\n");
    content.append("    return index;\r\n");
    content.append("  }\r\n");
    content.append("}\r\n");
    return Utils.buildFile(javaPkgFqn, enumType, content.toString());
  }
}
