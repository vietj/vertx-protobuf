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
import com.google.protobuf.compiler.PluginProtos;
import com.salesforce.jprotoc.Generator;
import com.salesforce.jprotoc.GeneratorException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VertxGrpcGeneratorImpl extends Generator {

  private static final int SERVICE_NUMBER_OF_PATHS = 2;
  private static final int METHOD_NUMBER_OF_PATHS = 4;

  private final VertxGrpcGenerator options;

  /**
   * Creates a new instance with the specified options.
   *
   * @param options the generator options
   */
  public VertxGrpcGeneratorImpl(VertxGrpcGenerator options) {
    this.options = options != null ? options : new VertxGrpcGenerator();
  }

  private String getServiceJavaDocPrefix() {
    return "    ";
  }

  private String getMethodJavaDocPrefix() {
    return "        ";
  }

  @Override
  protected List<PluginProtos.CodeGeneratorResponse.Feature> supportedFeatures() {
    return Collections.singletonList(PluginProtos.CodeGeneratorResponse.Feature.FEATURE_PROTO3_OPTIONAL);
  }

  @Override
  public List<PluginProtos.CodeGeneratorResponse.File> generateFiles(PluginProtos.CodeGeneratorRequest request) throws GeneratorException {

    List<DescriptorProtos.FileDescriptorProto> protosToGenerate = request.getProtoFileList().stream()
      .filter(protoFile -> request.getFileToGenerateList().contains(protoFile.getName()))
      .collect(Collectors.toList());

    List<PluginProtos.CodeGeneratorResponse.File> files = new ArrayList<>();

    protosToGenerate.forEach(fileProto -> {
      String javaPkgFqn = extractJavaPkgFqn(fileProto);
      List<MessageType> messageTypeList = new ArrayList<>();
      fileProto.getMessageTypeList().forEach(messageType -> {
        messageTypeList.add(new MessageType(javaPkgFqn, messageType));
      });

      files.addAll(generateDataObjectsFiles(messageTypeList));
      files.add(generateSchemaFile(javaPkgFqn, fileProto.getMessageTypeList()));
      files.add(generateProtoReaderFile(javaPkgFqn, fileProto.getMessageTypeList()));
    });

    return files;
  }

  private static String setterOf(String name) {
    return "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }

  private static PluginProtos.CodeGeneratorResponse.File generateProtoReaderFile(
          String javaPkgFqn,
          List<DescriptorProtos.DescriptorProto> messageTypeList) {
    StringBuilder content = new StringBuilder();

    content.append("package ").append(javaPkgFqn).append(";\r\n");
    content.append("import io.vertx.protobuf.Visitor;\r\n");
    content.append("import io.vertx.protobuf.schema.MessageType;\r\n");
    content.append("import io.vertx.protobuf.schema.Field;\r\n");
    content.append("import java.util.Deque;\r\n");
    content.append("import java.util.ArrayDeque;\r\n");
    content.append("public class ProtoReader implements Visitor {\r\n");

    content.append("  public final Deque<Object> stack = new ArrayDeque<>();\r\n");

    content.append("  public void init(MessageType type) {\r\n");
    for (DescriptorProtos.DescriptorProto messageType : messageTypeList) {
      content.append("    if (type == SchemaLiterals.").append(messageType.getName().toUpperCase()).append(") {\r\n");
      content.append("      stack.push(new ").append(messageType.getName()).append("());\r\n");
      content.append("    }\r\n");
    }
    content.append("  }\r\n");

    content.append("  public void visitVarInt32(Field field, int v) {\r\n");
    content.append("  }\r\n");

    content.append("  public void visitString(Field field, String s) {\r\n");
    content.append("      stack.push(s);\r\n");
    content.append("  }\r\n");

    content.append("  public void visitDouble(Field field, double d) {\r\n");
    content.append("  }\r\n");

    content.append("  public void enter(Field field) {\r\n");
    content.append("  }\r\n");

    content.append("  public void leave(Field field) {\r\n");
    for (DescriptorProtos.DescriptorProto messageType : messageTypeList) {
      for (DescriptorProtos.FieldDescriptorProto field : messageType.getFieldList()) {
        content.append("    if (field == SchemaLiterals.").append(messageType.getName().toUpperCase()).append("_").append(field.getName().toUpperCase()).append(") {\r\n");
        content.append("      ").append("String").append(" v = (").append("String").append(")stack.pop();\r\n");
        content.append("      ((").append(messageType.getName()).append(")stack.peek()).").append(setterOf(mixedLower(field.getName()))).append("(v);\n");
        content.append("    }\r\n");
      }
    }
    content.append("  }\r\n");

    content.append("  public void destroy() {\r\n");
    content.append("  }\r\n");

    content.append("}\r\n");

    return PluginProtos.CodeGeneratorResponse.File
            .newBuilder()
            .setName(absoluteFileName(javaPkgFqn, "ProtoReader"))
            .setContent(content.toString())
            .build();
  }

  private static String blah(String s) {
    if (s.startsWith(".")) {
      return s.substring(s.lastIndexOf(".") + 1);
    } else {
      return s;
    }
  }

  private static PluginProtos.CodeGeneratorResponse.File generateSchemaFile(
          String javaPkgFqn,
          List<DescriptorProtos.DescriptorProto> messageTypeList) {

    StringBuilder content = new StringBuilder();

    content.append("package ").append(javaPkgFqn).append(";\r\n");
    content.append("import io.vertx.protobuf.schema.Schema;\r\n");
    content.append("import io.vertx.protobuf.schema.MessageType;\r\n");
    content.append("import io.vertx.protobuf.schema.ScalarType;\r\n");
    content.append("import io.vertx.protobuf.schema.EnumType;\r\n");
    content.append("import io.vertx.protobuf.schema.Field;\r\n");
    content.append("public class SchemaLiterals {\r\n");

    content.append("  public static final Schema SCHEMA = new Schema();\r\n");

    messageTypeList.forEach(messageType -> {

      content.append("  public static final MessageType ").append(messageType.getName().toUpperCase()).append(" = SCHEMA.of(\"").append(messageType.getName()).append("\");\r\n");
      messageType.getFieldList().forEach(field -> {
        switch (field.getType()) {
          case TYPE_DOUBLE:
            content.append("  public static final Field ").append(messageType.getName().toUpperCase()).append("_").append(field.getName().toUpperCase()).append(" = ").append(messageType.getName().toUpperCase()).append(".addField(").append(field.getNumber()).append(", ScalarType.DOUBLE);\r\n");
            break;
          case TYPE_BOOL:
            content.append("  public static final Field ").append(messageType.getName().toUpperCase()).append("_").append(field.getName().toUpperCase()).append(" = ").append(messageType.getName().toUpperCase()).append(".addField(").append(field.getNumber()).append(", ScalarType.BOOL);\r\n");
            break;
          case TYPE_STRING:
            content.append("  public static final Field ").append(messageType.getName().toUpperCase()).append("_").append(field.getName().toUpperCase()).append(" = ").append(messageType.getName().toUpperCase()).append(".addField(").append(field.getNumber()).append(", ScalarType.STRING);\r\n");
            break;
          case TYPE_ENUM:
            content.append("  public static final Field ").append(messageType.getName().toUpperCase()).append("_").append(field.getName().toUpperCase()).append(" = ").append(messageType.getName().toUpperCase()).append(".addField(").append(field.getNumber()).append(", new EnumType());\r\n");
            break;
          case TYPE_MESSAGE:
            content.append("  public static final Field ").append(messageType.getName().toUpperCase()).append("_").append(field.getName().toUpperCase()).append(" = ").append(messageType.getName().toUpperCase()).append(".addField(").append(field.getNumber()).append(", SCHEMA.of(\"").append(blah(field.getTypeName())).append("\"));\r\n");
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

  private static String getJavaTypeFqn(DescriptorProtos.FieldDescriptorProto field) {
    switch (field.getType()) {
      case TYPE_MESSAGE:
        return blah(field.getTypeName());
      case TYPE_STRING:
        return "java.lang.String";
      case TYPE_ENUM:
        return "java.lang.Integer";
      case TYPE_DOUBLE:
        return "java.lang.Double";
      case TYPE_BOOL:
        return "java.lang.Boolean";
      default:
        return null;
    }
  }

  private static class MessageType {
    final String javaPkgFqn;
    final String name;
    final List<FieldDesc> fields = new ArrayList<>();
    MessageType(String javaPkgFqn,
                DescriptorProtos.DescriptorProto desc) {

      // Find nested map entries ...
      Set<String> mapEntries = new HashSet<>();
      for (DescriptorProtos.DescriptorProto nested : desc.getNestedTypeList()) {
        if (nested.getOptions().getMapEntry()) {
          mapEntries.add(nested.getName());
        } else {
          throw new UnsupportedOperationException();
        }
      }

      desc.getFieldList().forEach(fieldDesc -> {
        // Special handling
        if (fieldDesc.getType() == DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE &&
            mapEntries.contains(blah(fieldDesc.getTypeName()))) {
          fields.add(new FieldDesc(mixedLower(fieldDesc.getName()), "java.util.Map"));
        } else {
          String javaTypeFqn = getJavaTypeFqn(fieldDesc);
          if (javaTypeFqn != null) {
            fields.add(new FieldDesc(mixedLower(fieldDesc.getName()), javaTypeFqn));
          }
        }
      });

      this.javaPkgFqn = javaPkgFqn;
      this.name = desc.getName();
    }

    static class FieldDesc {
      final String name;
      final String javaType;
      FieldDesc(String name, String javaType) {
        this.name = name;
        this.javaType = javaType;
      }
    }
  }

  private String extractJavaPkgFqn(DescriptorProtos.FileDescriptorProto proto) {
    DescriptorProtos.FileOptions options = proto.getOptions();
    String javaPackage = options.getJavaPackage();
    if (!Strings.isNullOrEmpty(javaPackage)) {
      return javaPackage;
    }
    return Strings.nullToEmpty(proto.getPackage());
  }

  // java keywords from: https://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.9
  private static final List<CharSequence> JAVA_KEYWORDS = Arrays.asList(
    "abstract",
    "assert",
    "boolean",
    "break",
    "byte",
    "case",
    "catch",
    "char",
    "class",
    "const",
    "continue",
    "default",
    "do",
    "double",
    "else",
    "enum",
    "extends",
    "final",
    "finally",
    "float",
    "for",
    "goto",
    "if",
    "implements",
    "import",
    "instanceof",
    "int",
    "interface",
    "long",
    "native",
    "new",
    "package",
    "private",
    "protected",
    "public",
    "return",
    "short",
    "static",
    "strictfp",
    "super",
    "switch",
    "synchronized",
    "this",
    "throw",
    "throws",
    "transient",
    "try",
    "void",
    "volatile",
    "while",
    // additional ones added by us
    "true",
    "false"
  );

  /**
   * Adjust a method name prefix identifier to follow the JavaBean spec:
   * <ul>
   *   <li>decapitalize the first letter</li>
   *   <li>remove embedded underscores & capitalize the following letter</li>
   * </ul>
   *
   * Finally, if the result is a reserved java keyword, append an underscore.
   *
   * @param word method name
   * @return lower name
   */
  private static String mixedLower(String word) {
    StringBuffer w = new StringBuffer();
    w.append(Character.toLowerCase(word.charAt(0)));

    boolean afterUnderscore = false;

    for (int i = 1; i < word.length(); ++i) {
      char c = word.charAt(i);

      if (c == '_') {
        afterUnderscore = true;
      } else {
        if (afterUnderscore) {
          w.append(Character.toUpperCase(c));
        } else {
          w.append(c);
        }
        afterUnderscore = false;
      }
    }

    if (JAVA_KEYWORDS.contains(w)) {
      w.append('_');
    }

    return w.toString();
  }

  private List<PluginProtos.CodeGeneratorResponse.File> generateDataObjectsFiles(List<MessageType> messageTypeList) {
    return messageTypeList.stream()
      .map(this::buildFiles)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
  }

  private List<PluginProtos.CodeGeneratorResponse.File> buildFiles(MessageType messageType) {
    List<PluginProtos.CodeGeneratorResponse.File> files = new ArrayList<>();
    StringBuilder content = new StringBuilder();
    content.append("package ").append(messageType.javaPkgFqn).append(";\r\n");
    content.append("@io.vertx.codegen.annotations.DataObject\r\n");
    content.append("public class ").append(messageType.name).append(" {\r\n");
    messageType.fields.forEach(fd -> {
      content.append("  private ").append(fd.javaType).append(" ").append(fd.name).append(";\r\n");
    });
    messageType.fields.forEach(fd -> {
      String getter = "get" + Character.toUpperCase(fd.name.charAt(0)) + fd.name.substring(1);
      String setter = setterOf(fd.name);
      content.append("  public ")
        .append(fd.javaType)
        .append(" ")
        .append(getter)
        .append("() { \r\n");
      content.append("    return ").append(fd.name).append(";\r\n");
      content.append("  };\r\n");
      content.append("  public ")
        .append(messageType.name)
        .append(" ")
        .append(setter)
        .append("(")
        .append(fd.javaType)
        .append(" ")
        .append(fd.name)
        .append(") { \r\n");
      content.append("    this.").append(fd.name).append(" = ").append(fd.name).append(";\n");
      content.append("    return this;\r\n");
      content.append("  };\r\n");
    });
    content.append("}\r\n");
    files.add(buildFile(messageType, content.toString()));
    return files;
  }

/*
  private List<PluginProtos.CodeGeneratorResponse.File> buildFiles(ServiceContext context) {
    List<PluginProtos.CodeGeneratorResponse.File> files = new ArrayList<>();
    if (options.generateClient || options.generateService) {
      files.add(buildContractFile(context));
    }
    if (options.generateClient) {
      files.add(buildClientFile(context));
      files.add(buildGrpcClientFile(context));
    }
    if (options.generateService) {
      files.add(buildServiceFile(context));
      files.add(buildGrpcServiceFile(context));
    }
    if (options.generateIo) {
      files.add(buildGrpcIoFile(context));
    }
    return files;
  }
*/

/*
  private PluginProtos.CodeGeneratorResponse.File buildContractFile(ServiceContext context) {
    context.fileName = context.classPrefix + context.serviceName + ".java";
    return buildFile(context, applyTemplate("contract.mustache", context));
  }

  private PluginProtos.CodeGeneratorResponse.File buildClientFile(ServiceContext context) {
    context.fileName = context.classPrefix + context.serviceName + "Client.java";
    return buildFile(context, applyTemplate("client.mustache", context));
  }

  private PluginProtos.CodeGeneratorResponse.File buildGrpcClientFile(ServiceContext context) {
    context.fileName = context.classPrefix + context.serviceName + "GrpcClient.java";
    return buildFile(context, applyTemplate("grpc-client.mustache", context));
  }

  private PluginProtos.CodeGeneratorResponse.File buildServiceFile(ServiceContext context) {
    context.fileName = context.classPrefix + context.serviceName + "Service.java";
    return buildFile(context, applyTemplate("service.mustache", context));
  }

  private PluginProtos.CodeGeneratorResponse.File buildGrpcServiceFile(ServiceContext context) {
    context.fileName = context.classPrefix + context.serviceName + "GrpcService.java";
    return buildFile(context, applyTemplate("grpc-service.mustache", context));
  }

  private PluginProtos.CodeGeneratorResponse.File buildGrpcIoFile(ServiceContext context) {
    context.fileName = context.classPrefix + context.serviceName + "GrpcIo.java";
    return buildFile(context, applyTemplate("grpc-io.mustache", context));
  }
*/

  private PluginProtos.CodeGeneratorResponse.File buildFile(MessageType messageType, String content) {
    return PluginProtos.CodeGeneratorResponse.File
      .newBuilder()
      .setName(absoluteFileName(messageType))
      .setContent(content)
      .build();
  }

  private static String absoluteFileName(MessageType messageType) {
    return absoluteFileName(messageType.javaPkgFqn, messageType.name);
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
