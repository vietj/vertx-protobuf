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

import com.google.protobuf.AnyProto;
import com.google.protobuf.ApiProto;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DurationProto;
import com.google.protobuf.EmptyProto;
import com.google.protobuf.FieldMaskProto;
import com.google.protobuf.JavaFeaturesProto;
import com.google.protobuf.SourceContextProto;
import com.google.protobuf.StructProto;
import com.google.protobuf.TimestampProto;
import com.google.protobuf.TypeProto;
import com.google.protobuf.WrappersProto;
import com.google.protobuf.compiler.PluginProtos;
import com.salesforce.jprotoc.Generator;
import com.salesforce.jprotoc.GeneratorException;
import io.vertx.grpc.plugin.schema.EnumTypeDeclaration;
import io.vertx.grpc.plugin.schema.FieldDeclaration;
import io.vertx.grpc.plugin.schema.MessageTypeDeclaration;
import io.vertx.grpc.plugin.schema.SchemaGenerator;
import io.vertx.protobuf.extension.VertxProto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    final boolean generate;
    Descriptors.FileDescriptor fileDesc;
    Node(DescriptorProtos.FileDescriptorProto fileDescProto, boolean generate) {
      this.fileDescProto = fileDescProto;
      this.dependencies = new ArrayList<>();
      this.generate = generate;
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
      nodeMap.put(fileDescProto.getName(), new Node(fileDescProto, true));
    }
    Map<String, Node> wellKnownDependencies = new LinkedHashMap<>();
    for (Node node : nodeMap.values()) {
      for (String dependency : node.fileDescProto.getDependencyList()) {
        Node depNode = nodeMap.get(dependency);
        if (depNode == null) {
          switch (dependency) {
            case "google/protobuf/any.proto":
              depNode = new Node(AnyProto.getDescriptor().toProto(), false);
              break;
            case "google/protobuf/api.proto":
              depNode = new Node(ApiProto.getDescriptor().toProto(), false);
              break;
            case "google/protobuf/descriptor.proto":
              depNode = new Node(DescriptorProtos.getDescriptor().toProto(), false);
              break;
            case "google/protobuf/duration.proto":
              depNode = new Node(DurationProto.getDescriptor().toProto(), false);
              break;
            case "google/protobuf/empty.proto":
              depNode = new Node(EmptyProto.getDescriptor().toProto(), false);
              break;
            case "google/protobuf/field_mask.proto":
              depNode = new Node(FieldMaskProto.getDescriptor().toProto(), false);
              break;
            case "google/protobuf/java_features.proto":
              depNode = new Node(JavaFeaturesProto.getDescriptor().toProto(), false);
              break;
            case "google/protobuf/source_context.proto":
              depNode = new Node(SourceContextProto.getDescriptor().toProto(), false);
              break;
            case "google/protobuf/struct.proto":
              depNode = new Node(StructProto.getDescriptor().toProto(), false);
              break;
            case "google/protobuf/timestamp.proto":
              depNode = new Node(TimestampProto.getDescriptor().toProto(), false);
              break;
            case "google/protobuf/type.proto":
              depNode = new Node(TypeProto.getDescriptor().toProto(), false);
              break;
            case "google/protobuf/wrappers.proto":
              depNode = new Node(WrappersProto.getDescriptor().toProto(), false);
              break;
            case "vertx.proto":
              depNode = new Node(VertxProto.getDescriptor().toProto(), false);
              break;
            default:
              throw new UnsupportedOperationException("Import not found " + dependency);
          }
          wellKnownDependencies.put(dependency, depNode);
        }
      }
    }
    nodeMap.putAll(wellKnownDependencies);
    nodeMap.values().forEach(node -> {
      for (String dependency : node.fileDescProto.getDependencyList()) {
        node.dependencies.add(nodeMap.get(dependency));
      }
    });

    List<PluginProtos.CodeGeneratorResponse.File> files = new ArrayList<>();

    Map<String, List<Descriptors.FileDescriptor>> byPkg = new LinkedHashMap<>();
    nodeMap
      .values()
      .stream()
      .filter(node -> node.generate)
      .forEach(fileDescProto -> {
      Descriptors.FileDescriptor fileDesc;
      try {
        fileDesc = fileDescProto.build();
      } catch (Descriptors.DescriptorValidationException e) {
        GeneratorException ex = new GeneratorException(e.getMessage());
        ex.initCause(e);
        throw ex;
      }

      String key = Utils.extractJavaPkgFqn(fileDesc);
      byPkg.computeIfAbsent(key, k -> new ArrayList<>()).add(fileDesc);
    });

    byPkg.forEach((javaPkgFqn, v) -> {
      Map<String, Descriptors.Descriptor> messages = new LinkedHashMap<>();
      List<Descriptors.EnumDescriptor> enums = new ArrayList<>();
      for (Descriptors.FileDescriptor f : v) {
        Map<String, Descriptors.Descriptor> res = Utils.transitiveClosure(f.getMessageTypes());
        messages.putAll(res);
        enums.addAll(f.getEnumTypes());
        res
          .values()
          .stream()
          .flatMap(descriptor -> descriptor.getEnumTypes().stream())
          .forEach(enums::add);
      }

      files.add(new MessageBaseGenerator(javaPkgFqn).generate());
      files.addAll(new ElementGenerator(javaPkgFqn, new ArrayList<>(messages.values()), enums).generate());
//      files.add(new SchemaGenerator(javaPkgFqn, new ArrayList<>(messages.values())).generate());

      List<MessageTypeDeclaration> list = new ArrayList<>();
      List<FieldDeclaration> list2 = new ArrayList<>();
      init(messages.values(), list, list2);
      files.addAll(generate(javaPkgFqn, new SchemaGenerator(javaPkgFqn, list, list2)));

      files.add(new ProtoReaderGenerator(javaPkgFqn, new ArrayList<>(messages.values())).generate());
      files.add(new ProtoWriterGenerator(javaPkgFqn, new ArrayList<>(messages.values())).generate());
    });

    return files;
  }

  private List<PluginProtos.CodeGeneratorResponse.File> generate(String javaPkgFqn, SchemaGenerator schemaGenerator) {
    return Arrays.asList(
      PluginProtos.CodeGeneratorResponse.File
        .newBuilder()
        .setName(Utils.absoluteFileName(javaPkgFqn, "FieldLiteral"))
        .setContent(schemaGenerator.generateFieldLiterals())
        .build(),
      PluginProtos.CodeGeneratorResponse.File
        .newBuilder()
        .setName(Utils.absoluteFileName(javaPkgFqn, "MessageLiteral"))
        .setContent(schemaGenerator.generateMessageLiterals())
        .build());
  }

  private void init(Collection<Descriptors.Descriptor> fileDesc, List<MessageTypeDeclaration> list, List<FieldDeclaration> list2) {
    Map<Descriptors.EnumDescriptor, EnumTypeDeclaration> list3 = new LinkedHashMap<>();
    fileDesc.forEach(messageType -> {
      list.add(new MessageTypeDeclaration(Utils.schemaIdentifier(messageType), messageType.getName()));
      messageType.getFields().forEach(field -> {
        String identifier = Utils.schemaIdentifier(field);
        String messageTypeRef = Utils.schemaIdentifier(messageType);
        int number = field.getNumber();
        String typeExpr;
        switch (field.getType()) {
          case FLOAT:
            typeExpr = "ScalarType.FLOAT";
            break;
          case DOUBLE:
            typeExpr = "ScalarType.DOUBLE";
            break;
          case BOOL:
            typeExpr = "ScalarType.BOOL";
            break;
          case STRING:
            typeExpr = "ScalarType.STRING";
            break;
          case ENUM:
            typeExpr = Utils.javaTypeOfInternal(field) + ".TYPE";
            break;
          case BYTES:
            typeExpr = "ScalarType.BYTES";
            break;
          case INT32:
            typeExpr = "ScalarType.INT32";
            break;
          case INT64:
            typeExpr = "ScalarType.INT64";
            break;
          case UINT32:
            typeExpr = "ScalarType.UINT32";
            break;
          case UINT64:
            typeExpr = "ScalarType.UINT64";
            break;
          case SINT32:
            typeExpr = "ScalarType.SINT32";
            break;
          case SINT64:
            typeExpr = "ScalarType.SINT64";
            break;
          case FIXED32:
            typeExpr = "ScalarType.FIXED32";
            break;
          case FIXED64:
            typeExpr = "ScalarType.FIXED64";
            break;
          case SFIXED32:
            typeExpr = "ScalarType.SFIXED32";
            break;
          case SFIXED64:
            typeExpr = "ScalarType.SFIXED64";
            break;
          case MESSAGE:
            typeExpr = Utils.extractJavaPkgFqn(field.getMessageType().getFile()) + ".MessageLiteral." + Utils.literalIdentifier(field.getMessageType());
            break;
          default:
            return;
        }
        list2.add(new FieldDeclaration(identifier, field.getName(), field.isMapField(), Utils.isMapKey(field), Utils.isMapValue(field), field.isRepeated(), field.isPacked(), field.getJsonName(), messageTypeRef, number, field.getContainingType().getName(), typeExpr));
        if (field.getType() == Descriptors.FieldDescriptor.Type.ENUM) {
          Descriptors.EnumDescriptor enumType = field.getEnumType();
          if (!list3.containsKey(enumType)) {
            EnumTypeDeclaration decl = new EnumTypeDeclaration(enumType.getName());
            list3.put(enumType, decl);
            enumType.getValues().forEach(value -> {
              decl.numberToIdentifier.put(value.getNumber(), value.getName());
            });
          }
        }
      });
    });
  }
}
