/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.grpc.plugin;

import com.google.common.base.Strings;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;
import com.salesforce.jprotoc.Generator;
import com.salesforce.jprotoc.GeneratorException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VertxGrpcGeneratorImpl extends Generator {

  public VertxGrpcGeneratorImpl() {
  }

  @Override
  protected List<PluginProtos.CodeGeneratorResponse.Feature> supportedFeatures() {
    return Collections.singletonList(PluginProtos.CodeGeneratorResponse.Feature.FEATURE_PROTO3_OPTIONAL);
  }

  private static class Node {
    final DescriptorProtos.FileDescriptorProto fileDescProto;
    final List<Node> dependencies;
    Descriptors.FileDescriptor fileDesc;
    Node(DescriptorProtos.FileDescriptorProto fileDescProto) {
      this.fileDescProto = fileDescProto;
      this.dependencies = new ArrayList<>();
    }
    Descriptors.FileDescriptor build() throws Descriptors.DescriptorValidationException {
      if (fileDesc == null) {
        List<Descriptors.FileDescriptor> deps = new ArrayList<>();
        for (Node dep : dependencies) {
          deps.add(dep.build());
        }
        fileDesc = Descriptors.FileDescriptor.buildFrom(fileDescProto, deps.toArray(new Descriptors.FileDescriptor[0]));
      }
      return fileDesc;
    }
  }

  @Override
  public List<PluginProtos.CodeGeneratorResponse.File> generateFiles(PluginProtos.CodeGeneratorRequest request) throws GeneratorException {

    List<DescriptorProtos.FileDescriptorProto> protosToGenerate = request.getProtoFileList().stream()
      .filter(protoFile -> request.getFileToGenerateList().contains(protoFile.getName()))
      .collect(Collectors.toList());

    Map<String, Node> nodeMap = new LinkedHashMap<>();
    for (DescriptorProtos.FileDescriptorProto fileDescProto : protosToGenerate) {
      nodeMap.put(fileDescProto.getName(), new Node(fileDescProto));
    }
    nodeMap.values().forEach(node -> {
      for (String dependency : node.fileDescProto.getDependencyList()) {
        node.dependencies.add(nodeMap.get(dependency));
      }
    });

    List<PluginProtos.CodeGeneratorResponse.File> files = new ArrayList<>();

    for (Node fileDescProto : nodeMap.values()) {

      Descriptors.FileDescriptor fileDesc;
      try {
        fileDesc = fileDescProto.build();
      } catch (Descriptors.DescriptorValidationException e) {
        GeneratorException ex = new GeneratorException(e.getMessage());
        ex.initCause(e);
        throw ex;
      }
      String javaPkgFqn = extractJavaPkgFqn(fileDescProto.fileDescProto);
      files.addAll(generateDataObjectsFiles(javaPkgFqn, fileDesc));
      files.addAll(generateEnumFiles(javaPkgFqn, fileDesc));
      files.add(generateSchemaFile(javaPkgFqn, fileDesc));
      files.add(generateProtoReaderFile(javaPkgFqn, fileDesc));
      files.add(generateProtoWriterFile(javaPkgFqn, fileDesc));
    }

    return files;
  }

  private static String setterOf(Descriptors.FieldDescriptor field) {
    return "set" + Character.toUpperCase(field.getJsonName().charAt(0)) + field.getJsonName().substring(1);
  }

  private static String getterOf(Descriptors.FieldDescriptor field) {
    return "get" + Character.toUpperCase(field.getJsonName().charAt(0)) + field.getJsonName().substring(1);
  }

  private static String schemaLiteralOf(Descriptors.FieldDescriptor field) {
    return schemaLiteralOf(field.getContainingType()) + "_" + field.getName().toUpperCase();
  }

  private static String schemaLiteralOf(Descriptors.Descriptor field) {
    return field.getName().toUpperCase();
  }

  private static PluginProtos.CodeGeneratorResponse.File generateProtoWriterFile(
          String javaPkgFqn,
          Descriptors.FileDescriptor fileDesc) {
    StringBuilder content = new StringBuilder();

    content.append("package ").append(javaPkgFqn).append(";\r\n");
    content.append("import io.vertx.protobuf.Visitor;\r\n");
    content.append("import io.vertx.protobuf.schema.MessageType;\r\n");
    content.append("import io.vertx.protobuf.schema.Field;\r\n");
    content.append("public class ProtoWriter {\r\n");

    for (Descriptors.Descriptor d : fileDesc.getMessageTypes()) {
      content.append("  public static void emit(").append(d.getName()).append(" value, Visitor visitor) {\r\n");
      content.append("    visitor.init(SchemaLiterals.").append(schemaLiteralOf(d)).append(");\r\n");
      content.append("    visit(value, visitor);\r\n");
      content.append("    visitor.destroy();\r\n");
      content.append("  }\r\n");
    }

    for (Descriptors.Descriptor d : fileDesc.getMessageTypes()) {
      content.append("  public static void visit(").append(d.getName()).append(" value, Visitor visitor) {\r\n");
      for (Descriptors.FieldDescriptor field : d.getFields()) {
        content.append("    if (value.").append(getterOf(field)).append("() != null) {\r\n");
        content.append("      ").append(javaTypeOf(field)).append(" v = value.").append(getterOf(field)).append("();\r\n");
        switch (field.getType()) {
          case MESSAGE:
            if (field.isMapField()) {
              content.append("      for (java.util.Map.Entry<").append(javaTypeOf(field.getMessageType().getFields().get(0))).append(", ").append(javaTypeOf(field.getMessageType().getFields().get(1))).append("> entry : v.entrySet()) {\r\n");
              content.append("        visitor.enter(SchemaLiterals.").append(schemaLiteralOf(field)).append(");\r\n");
              switch (field.getMessageType().getFields().get(0).getType()) {
                case STRING:
                  content.append("        visitor.visitString(SchemaLiterals.").append(schemaLiteralOf(field.getMessageType().getFields().get(0))).append(", entry.getKey());\r\n");
                  break;
                default:
                  throw new UnsupportedOperationException("Handle me");
              }
              if (field.getMessageType().getFields().get(1).getType() != Descriptors.FieldDescriptor.Type.MESSAGE) {
                throw new UnsupportedOperationException("Handle me");
              }
              content.append("        visitor.enter(SchemaLiterals.").append(schemaLiteralOf(field.getMessageType().getFields().get(1))).append(");\r\n");
              content.append("        visit(entry.getValue(), visitor);\r\n");
              content.append("        visitor.leave(SchemaLiterals.").append(schemaLiteralOf(field.getMessageType().getFields().get(1))).append(");\r\n");
              content.append("        visitor.leave(SchemaLiterals.").append(schemaLiteralOf(field)).append(");\r\n");
              content.append("      }\r\n");
            } else {
              content.append("      visitor.enter(SchemaLiterals.").append(schemaLiteralOf(field)).append(");\r\n");
              content.append("      ").append(field.getMessageType().getFile().getOptions().getJavaPackage()).append(".ProtoWriter.visit(v, visitor);\r\n");
              content.append("      visitor.leave(SchemaLiterals.").append(schemaLiteralOf(field)).append(");\r\n");
            }
            break;
          case STRING:
            content.append("      visitor.visitString(SchemaLiterals.").append(schemaLiteralOf(field)).append(", v);\r\n");
            break;
          case DOUBLE:
            content.append("      visitor.visitDouble(SchemaLiterals.").append(schemaLiteralOf(field)).append(", v);\r\n");
            break;
          case BOOL:
            content.append("      visitor.visitVarInt32(SchemaLiterals.").append(schemaLiteralOf(field)).append(", v ? 1 : 0);\r\n");
            break;
          case ENUM:
            content.append("      visitor.visitVarInt32(SchemaLiterals.").append(schemaLiteralOf(field)).append(", v.index());\r\n");
            break;
          default:
            content.append("      // Handle field name=").append(field.getName()).append(" type=").append(field.getType()).append("\r\n");
            break;
        }
        content.append("    }\r\n");
      }
      content.append("  }\r\n");
    }

    content.append("}\r\n");
    return PluginProtos.CodeGeneratorResponse.File
            .newBuilder()
            .setName(absoluteFileName(javaPkgFqn, "ProtoWriter"))
            .setContent(content.toString())
            .build();
  }

  private static PluginProtos.CodeGeneratorResponse.File generateProtoReaderFile(
          String javaPkgFqn,
          Descriptors.FileDescriptor fileDesc) {
    StringBuilder content = new StringBuilder();

    content.append("package ").append(javaPkgFqn).append(";\r\n");
    content.append("import io.vertx.protobuf.Visitor;\r\n");
    content.append("import io.vertx.protobuf.schema.MessageType;\r\n");
    content.append("import io.vertx.protobuf.schema.Field;\r\n");
    content.append("import java.util.Deque;\r\n");
    content.append("import java.util.ArrayDeque;\r\n");
    content.append("public class ProtoReader implements Visitor {\r\n");

    content.append("  public final Deque<Object> stack;\r\n");
    content.append("  private Visitor next;");
    content.append("  public ProtoReader(Deque<Object> stack) {\r\n");
    content.append("    this.stack = stack;\r\n");
    content.append("  }\r\n");
    content.append("  public ProtoReader() {\r\n");
    content.append("    this(new ArrayDeque<>());\r\n");
    content.append("  }\r\n");

    // **************
    // VISIT STRING
    // **************

    content.append("  public void init(MessageType type) {\r\n");
    boolean first = true;
    for (Descriptors.Descriptor messageType : fileDesc.getMessageTypes()) {
      if (first) {
        content.append("    ");
        first = false;
      } else {
        content.append(" else ");
      }
      content.append("if (type == SchemaLiterals.").append(schemaLiteralOf(messageType)).append(") {\r\n");
      content.append("      stack.push(new ").append(messageType.getName()).append("());\r\n");
      content.append("    }");
    }
    if (first) {
      content.append("    ");
    } else {
      content.append(" else ");
    }
    content.append("if (next != null) {\r\n");
    content.append("      next.init(type);\r\n");
    content.append("    }\r\n");
    content.append("  }\r\n");

    // **************
    // VISIT STRING
    // **************

    class Foo {
      final String methodStart;
      final Set<Descriptors.FieldDescriptor.Type> types;
      final String next;
      Foo(String methodStart, String next, Descriptors.FieldDescriptor.Type... types) {
        this.methodStart = methodStart;
        this.types = new HashSet<>(Arrays.asList(types));
        this.next = next;
      }
    }

    Foo[] foos = {
      new Foo("visitString(Field field, String value)", "visitString(field, value)", Descriptors.FieldDescriptor.Type.STRING),
      new Foo("visitDouble(Field field, double value)", "visitDouble(field, value)", Descriptors.FieldDescriptor.Type.DOUBLE),
      new Foo("visitVarInt32(Field field, int value)", "visitVarInt32(field, value)", Descriptors.FieldDescriptor.Type.BOOL, Descriptors.FieldDescriptor.Type.ENUM)
    };

    for (Foo foo : foos) {
      content.append("  public void ").append(foo.methodStart).append(" {\r\n");
      first = true;
      for (Descriptors.Descriptor mt : fileDesc.getMessageTypes()) {
        for (Descriptors.FieldDescriptor fd : mt.getFields()) {
          if (foo.types.contains(fd.getType())) {
            if (first) {
              content.append("    ");
              first = false;
            } else {
              content.append(" else ");
            }
            content.append("if (field == SchemaLiterals.").append(schemaLiteralOf(fd)).append(") {\r\n");
            Function<String, String> converter = Function.identity();
            switch (fd.getType()) {
              case BOOL:
                converter = s -> "value == 1";
                break;
              case ENUM:
                converter = s -> javaTypeOf(fd) + ".valueOf(" + s + ")";
            }
            content.append("      ((").append(javaTypeOf(fd.getContainingType())).append(")stack.peek()).").append(setterOf(fd)).append("(").append(converter.apply("value")).append(");\t\n");
            content.append("    }");
          }
        }
      }
      if (first) {
        content.append("    ");
      } else {
        content.append(" else ");
      }
      content.append("if (next != null) {\r\n");
      content.append("      next.").append(foo.next).append(";\r\n");
      content.append("    } else {\r\n");
      content.append("      stack.push(value);\r\n");
      content.append("    }\r\n");
      content.append("  }\r\n");
    }

    Map<String, Descriptors.Descriptor> all = transitiveClosure(fileDesc.getMessageTypes());

    // **************
    // ENTER
    // **************

    content.append("  public void enter(Field field) {\r\n");
    first = true;
    for (Descriptors.Descriptor messageType : all.values()) {
      for (Descriptors.FieldDescriptor field : messageType.getFields()) {
        if (field.getJavaType() != Descriptors.FieldDescriptor.JavaType.MESSAGE) {
          continue;
        }
        if (first) {
          content.append("    ");
          first = false;
        } else {
          content.append(" else ");
        }
        content.append("if (field == SchemaLiterals.").append(schemaLiteralOf(field)).append(") {\r\n");
        if (field.isMapField()) {
          content.append("      ").append(field.getContainingType().getName()).append(" container = (").append(field.getContainingType().getName()).append(")stack.peek();").append("\r\n");
          content.append("      stack.push(container.").append(getterOf(field)).append("());\r\n");
        } else {
          if (field.getType() != Descriptors.FieldDescriptor.Type.MESSAGE || field.getMessageType().getFile() == fileDesc) {
            content.append("      ").append(javaTypeOf(field)).append(" v = new ").append(javaTypeOf(field)).append("();\r\n");
            content.append("      stack.push(v);\r\n");
          } else {
            content.append("      Visitor v = new ").append(field.getMessageType().getFile().getOptions().getJavaPackage()).append(".ProtoReader(stack);\r\n");
            content.append("      v.init((MessageType)field.type);\r\n");
            content.append("      next = v;\r\n");
          }
        }
        content.append("    }");
      }
    }
    if (first) {
      content.append("    ");
    } else {
      content.append(" else ");
    }
    content.append("if (next != null) {\r\n");
    content.append("      next.enter(field);\r\n");
    content.append("    }\r\n");
    content.append("  }\r\n");

    // **************
    // VISIT LEAVE
    // **************

    content.append("  public void leave(Field field) {\r\n");
    first = true;
    for (Descriptors.Descriptor messageType : fileDesc.getMessageTypes()) {
      for (Descriptors.FieldDescriptor field : messageType.getFields()) {
        if (field.getType() != Descriptors.FieldDescriptor.Type.MESSAGE) {
          continue;
        }
        if (first) {
          content.append("    ");
          first = false;
        } else {
          content.append(" else ");
        }
        content.append("if (field == SchemaLiterals.").append(schemaLiteralOf(field)).append(") {\r\n");
        if (field.isMapField()) {
          content.append("      Object value = stack.pop();\r\n");
          content.append("      Object key = stack.pop();\r\n");
          content.append("      java.util.Map entries = (java.util.Map)stack.pop();\r\n");
          content.append("      entries.put(key, value);\r\n");
        } else {
          if (field.getMessageType().getFile() != fileDesc) {
            content.append("      next.destroy();\r\n");
            content.append("      next = null;\r\n");
          }
          content.append("      ").append(javaTypeOf(field)).append(" v = (").append(javaTypeOf(field)).append(")stack.pop();\r\n");
          content.append("      ((").append(messageType.getName()).append(")stack.peek()).").append(setterOf(field)).append("(v);\n");
        }
        content.append("    }");
      }
    }
    if (first) {
      content.append("    ");
    } else {
      content.append(" else ");
    }
    content.append("if (next != null) {\r\n");
    content.append("      next.leave(field);\r\n");
    content.append("    }\r\n");
    content.append("  }\r\n");

    // **************
    // DESTROY
    // **************

    content.append("  public void destroy() {\r\n");
    content.append("    if (next != null) {\r\n");
    content.append("      next.destroy();\r\n");
    content.append("    }\r\n");
    content.append("  }\r\n");

    content.append("}\r\n");

    return PluginProtos.CodeGeneratorResponse.File
            .newBuilder()
            .setName(absoluteFileName(javaPkgFqn, "ProtoReader"))
            .setContent(content.toString())
            .build();
  }

  private static Map<String, Descriptors.Descriptor> transitiveClosure(List<Descriptors.Descriptor> descriptors) {
    Map<String, Descriptors.Descriptor> all = new LinkedHashMap<>();
    descriptors.forEach(messageType -> {
      transitiveClosure(messageType, all);
    });
    return all;
  }

  private static void transitiveClosure(Descriptors.Descriptor descriptor, Map<String, Descriptors.Descriptor> result) {
    result.put(descriptor.getName(), descriptor);
    descriptor.getNestedTypes().forEach(nested -> transitiveClosure(nested, result));
  }

  private static PluginProtos.CodeGeneratorResponse.File generateSchemaFile(
          String javaPkgFqn,
          Descriptors.FileDescriptor file) {

    StringBuilder content = new StringBuilder();

    content.append("package ").append(javaPkgFqn).append(";\r\n");
    content.append("import io.vertx.protobuf.schema.Schema;\r\n");
    content.append("import io.vertx.protobuf.schema.MessageType;\r\n");
    content.append("import io.vertx.protobuf.schema.ScalarType;\r\n");
    content.append("import io.vertx.protobuf.schema.EnumType;\r\n");
    content.append("import io.vertx.protobuf.schema.Field;\r\n");
    content.append("public class SchemaLiterals {\r\n");

    content.append("  public static final Schema SCHEMA = new Schema();\r\n");

    Map<String, Descriptors.Descriptor> all = transitiveClosure(file.getMessageTypes());

    all.values().forEach(messageType -> {

      content.append("  public static final MessageType ").append(schemaLiteralOf(messageType)).append(" = SCHEMA.of(\"").append(messageType.getName()).append("\");\r\n");
      messageType.getFields().forEach(field -> {
        switch (field.getJavaType()) {
          case DOUBLE:
            content.append("  public static final Field ").append(schemaLiteralOf(field)).append(" = ").append(schemaLiteralOf(messageType)).append(".addField(").append(field.getNumber()).append(", ScalarType.DOUBLE);\r\n");
            break;
          case BOOLEAN:
            content.append("  public static final Field ").append(schemaLiteralOf(field)).append(" = ").append(schemaLiteralOf(messageType)).append(".addField(").append(field.getNumber()).append(", ScalarType.BOOL);\r\n");
            break;
          case STRING:
            content.append("  public static final Field ").append(schemaLiteralOf(field)).append(" = ").append(schemaLiteralOf(messageType)).append(".addField(").append(field.getNumber()).append(", ScalarType.STRING);\r\n");
            break;
          case ENUM:
            content.append("  public static final Field ").append(schemaLiteralOf(field)).append(" = ").append(schemaLiteralOf(messageType)).append(".addField(").append(field.getNumber()).append(", new EnumType());\r\n");
            break;
          case MESSAGE:
            String prefix;
            if (field.getMessageType().getFile() != file) {
              prefix = field.getMessageType().getFile().getOptions().getJavaPackage() + ".SchemaLiterals.";
            } else {
              prefix = "";
            }
            content.append("  public static final Field ").append(schemaLiteralOf(field)).append(" = ").append(schemaLiteralOf(messageType)).append(".addField(").append(field.getNumber()).append(", ").append(prefix).append("SCHEMA.of(\"").append(field.getMessageType().getName()).append("\"));\r\n");
            break;
        }
      });

    });

    content.append("}\r\n");

    return PluginProtos.CodeGeneratorResponse.File
      .newBuilder()
      .setName(absoluteFileName(javaPkgFqn, "SchemaLiterals"))
      .setContent(content.toString())
      .build();
  }

  private static String extractJavaPkgFqn(Descriptors.FileDescriptor proto) {
    DescriptorProtos.FileOptions options = proto.getOptions();
    String javaPackage = options.getJavaPackage();
    if (!Strings.isNullOrEmpty(javaPackage)) {
      return javaPackage;
    }
    return Strings.nullToEmpty(proto.getPackage());
  }

  private String extractJavaPkgFqn(DescriptorProtos.FileDescriptorProto proto) {
    DescriptorProtos.FileOptions options = proto.getOptions();
    String javaPackage = options.getJavaPackage();
    if (!Strings.isNullOrEmpty(javaPackage)) {
      return javaPackage;
    }
    return Strings.nullToEmpty(proto.getPackage());
  }

  private List<PluginProtos.CodeGeneratorResponse.File> generateDataObjectsFiles(String javaPkgFqn, Descriptors.FileDescriptor fileDesc) {
    return fileDesc.getMessageTypes()
            .stream()
            .map(mt -> buildFiles(javaPkgFqn, mt))
            .collect(Collectors.toList());
  }

  private List<PluginProtos.CodeGeneratorResponse.File> generateEnumFiles(String javaPkgFqn, Descriptors.FileDescriptor fileDesc) {
    return fileDesc.getEnumTypes()
            .stream()
            .map(mt -> buildFiles(javaPkgFqn, mt))
            .collect(Collectors.toList());
  }

  private static String javaTypeOf(Descriptors.Descriptor mt) {
    String pkg = extractJavaPkgFqn(mt.getFile());
    return pkg + "." + mt.getName();
  }

  private static String javaTypeOf(Descriptors.FieldDescriptor field) {
    String pkg;
    switch (field.getJavaType()) {
      case BOOLEAN:
        return "java.lang.Boolean";
      case STRING:
        return "java.lang.String";
      case DOUBLE:
        return "java.lang.Double";
      case ENUM:
        pkg = extractJavaPkgFqn(field.getEnumType().getFile());
        return pkg + "." + field.getEnumType().getName();
      case MESSAGE:
        if (field.isMapField()) {
          String keyType = javaTypeOf(field.getMessageType().getFields().get(0));
          String valueType = javaTypeOf(field.getMessageType().getFields().get(1));
          return "java.util.Map<" + keyType + ", " + valueType + ">";
        } else {
          pkg = extractJavaPkgFqn(field.getMessageType().getFile());
          return pkg + "." + field.getMessageType().getName();
        }
      default:
        return null;
    }
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
    return buildFile(javaPkgFqn, enumType, content.toString());
  }

  private PluginProtos.CodeGeneratorResponse.File buildFiles(String javaPkgFqn, Descriptors.Descriptor messageType) {
    StringBuilder content = new StringBuilder();
    content.append("package ").append(javaPkgFqn).append(";\r\n");
    content.append("@io.vertx.codegen.annotations.DataObject\r\n");
    content.append("public class ").append(messageType.getName()).append(" {\r\n");
    messageType.getFields().forEach(fd -> {
      String javaType = javaTypeOf(fd);
      if (javaType != null) {
        content.append("  private ").append(javaType).append(" ").append(fd.getJsonName());
        if (fd.isMapField()) {
          content.append(" = new java.util.HashMap()");
        }
        content.append(";\r\n");
      }
    });
    messageType.getFields().forEach(field -> {
      String javaType = javaTypeOf(field);
      if (javaType != null) {
        String getter = getterOf(field);
        String setter = setterOf(field);
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
    return buildFile(javaPkgFqn, messageType, content.toString());
  }

  private PluginProtos.CodeGeneratorResponse.File buildFile(String javaPkgFqn, Descriptors.GenericDescriptor messageType, String content) {
    return PluginProtos.CodeGeneratorResponse.File
      .newBuilder()
      .setName(absoluteFileName(javaPkgFqn, messageType))
      .setContent(content)
      .build();
  }

  private static String absoluteFileName(String javaPkgFqn, Descriptors.GenericDescriptor messageType) {
    return absoluteFileName(javaPkgFqn, messageType.getName());
  }

  private static String absoluteFileName(String javaPkgFqn, String simpleName) {
    String dir = javaPkgFqn.replace('.', '/');
    if (dir.isEmpty()) {
      return simpleName + ".java";
    } else {
      return dir + "/" + simpleName + ".java";
    }
  }
}
